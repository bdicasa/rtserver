package rtserver.internal;

import static org.jboss.netty.handler.codec.http.HttpHeaders.*; 
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.*; 
import static org.jboss.netty.handler.codec.http.HttpHeaders.Values.*;  
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.*; 
import static org.jboss.netty.handler.codec.http.HttpVersion.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URLDecoder;
import java.util.Collections;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelFutureProgressListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.DefaultFileRegion;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.FileRegion;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrameDecoder;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrameEncoder;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import org.jboss.netty.handler.ssl.SslHandler;
import org.jboss.netty.handler.stream.ChunkedFile;
import org.jboss.netty.handler.timeout.IdleState;
import org.jboss.netty.handler.timeout.IdleStateAwareChannelUpstreamHandler;
import org.jboss.netty.handler.timeout.IdleStateEvent;
import org.jboss.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rtserver.Connection;
import rtserver.Connections;
import rtserver.DefaultConnectionsCollection;
import rtserver.MessageHandler;
import rtserver.Request;
import rtserver.Response;
import rtserver.Server;
import rtserver.internal.connections.ConnectionType;

public class RTServerHandler extends IdleStateAwareChannelUpstreamHandler {

	public static final String WEBSOCKET_PATH = "/websocket";
	private Connection connection;
	private final Logger logger = LoggerFactory.getLogger(RTServerHandler.class);
	private final MessageHandler messageHandler;
	private final Connections connections;
	
	public RTServerHandler(MessageHandler messageHandler, Connections connections) {
		this.connections = connections;
		this.messageHandler = messageHandler;
	}
	
	@Override
	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) {

	}
	
	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) {
		closeConnection(ctx, "channelClosed");
	}
	
