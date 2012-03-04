package rtserver.internal.connections;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import rtserver.Connection;

public class WebSocketConnection implements Connection {
	
	private final String id;
	private final ChannelHandlerContext context;
	
	public WebSocketConnection(String id, ChannelHandlerContext context) {
		this.id = id;
		this.context = context;
	}
	
	public String getId() {
		return this.id;
	}
	
	public ChannelFuture write(String data) {
		
		return context.getChannel().write(new TextWebSocketFrame(data));
	}
	
	public ChannelFuture write(ChannelBuffer data) {
		
		return context.getChannel().write(new BinaryWebSocketFrame(data));
	}
	
	public ChannelFuture write(byte[] data) {
		
		ChannelBuffer buffer = ChannelBuffers.copiedBuffer(data);
		return context.getChannel().write(new BinaryWebSocketFrame(buffer));
	}
}
