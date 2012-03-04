package rtserver.internal;

import static org.jboss.netty.channel.Channels.*;

import java.util.Map;

import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.util.Timer;

import rtserver.MessageHandler;
import rtserver.Server;


public class RTServerPipelineFactory implements ChannelPipelineFactory {
	
	private final ChannelHandler idleStateHandler;
	private final MessageHandler messageHandler;
	
	public RTServerPipelineFactory(MessageHandler messageHandler, Timer timer) {
		this.messageHandler = messageHandler;
		this.idleStateHandler = new IdleStateHandler(timer, 0, 30, 0);
	}
	
	public ChannelPipeline getPipeline() throws Exception {
		
		ChannelPipeline pipeline = pipeline();
		
		pipeline.addLast("timeout", this.idleStateHandler);
		pipeline.addLast("decoder", new HttpRequestDecoder());
		pipeline.addLast("aggregator", new HttpChunkAggregator(65536));
		pipeline.addLast("encoder", new HttpResponseEncoder());
		pipeline.addLast("handler", new RTServerHandler(messageHandler, Server.getConnections()));
		
		return pipeline;
	}
}
