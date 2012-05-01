package io.rtserver.netty;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.Executors;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;

import io.rtserver.ServerBootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server {
	
	public final int port;
	private final Logger logger;
	
	/**
	 * Creates a server. Only one server can be created per JVM.
	 * @param port The port the server should run on.
	 * @param config A map of configuration options.
	 */
	public Server(int port, Map<String, Object> config) {
		
		ServerBootstrap.initialize(config);
		this.logger = LoggerFactory.getLogger(Server.class);
		this.port = port;
	}
	
	/**
	 * Starts the server.
	 */
	public void start() {
		
		org.jboss.netty.bootstrap.ServerBootstrap bootstrap = new org.jboss.netty.bootstrap.ServerBootstrap(
			new NioServerSocketChannelFactory(
				Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool()));
		
		Timer timer = new HashedWheelTimer();
		bootstrap.setPipelineFactory(new RTServerPipelineFactory(ServerBootstrap.getSocketMessageHandler(), timer));
			
		bootstrap.bind(new InetSocketAddress(this.port));
			
		this.logger.info("RTServer Started");
	}
}
