package io.rtserver;

import io.rtserver.concurrent.Callback;
import io.rtserver.sockets.Socket;
import io.rtserver.sockets.SocketCollection;

import java.util.Collection;
import java.util.List;

/*
 * Allows easy communication with other sockets on the server.
 */
public class DefaultCommunicator implements Communicator {
	
	private final SocketCollection socketCollection;
	
	public DefaultCommunicator(SocketCollection sockets) {
		
		this.socketCollection = sockets;
	}
	
	public void send(final byte[] message, String socketId) {
		
		this.socketCollection.getAsync(socketId, new Callback<Socket>() {

			public void execute(Socket socket, Throwable t) {
				write(socket, message);
			}
		});
	}
	
	public void send(final String message, String socketId) {
		
		this.socketCollection.getAsync(socketId, new Callback<Socket>() {

			public void execute(Socket socket, Throwable t) {
				write(socket, message);
			}
			
		});
	}

	public void send(final byte[] message, List<String> socketIds) {
		
		this.socketCollection.getAsync(socketIds, new Callback<Collection<Socket>>() {

			public void execute(Collection<Socket> sockets, Throwable t) {
				for (Socket socket : sockets) {
					write(socket, message);
				}
			}
		});
	}
	
	public void send(final String message, List<String> socketIds) {
		
		this.socketCollection.getAsync(socketIds, new Callback<Collection<Socket>>() {

			public void execute(Collection<Socket> sockets, Throwable t) {
				for (Socket socket : sockets) {
					write(socket, message);
				}
			}
		});
	}
	
	public void broadcast(String message) {
		
		for (Socket socket : socketCollection.getAll()) {
			write(socket, message);
		}
	}
	
	public Socket getSocket(String socketId) {
		
		return socketCollection.get(socketId);
	}
	
	public void getSocketAsync(String socketId, Callback<Socket> callback) {
		
		this.socketCollection.getAsync(socketId, callback);
	}
	
	public Collection<Socket> getSockets(Collection<String> socketIds) {
		
		return socketCollection.get(socketIds);
	}
	
	public void getSocketsAsync(List<String> socketIds, Callback<Collection<Socket>> callback) {
		
		this.socketCollection.getAsync(socketIds, callback);
	}
	
	private void write(Socket socket, String message) {
		
		if (socket != null) {
			socket.write(message);
		}
	}
	
	private void write(Socket socket, byte[] message) {
		
		if (socket != null) {
			socket.write(message);
		}
	}
}
