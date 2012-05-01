package io.rtserver.netty.sockets;

import io.rtserver.sockets.WebSocket;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public class NettyWebSocket implements WebSocket {
	
	private final String id;
	private final ChannelHandlerContext context;
	
	public NettyWebSocket(String id, ChannelHandlerContext context) {
		this.id = id;
		this.context = context;
	}
	
	public String getId() {
		return this.id;
	}
	
	public void write(String data) {
		
		context.getChannel().write(new TextWebSocketFrame(data));
	}
	
	public ChannelFuture write(ChannelBuffer data) {
		
		return context.getChannel().write(new BinaryWebSocketFrame(data));
	}
	
	public void write(byte[] data) {
		
		ChannelBuffer buffer = ChannelBuffers.copiedBuffer(data);
		context.getChannel().write(new BinaryWebSocketFrame(buffer));
	}
}
