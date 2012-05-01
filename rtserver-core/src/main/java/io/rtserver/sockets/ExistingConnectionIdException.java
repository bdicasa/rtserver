package io.rtserver.sockets;

public class ExistingConnectionIdException extends RuntimeException {
	
	private static final long serialVersionUID = 1165743994541426900L;

	public ExistingConnectionIdException(String connectionId) {
		
		super("Failed to add connection with id: " + connectionId +
			". It already exists in the connections collection.");
	}
}