//	@Override
//	public void channelUnbound(ChannelHandlerContext ctx, ChannelStateEvent e) {
//		closeConnection(ctx, "channelUnbound");
//	}
//	
//	@Override
//	public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
//		closeConnection(ctx, "channelDisconnected");
//	}
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		
		try {
			Object msg = e.getMessage();
			
			if (msg instanceof HttpRequest) {
				handleHttpRequest(ctx, (HttpRequest) msg);
			} else if (msg instanceof WebSocketFrame) {
				handleWebSocketMessage(ctx, (WebSocketFrame) msg);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	@Override
	public void channelIdle(ChannelHandlerContext ctx, IdleStateEvent e) {
		
		if (e.getState() == IdleState.WRITER_IDLE) {
			
			if (connection != null) {
				
				if (connection instanceof LongPollingConnection) {
					
					((LongPollingConnection)connection).writeTimeout();
				}
			}
		}
	}
	
	private void closeConnection(ChannelHandlerContext ctx, String from) {
		
		if (connection != null) {
			
			if (connection instanceof HttpConnection) {
				
				LongPollingConnection longPollConnection = (LongPollingConnection)connection;
				
				// If this channel context is the writer context for the polling connection
				// and our requester channel is closed, remove the connection as both
				// channels associated with the HttpConnection are closed.
				if (ctx.equals(longPollConnection.getWriterContext())) {
					
					longPollConnection.setWriterContext(null);
					logger.debug("Writer channel closed for connection " + this.connection.getId());
					//if (longPollConnection.getRequesterContext() == null ||
					//	!longPollConnection.getRequesterContext().getChannel().isConnected()) {
						
						longPollConnection.close();
						this.connection = null;
					//}
				
				// If this channel context is the request context and our
				// writer channel is closed, remove the connection.
				//} else if (ctx.equals(longPollConnection.getRequesterContext())) {
//					
//					longPollConnection.setRequesterContext(null);
//					logger.debug("Requester channel closed for connection " + this.connection.getId());
//					if (longPollConnection.getWriterContext() == null ||
//						!longPollConnection.getWriterContext().getChannel().isConnected()) {
//						
//						longPollConnection.close();
//						this.connection = null;
//					}
				}
			} else {
				
				this.connections.remove(connection.getId());
				logger.debug("Connection closed from " + from + ": " + connection.getId());
				this.connection = null;
			}
		} else {
			logger.info("Unknown connection closed.");
		}
	}
	
	private void handleHttpRequest(ChannelHandlerContext ctx, HttpRequest req) throws Exception {
		
		if (req.getUri().equals(WEBSOCKET_PATH)) {
			
			// A websocket handshake request was made, complete handshake
			if (req.getHeader(CONNECTION).toLowerCase().contains(Values.UPGRADE.toLowerCase()) &&
				WEBSOCKET.equalsIgnoreCase(req.getHeader(Names.UPGRADE))) {
				
				performWebSocketHandshake(ctx, req);
				addConnection(ctx, ConnectionType.WEB_SOCKET);
				
			
			// A websocket upgrade request wasn't made, we will use long polling to handle
			// real time communication with the client.
			} else {
				
				if (req.getMethod() == HttpMethod.GET) {
					
					if (connection == null) {
						
						String initialConnection = req.getHeader("X-Initial-Connection");
						
						if (initialConnection != null && initialConnection.equals("true")) {
							
							addConnection(ctx, ConnectionType.LONG_POLLING);
							LongPollingConnection longPollConnection = (LongPollingConnection)connection;
							longPollConnection.setWriterContext(ctx);
							longPollConnection.writeHandshake();
							
						} else {
							
							String connectionId = req.getHeader("X-Connection-Id");
							connection = this.connections.get(connectionId);

						}
					}
					
					if (connection == null) {
						// If connection is still null, the server must have lost the connectionId.
						// Check to see if the client has a connection id associated with that, and
						// add a connection using that id.
						
						String connectionId = req.getHeader("X-Connection-Id");
						
						if (connectionId != null) {
							connection = this.connections.add(ctx, ConnectionType.LONG_POLLING, connectionId);
						}
					}
					
					if (connection == null) {
						// Nothing else we can do, send disconnected response
						sendConnectionLostResponse(ctx, req);
					} else {
						LongPollingConnection pollingConnection = (LongPollingConnection) connection;
						
						// Ensure we have the proper channel context associated with the
						// long polling connection.
						pollingConnection.setWriterContext(ctx);
						// Determine what to do now that we have a writer request
						pollingConnection.writerRequestOpened();
					}
					
				} else if (req.getMethod() == HttpMethod.POST) {
					
					if (connection == null) {
						String connectionId = req.getHeader("X-Connection-Id");
						connection = this.connections.get(connectionId);
					}
					
					if (connection == null) {
						// If the connection is still null it must be lost
						sendConnectionLostResponse(ctx, req);
					} else {
						((LongPollingConnection)connection).setRequesterContext(ctx);
						LongPollingConnection pollingConnection = (LongPollingConnection)connection;
						pollingConnection.setRequesterContext(ctx);
						handleLongPollingMessage(ctx, req);
					}
				}
			}
			
		} else {
			
			// If the URI contains a period, look for an appropriate file to serve first.
			if (req.getUri().contains(".")) {
				serveFile(ctx, req);
			}
		}
	}
	
	private void sendConnectionLostResponse(ChannelHandlerContext context, HttpRequest req) {
		
		HttpResponse response = new DefaultHttpResponse(HTTP_1_1, BAD_REQUEST);
		response.setContent(ChannelBuffers.copiedBuffer("disconnected", CharsetUtil.UTF_8));
		response.setHeader(CONTENT_TYPE, "text/html");
		setContentLength(response, response.getContent().readableBytes());
		sendHttpResponse(context, req, response);
	}
	
	private void addConnection(ChannelHandlerContext ctx, ConnectionType connectionType) {
		
		connection = this.connections.add(ctx, connectionType);
		logger.debug("New connection added " + connection.getId());
	}
	
	private void serveFile(ChannelHandlerContext ctx, HttpRequest req) throws Exception {
		
		final String path = sanitizeUri(req.getUri());
		
		if (path == null) {
			sendError(ctx, FORBIDDEN);
			return;
		}
		
		
		File file = new File(path);
		if (file.isHidden() || !file.exists()) {
			sendError(ctx, NOT_FOUND);
			return;
		}
		
		if (!file.isFile()) {
			sendError(ctx, FORBIDDEN);
			return;
		}
		
		RandomAccessFile raf;
		try {
			raf = new RandomAccessFile(file, "r");
		} catch (FileNotFoundException ex) {
			sendError(ctx, NOT_FOUND);
			return;
		}
		
		long fileLength = 0;
		try {
			fileLength = raf.length();
		} catch (IOException ex) {
			sendError(ctx, INTERNAL_SERVER_ERROR);
		}
		
		HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
		setContentLength(response, fileLength);
		
		Channel ch = ctx.getChannel();
		
		// Write the initial line and header.
		ch.write(response);
		
		ChannelFuture writeFuture;
		if (ch.getPipeline().get(SslHandler.class) != null) {
			// Cannot use zero-copy with HTTPS.
			writeFuture = ch.write(new ChunkedFile(raf, 0, fileLength, 8192));
		} else {
			// No encryption - use zero-copy.
			final FileRegion region =
				new DefaultFileRegion(raf.getChannel(), 0, fileLength);
			writeFuture = ch.write(region);
			writeFuture.addListener(new ChannelFutureProgressListener() {
				public void operationComplete(ChannelFuture future) {
					region.releaseExternalResources();
				}
				
				public void operationProgressed(ChannelFuture future, long amount, long current, long total) {
					
				}
			});
		}
		
		// Decide wheather to close the connection or not.
		if (!isKeepAlive(req)) {
			// Close the connection when the while content is written out.
			writeFuture.addListener(ChannelFutureListener.CLOSE);
		}
	}
	
	private String sanitizeUri(String uri) {
		
		// TO DO: probably want to read request encoding and try decoding using that
		// Decode the path
		try {
			uri = URLDecoder.decode(uri, "UTF-8");
		} catch(Exception ex) {
			return null;
		}
		
		// Convert file separators
		uri = uri.replace('/', File.separatorChar);
		
		// TO DO: what other precations should be taken?
		// Simplistic dumb security check.
		// You will have to do something serious in the production environment.
		if (uri.contains(File.separator + ".") ||
			uri.contains("." + File.separator) ||
			uri.startsWith(".") || uri.endsWith(".")) {
			return null;
		}
		
		return Server.getAbsoluteWebDirectoryPath() + File.separator + uri;
	}
	
	private void performWebSocketHandshake(ChannelHandlerContext ctx, HttpRequest req) throws InterruptedException {
		
		WebSocketServerHandshakerFactory factory = new WebSocketServerHandshakerFactory(
			getWebSocketLocation(req), null, false);
		WebSocketServerHandshaker handshaker = factory.newHandshaker(req);
		
		handshaker.handshake(ctx.getChannel(), req);
	}
	
	private void handleWebSocketMessage(ChannelHandlerContext ctx, WebSocketFrame frame) {
		
		Request request = new Request(connection, frame.getBinaryData());
		Response response = new Response(connection, request);
		this.messageHandler.messageReceived(request, response);
	}
	
	private void handleLongPollingMessage(ChannelHandlerContext ctx, HttpRequest req) {
		
		Request request = new Request(connection, req.getContent());
		Response response = new Response(connection, request);
		this.messageHandler.messageReceived(request, response);
	}
	
	private void sendHttpResponse(ChannelHandlerContext ctx, HttpRequest req, HttpResponse res) {
		
		// Generate an error page if response status code is not OK (200)
		if (res.getStatus().getCode() != 200) {
			res.setContent(
				ChannelBuffers.copiedBuffer(
					res.getStatus().toString(), CharsetUtil.UTF_8));
			setContentLength(res, res.getContent().readableBytes());
		}
		
		// Send the response and close the connection if necessary.
		// TODO: Probably dont want to close the connection if we dont have a status of 200
		ChannelFuture f = ctx.getChannel().write(res);
		if (!isKeepAlive(req)) {
			f.addListener(ChannelFutureListener.CLOSE);
		}
	}
	
	private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
		
		HttpResponse response = new DefaultHttpResponse(HTTP_1_1, status);
		response.setHeader(CONTENT_TYPE, "text/plain; charset=UTF-8");
		response.setContent(ChannelBuffers.copiedBuffer(status.toString() + "\r\n",
			CharsetUtil.UTF_8));
		ctx.getChannel().write(response);
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		
		// TODO : Probably dont want to close the users connection on error
		e.getCause().printStackTrace();
	}
	
	private String getWebSocketLocation(HttpRequest req) {
		return "ws://" + req.getHeader(HOST) + WEBSOCKET_PATH;
	}
}
