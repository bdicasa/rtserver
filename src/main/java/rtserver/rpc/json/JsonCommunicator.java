package rtserver.rpc.json;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import rtserver.Communicator;
import rtserver.internal.Json;

public class JsonCommunicator {
	
	private final Communicator communicator;
	
	public JsonCommunicator(Communicator communicator) {
		this.communicator = communicator;
	}
	
	/**
	 * Sends a JSON message to the connection with the specified id.
	 * @param messageName The name of the message to send.
	 * @param params Any data to send with the message.
	 * @param connectionId The id of the connection to send the message to.
	 */
	public void send(String messageName, Object params, String connectionId) {
		
		Map<String, Object> jsonMessage = createJsonMessage(messageName, params);
		this.communicator.send(Json.stringify(jsonMessage), connectionId);
	}
	
	/**
	 * Sends a JSON message to the connections with the specified id's.
	 * @param messageName The name of the message to send.
	 * @param params Any data to send with the message.
	 * @param connectionIds The id's of the connections to send the message to.
	 */
	public void send(String messageName, Object params, Set<String> connectionIds) {
		
		Map<String, Object> jsonMessage = createJsonMessage(messageName, params);
		this.communicator.send(Json.stringify(jsonMessage), connectionIds);
	}
	
	/**
	 * Sends a JSON message to all connected clients.
	 * @param messageName The name of the message to send.
	 * @param params Any data to send with the message.
	 */
	public void broadcast(String messageName, Object params) {
		
		Map<String, Object> jsonMessage = createJsonMessage(messageName, params);
		this.communicator.broadcast(Json.stringify(jsonMessage));
	}
	
	private Map<String, Object> createJsonMessage(String messageName, Object params) {
		
		Map<String, Object> jsonMessage = new HashMap<String, Object>();
		jsonMessage.put("messageName", messageName);
		jsonMessage.put("params", params);
		
		return jsonMessage;
	}
}
