package rtserver;

import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.jboss.netty.channel.ChannelHandlerContext;

import rtserver.internal.LongPollingConnection;
import rtserver.internal.connections.ConnectionType;
import rtserver.internal.connections.ExistingConnectionIdException;
import rtserver.internal.connections.WebSocketConnection;

import org.slf4j.*;

public class DefaultConnectionsCollection implements Connections{
	
	private Map<String, Connection> connectionMap = null;
	private static final Logger logger = LoggerFactory.getLogger(DefaultConnectionsCollection.class);
	
	public DefaultConnectionsCollection() {
		connectionMap = new ConcurrentHashMap<String, Connection>(100);
	}
	
	/**
	 * Adds a connection with a new random id.
	 * @param context The channel handler context associated with this connection. 
	 * @param connectionType The type of connection to add.
	 * @return The added connection.
	 */
	public Connection add(ChannelHandlerContext context, ConnectionType connectionType) {
		
		Connection connection;
		try {
			connection = add(context, connectionType, createRandomId());
			return connection;
		} catch (ExistingConnectionIdException ex) {
			return add(context, connectionType);
		}
	}
	
	/**
	 * Tries to add a connection with the specified id.
	 * @param context The channel handler context to be associated with this connection.
	 * @param connectionType The type of connection to add.
	 * @param id The id for the connection.
	 * @return The added connection.
	 */
	public Connection add(ChannelHandlerContext context, ConnectionType connectionType, String id) {
		
		if (connectionMap.containsKey(id))
			throw new ExistingConnectionIdException(id);
		
		Connection connection = null;
		
		if (connectionType == ConnectionType.WEB_SOCKET) {
			connection = new WebSocketConnection(id, context);
		} else if (connectionType == ConnectionType.LONG_POLLING) {
			connection = new LongPollingConnection(id, context);
		}
		
		connectionMap.put(id, connection);
		
		return connection;
	}
	
	/**
	 * Creates a random connection id. A random connection id is just a UUID with the dashes removed.
	 * @return The connection id.
	 */
	public String createRandomId() {
		
		return UUID.randomUUID().toString().replaceAll("-", "");
	}
	
	public Map<String, Connection> getAll() {
		return connectionMap;
	}
	
	public Set<String> getAllIds() {
		return connectionMap.keySet();
	}
	
	public Connection get(String id) {
		return connectionMap.get(id);
	}
	
	public void remove(String connectionId) {
		
		connectionMap.remove(connectionId);
		logger.debug("Removed connection " + connectionId);
	}
}
