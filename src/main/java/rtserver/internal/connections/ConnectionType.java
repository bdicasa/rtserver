package rtserver.internal.connections;

public enum ConnectionType {
	
	/**
	 * A raw TCP connection.
	 */
	TCP,
	
	/**
	 * A WebSocket connection.
	 */
	WEB_SOCKET,
	
	/**
	 * Connection uses a long polling connection strategy.
	 */
	LONG_POLLING
}
