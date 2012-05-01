package io.rtserver.netty.sockets;

public enum SocketType {
	
	/**
	 * A web socket.
	 */
	WEB,
	
	/**
	 * Socket uses a long polling connection strategy.
	 */
	LONG_POLLING
}
