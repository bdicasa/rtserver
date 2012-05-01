package io.rtserver.netty.sockets;

import static org.jboss.netty.handler.codec.http.HttpHeaders.setContentLength;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.*; 
import static org.jboss.netty.handler.codec.http.HttpHeaders.Values.*;

import io.rtserver.ServerBootstrap;
import io.rtserver.json.Json;
import io.rtserver.sockets.LongPollingSocket;

import java.util.LinkedList;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.util.CharsetUtil;

public class NettyLongPollingSocket extends HttpSocket implements LongPollingSocket {
	
	private final String id;
	// Lock used for write actions to the connection.
	private Object writeLock = new Object();
	private boolean writeRequestOpen; // Guarded by writelock
	private LinkedList<Object> messageQueue; // Guarded by writelock
	private ChannelHandlerContext writerContext; // Guarded by writelock
	private ChannelHandlerContext requesterContext;
	
	public NettyLongPollingSocket(String id, ChannelHandlerContext writerContext) {
		this.id = id;
		this.writerContext = writerContext;
		messageQueue = new LinkedList<Object>();
	}
	
	public String getId() {
		return this.id;
	}
	
	public void write(String data) {
		
		writeData(data);
	}
	
	public ChannelFuture write(ChannelBuffer data) {
		
		return writeData(data);
	}
	
	public void write(byte[] data) {
		writeData(data);
	}
	
	/**
	 * Writes to the requester channel. The requester channel is the channel associated with
	 * incoming requests.
	 * @param data The data to write.
	 * @return The ChannelFuture associated with the write event.
	 */
	public ChannelFuture writeToRequester(Object data) {
		return writeResponse(this.requesterContext.getChannel(), data);
	}
	
	/**
	 * Writes an empty response to the requester channel. This is required for scenarios where
	 * the server doesn't want to respond to the request, which is perfectly fine using a socket connection.
	 * However HTTP always expects a response so we mimic not responding by writing an empty response
	 * with the X-Empty-Response : true header specified. The client can then choose to ignore any received
	 * messages with this header specified.
	 * @return
	 */
	public void write() {
		
		HttpResponse res = new DefaultHttpResponse(HTTP_1_1, OK);
		res.setHeader("X-Empty-Response", "true");
		res.setHeader(CACHE_CONTROL, NO_CACHE);
		setContentLength(res, 0);
		this.requesterContext.getChannel().write(res);
	}
	
	/**
	 * Notifies this connection that a writer request has been opened. I.e. a long polling
	 * request has been received from the client. If any messages are written to this
	 * connection while waiting for a long polling connection to return they are added to a message
	 * queue. If any messages are in the queue, this method writes those messages to the client
	 * browser immediately. If the queue is empty, the connection is flagged to be available to
	 * write a message as soon as it comes in.
	 * @return If the message queue is not empty and written to the channel, the ChannelFuture is
	 * returned. If the message queue is empty, not write needs to take place so null is returned.
	 */
	public ChannelFuture writerRequestOpened() {
		
		synchronized(writeLock) {
			
			if (messageQueue.size() > 0) {
				writeRequestOpen = false;
				
				String json = Json.stringify(messageQueue);
				messageQueue.clear();
				ChannelBuffer content = ChannelBuffers.copiedBuffer(json, CharsetUtil.UTF_8);
				
				HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
				response.setContent(content);
				response.setHeader(CACHE_CONTROL, NO_CACHE);
				response.setHeader(CONTENT_TYPE, "application/json");
				return writeResponse(this.writerContext.getChannel(), response);
			} else {
				writeRequestOpen = true;
				return null;
			}
			
		}
	}
	
	public void setRequesterContext(ChannelHandlerContext context) {
		
		synchronized (writeLock) {
			this.requesterContext = context;
		}
	}
	
	public ChannelHandlerContext getRequesterContext() {
		synchronized (writeLock) {
			return this.requesterContext;
		}
	}
	
	
	public void setWriterContext(ChannelHandlerContext context) {
		
		synchronized (writeLock) {
			this.writerContext = context;
		}
	}
	
	public ChannelHandlerContext getWriterContext() {
		
		synchronized (writeLock) {
			return this.writerContext;
		}
	}
	
	public ChannelFuture writeTimeout() {
		
		synchronized(writeLock) {
			if (writeRequestOpen)  {
				writeRequestOpen = false;
				HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
				response.addHeader("X-Timeout", "true");
				response.addHeader(CACHE_CONTROL, NO_CACHE);
				response.setContent(ChannelBuffers.EMPTY_BUFFER);
				return writeResponse(this.writerContext.getChannel(), response);
			}
			
			return null;
		}
	}
	
	private ChannelFuture writeData(Object data) {
		
		synchronized(writeLock) {
			
			// If our writer and requester channel are both closed, this connection is
			// no longer active. Remove it.
			if ((this.writerContext == null || !this.writerContext.getChannel().isConnected()) &&
				(this.requesterContext == null || !this.requesterContext.getChannel().isConnected())) {
				
				close();
				return null;
			}
			
			if (writeRequestOpen) {
				
				writeRequestOpen = false;
				String[] messages = { (String)data };
				String json = Json.stringify(messages);
				ChannelBuffer content = ChannelBuffers.copiedBuffer(json, CharsetUtil.UTF_8);
				HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
				response.setHeader(CONTENT_TYPE, "application/json");
				response.setHeader(CACHE_CONTROL, NO_CACHE);
				response.setContent(content);
				return writeResponse(this.writerContext.getChannel(), response);
				
			} else {
				
				messageQueue.addLast(data);
				return null;
				
			}
		}
	}
	
	public ChannelFuture writeHandshake() {
		
		synchronized (writeLock) {
			writeRequestOpen = false;
			// If the client says this is an initial connection we need to respond with
			// the clients connectionId
			HttpResponse res = new DefaultHttpResponse(HTTP_1_1, OK);
			res.setHeader(CONTENT_TYPE, "text/html");
			res.setHeader(CACHE_CONTROL, NO_CACHE);
			res.setContent(ChannelBuffers.copiedBuffer(id, CharsetUtil.UTF_8));
			setContentLength(res, res.getContent().readableBytes());
			return writeResponse(this.writerContext.getChannel(), res);
		}
	}
	
	public void close() {
		
		synchronized (writeLock) {
			
			if (writerContext != null && writerContext.getChannel().isOpen()) {
				writerContext.getChannel().close();
				writerContext = null;
			}
			
			if (requesterContext != null && requesterContext.getChannel().isOpen()) {
				requesterContext.getChannel().close();
				requesterContext = null;
			}
			
			ServerBootstrap.getConnectionCollection().remove(id);
		}
	}
}
