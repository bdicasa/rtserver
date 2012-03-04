package rtserver;

import java.util.Set;

/*
 * Allows easy communication with other connections on the server.
 */
public  class DefaultCommunicator implements Communicator {
	
	private final Connections connections;
	
	public DefaultCommunicator(Connections connections) {
		
		this.connections = connections;
	}
	
	/**
	 * Writes the given message to the connection with the given id.
	 * @param message The message to send.
	 * @param connectionId The id of the connection the message should be written to.
	 * @return A channel future for the write event.
	 */
	public void send(byte[] message, String connectionId) {
		
		this.connections.get(connectionId).write(message);
	}

	/**
	 * Writes the given message to the connection with the given id.
	 * @param message The message to send.
	 * @param connectionId The id of the connection the message should be written to.
	 * @return A channel future for the write event.
	 */
	public void send(String message, String connectionId) {
		
		this.connections.get(connectionId).write(message);
	}

	/**
	 * Writes the given message to the connections with the given id's.
	 * @param message The message to send.
	 * @param connectionId The id's of the connections the message should be written to.
	 * @return 
	 */
	public void send(byte[] message, Set<String> connectionIds) {
		
		for (String id : connectionIds) {
			
			Connection connection = this.connections.get(id);
			connection.write(message);
		}
	}
	
	/**
	 * Writes the given message to the connection with the given id.
	 * @param message The message to send.
	 * @param connectionId The id of the connection the message should be written to.
	 * @return 
	 */
	public void send(String message, Set<String> connectionIds) {
		
		for (String id : connectionIds) {
			
			Connection connection = this.connections.get(id);
			connection.write(message);
		}
	}
	
	/**
	 * Writes the given message to all connected clients.
	 * @param message The message to write.
	 */
	public void broadcast(String message) {
		
		for (Connection connection : connections.getAll().values()) {
			connection.write(message);
		}
	}
}
