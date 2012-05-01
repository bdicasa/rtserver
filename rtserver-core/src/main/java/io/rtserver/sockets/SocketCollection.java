package io.rtserver.sockets;

import io.rtserver.concurrent.Callback;

import java.util.Collection;
import java.util.List;

public interface SocketCollection {
	
	public void add(Socket socket);
	
	public String createRandomId();
	
	public Collection<String> getAllIds();
	
	public Collection<Socket> getAll();
	
	public Socket get(String id);
	
	public void getAsync(String id, Callback<Socket> callback);
	
	public Collection<Socket> get(Collection<String> ids);
	
	public void getAsync(List<String> ids, Callback<Collection<Socket>> callback);
	
	public void remove(String connectionId);
}
