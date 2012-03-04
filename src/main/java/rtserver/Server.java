package rtserver;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.Executors;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;
import rtserver.internal.RTServerPipelineFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server {
	
	private static final String JSON_RPC_MESSAGE_HANDLER = "rtserver.rpc.json.internal.JsonRpcMessageHandler";
	private static final String MESSAGE_HANDLER = "messageHandler";
	private static final String WEB_DIRECTORY = "webDirectory";
	private static final String DEFAULT_WEB_DIR = "/web";
	
	private static Connections connections;
	private static Communicator communicator;
	public final MessageHandler messageHandler;
	public final int port;
	public static String absoluteWebDirectoryPath;
	private final Logger logger;
	public Map<String, Object> config = null;
	
	
	/**
	 * Creates a server. Only one server can be created per JVM.
	 * @param port The port the server should run on.
	 * @param config A map of configuration options.
	 */
	public Server(int port, Map<String, Object> config) {
		
		this.config = initializeConfig(config);
		this.logger = LoggerFactory.getLogger(Server.class);
		
		this.port = port;
		
		connections = new DefaultConnectionsCollection();
		communicator = new DefaultCommunicator(connections);
		messageHandler = initializeMessageHandler();
		messageHandler.serverStarted(config);
	}
	
	/**
	 * Starts the server.
	 */
	public void start() {
		
		ServerBootstrap bootstrap = new ServerBootstrap(
			new NioServerSocketChannelFactory(
				Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool()));
		
		Timer timer = new HashedWheelTimer();
		bootstrap.setPipelineFactory(new RTServerPipelineFactory(messageHandler, timer));
			
		bootstrap.bind(new InetSocketAddress(this.port));
			
		this.logger.info("RTServer Started");
	}
	
	/**
	 * Get the absolute path to the path that is holding the web resources.
	 * @return The absolute path that is holding your web resources.
	 */
	public static String getAbsoluteWebDirectoryPath() {
		return absoluteWebDirectoryPath;
	}
	
	public static Connections getConnections() {
		
		return connections;
	}
	
	public static Communicator getCommunicator() {
		
		return communicator;
	}
	
	private Map<String, Object> initializeConfig(Map<String, Object> providedConfig) {
		
		// Default message handler is to use json rpc.
		if (!providedConfig.containsKey(MESSAGE_HANDLER)) {
			providedConfig.put(MESSAGE_HANDLER, JSON_RPC_MESSAGE_HANDLER);
		}
		
		// Set web directory to default if not specified
		if (!providedConfig.containsKey(WEB_DIRECTORY)) {
			providedConfig.put(WEB_DIRECTORY, DEFAULT_WEB_DIR);
		}
		
		// Set the absolute path to the web directory.
		String runningDirectory = System.getProperty("user.dir");
		absoluteWebDirectoryPath = new File(runningDirectory +
			providedConfig.get(WEB_DIRECTORY)).toString();
		
		return providedConfig;
	}
	
	private MessageHandler initializeMessageHandler() {
		
		String handler = (String)this.config.get(MESSAGE_HANDLER);
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
			handlerInstance = constructor.newInstance(this.config);
			
		} catch (Exception ex) {
			
			System.err.println("Failed to create an instance of the specified message " +
				"handler: " + handler);
			System.err.println(ex);
			System.exit(0);
		}
		
		return (MessageHandler)handlerInstance;
	}
}
