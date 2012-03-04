package rtserver;

import java.util.Set;

public interface Communicator {
	
	public void send(byte[] message, String connectionId);
	
	public void send(String message, String connectionId);
	
	public void send(byte[] message, Set<String> connectionIds);
	
	public void send(String message, Set<String> connectionIds);
	
	public void broadcast(String message);
}
