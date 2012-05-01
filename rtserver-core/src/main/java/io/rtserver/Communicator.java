package io.rtserver;

import io.rtserver.concurrent.Callback;
import io.rtserver.sockets.Socket;

import java.util.Collection;
import java.util.List;


public interface Communicator {
	
	/**
	 * Writes the given message to the socket with the given id.
	 * @param message The message to send.
	 * @param socketId The id of the socket the message should be written to.
	 */
	public void send(byte[] message, String socketId);
	
	/**
	 * Writes the given message to the socket with the given id.
	 * @param message The message to send.
	 * @param socketId The id of the socket the message should be written to. 
	 */
	public void send(String message, String socketId);
	
	/**
	 * Writes the given message to the sockets with the given id's.
	 * @param message The message to send.
	 * @param socketIds The id's of the sockets the message should be written to. 
	 */
	public void send(byte[] message, List<String> socketIds);
	
	/**
	 * Writes the given message to the sockets with the given id's.
	 * @param message The message to send.
	 * @param socketIds The id's of the sockets the message should be written to. 
	 */
	public void send(String message, List<String> socketIds);
	
	/**
	 * Writes the given message to all connected clients.
	 * @param message The message to write.
	 */
	public void broadcast(String message);
	
	/**
	 * Retrieves a socket object by it's id.
	 * @param socketId The id of the socket you want to retrieve.
	 * @return The socket with the given id or null if the socket does not exist.
	 */
	public Socket getSocket(String socketId);
	
	/**
	 * Retrieves a socket object by it's id asynchronously.
	 * @param socketId The id of the socket you want to retrieve.
	 * @param callback The callback to be executed once the socket has been retrieved.
	 */
	public void getSocketAsync(String socketId, Callback<Socket> callback);
	
	/**
	 * Retrieves the sockets associated with the given id's.
	 * @param socketIds The id's of the sockets you want to retrieve.
	 * @return A collection of socket objects that have the specified socket ids.
	 */
	public Collection<Socket> getSockets(Collection<String> socketIds);
	
	/**
	 * Retrieves the sockets associated with the given id's asynchronously.
	 * @param socketIds The id's of the sockets you want to retrieve.
	 * @param callback The callback to be executed once the sockets have been retrieved.
	 */
	public void getSocketsAsync(List<String> socketIds, Callback<Collection<Socket>> callback);
}
