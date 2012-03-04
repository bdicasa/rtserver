package rtserver.internal;
//package rst;
//
//import static org.jboss.netty.handler.codec.http.HttpHeaders.setContentLength;
//import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
//import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;
//import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.*; 
//import static org.jboss.netty.handler.codec.http.HttpHeaders.Values.*;
//
//import org.jboss.netty.buffer.ChannelBuffer;
//import org.jboss.netty.buffer.ChannelBuffers;
//import org.jboss.netty.channel.Channel;
//import org.jboss.netty.channel.ChannelFuture;
//import org.jboss.netty.channel.ChannelHandlerContext;
//import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
//import org.jboss.netty.handler.codec.http.HttpResponse;
//import org.jboss.netty.util.CharsetUtil;
//import org.jboss.netty.buffer.ChannelBuffers;
//import org.jboss.netty.channel.ChannelFuture;
//import org.jboss.netty.channel.ChannelHandler;
//import org.jboss.netty.channel.ChannelHandlerContext;
//import org.jboss.netty.handler.codec.http.DefaultHttpChunk;
//import org.jboss.netty.handler.codec.http.HttpChunk;
//import org.jboss.netty.util.CharsetUtil;
//
//public class HttpStreamingConnection extends HttpConnection {
//	
//	
//	public HttpStreamingConnection(String id, ChannelHandlerContext context) {
//		super(id, context);
//	}
//	
//	@Override
//	public ChannelFuture write(String data) {
//		
//		return writeData(data);
//	}
//
//	@Override
//	public ChannelFuture write(ChannelBuffer data) {
//		
//		return writeData(data);
//	}
//
//	@Override
//	public ChannelFuture write(byte[] data) {
//		
//		return writeData(data);
//	}
//	
//	@Override
//	public ChannelFuture writeToRequester(Object data) {
//		
//		return writeResponse(this.requesterChannel, data);
//	}
//	
//	private ChannelFuture writeData(Object data) {
//		
//		HttpChunk chunk;
//		ChannelBuffer buffer = null;
//		
//		if (data instanceof String) {
//			
//			buffer = ChannelBuffers.copiedBuffer((String) data, CharsetUtil.UTF_8);
//			
//		} else if (data instanceof byte[]) {
//			
//			buffer = ChannelBuffers.copiedBuffer((byte[]) data);
//			
//		} else if (data instanceof ChannelBuffer) {
//			
//			buffer = (ChannelBuffer) data;
//		}
//		
//		chunk = new DefaultHttpChunk(buffer);
//		
//		return channel.write(chunk);
//	}
//
//
//}
