package io.rtserver;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

import io.rtserver.Communicator;
import io.rtserver.pubsub.DefaultChannelCollection;
import io.rtserver.DefaultCommunicator;
import io.rtserver.pubsub.DefaultPubSub;
import io.rtserver.SocketMessageHandler;
import io.rtserver.pubsub.PubSub;
import io.rtserver.pubsub.ChannelCollection;
import io.rtserver.sockets.DefaultSocketCollection;
import io.rtserver.sockets.SocketCollection;

public class ServerBootstrap {

	private static final String JSON_RPC_MESSAGE_HANDLER = "io.rtserver.rpc.json.internal.JsonRpcMessageHandler";
	private static final String MESSAGE_HANDLER = "messageHandler";
	private static final String WEB_DIRECTORY = "webDirectory";
	private static final String DEFAULT_WEB_DIR = "/web";
	private static final Logger LOGGER = LoggerFactory.getLogger(ServerBootstrap.class);
	
	private static SocketCollection connectionCollection;
	private static ChannelCollection channelCollection;
	private static Communicator communicator;
	private static PubSub pubSub;
	private static SocketMessageHandler messageHandler;
	private static ImmutableMap<String, Object> configuration;
	private static String absoluteWebDirectoryPath;
	
	public static void initialize(Map<String, Object> config) {
		
		if (config == null) {
			config = new HashMap<String, Object>();
		}
		
		configuration = initializeConfig(config);
		
		connectionCollection = new DefaultSocketCollection();
		channelCollection = new DefaultChannelCollection();
		communicator = new DefaultCommunicator(connectionCollection);
		pubSub = new DefaultPubSub(communicator, channelCollection);
		messageHandler = initializeMessageHandler();
		messageHandler.serverStarted(configuration);
	}
	
	public static SocketCollection getConnectionCollection() {
		
		return connectionCollection;
	}
	
	public static Communicator getCommunicator() {
		
		return communicator;
	}
	
	public static PubSub getPubSub() {
		
		return pubSub;
	}
	
	public static String getAbsoluteWebDirectoryPath() {
		return absoluteWebDirectoryPath;
	}
	
	public static SocketMessageHandler getSocketMessageHandler() {
		return messageHandler;
	}
	
	private static ImmutableMap<String, Object> initializeConfig(Map<String, Object> providedConfig) {
		
		ImmutableMap.Builder<String, Object> builder = new ImmutableMap.Builder<String, Object>();
		
		for (Map.Entry<String, Object> entry : providedConfig.entrySet()) {
			builder.put(entry.getKey(), entry.getValue());
		}
		
		// Default message handler is to use json rpc.
		if (!providedConfig.containsKey(MESSAGE_HANDLER)) {
			builder.put(MESSAGE_HANDLER, JSON_RPC_MESSAGE_HANDLER);
		}
		
		// Set web directory to default if not specified
		if (!providedConfig.containsKey(WEB_DIRECTORY)) {
			builder.put(WEB_DIRECTORY, DEFAULT_WEB_DIR);
		}
		
		ImmutableMap<String, Object> finalConfig = builder.build();
		
		// Set the absolute path to the web directory.
		String runningDirectory = System.getProperty("user.dir");
		absoluteWebDirectoryPath = new File(runningDirectory +
			finalConfig.get(WEB_DIRECTORY)).toString();
		
		return finalConfig;
	}
	
	private static SocketMessageHandler initializeMessageHandler() {
		
		String handler = (String)configuration.get(MESSAGE_HANDLER);
		Class c = null;
		Object handlerInstance = null;
		
		try {
			
			c = Class.forName(handler);
			
		} catch (ClassNotFoundException ex) {
			
			System.err.print("Failed to start RTServer. Could not find the specified message " +
				"handler: " + handler);
			System.exit(0);
		}
		
		try {
			Constructor constructor = c.getConstructor(Map.class);
			handlerInstance = constructor.newInstance(configuration);
			
		} catch (Exception ex) {
			
			System.err.println("Failed to create an instance of the specified message " +
				"handler: " + handler);
			System.err.println(ex);
			System.exit(0);
		}
		
		return (SocketMessageHandler)handlerInstance;
	}
}
