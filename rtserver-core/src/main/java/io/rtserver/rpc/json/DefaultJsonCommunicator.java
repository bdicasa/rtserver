package io.rtserver.rpc.json;

import io.rtserver.Communicator;
import io.rtserver.json.Json;
import io.rtserver.pubsub.ChannelCollection;
import io.rtserver.pubsub.PubSub;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class DefaultJsonCommunicator implements JsonCommunicator {
	
	private final Communicator communicator;
	private final PubSub pubSub;
	
	public DefaultJsonCommunicator(Communicator communicator, PubSub pubSub) {
		
		this.communicator = communicator;
		this.pubSub = pubSub;
	}
	
	public void subscribe(String channel, String connectionId) {
		
		this.pubSub.subscribe(channel, connectionId);
	}
	
	public void subscribe(String channel, String ... connectionIds) {
		
		this.pubSub.subscribe(channel, connectionIds);
	}
	
	public void subscribe(String channel, Collection<String> connectionIds) {
		
		this.pubSub.subscribe(channel, connectionIds);
	}
	
	public void unsubscribe(String channel, String connectionId) {
		
		this.pubSub.unsubscribe(channel, connectionId);
	}
	
	public void unsubscribe(String channel, String ... connectionIds) {
		
		this.pubSub.unsubscribe(channel, connectionIds);
	}
	
	public void unsubscribe(String channel, Collection<String> connectionIds) {
		
		this.pubSub.unsubscribe(channel, connectionIds);
	}
	
	public void publish(String channel, Object data) {
		
		List<String> connectionIds = this.pubSub.getSocketsSubscribedTo(channel);
		String jsonMessage = Json.stringify(createJsonMessage(channel, data));
		this.communicator.send(jsonMessage, connectionIds);
	}
	
	public void send(String connectionId, Object data) {
		
		Map<String, Object> jsonMessage = createJsonMessage("connection", data);
		this.communicator.send(Json.stringify(jsonMessage), connectionId);
	}
	
	public void broadcast(Object data) {
		
		Map<String, Object> jsonMessage = createJsonMessage("broadcast", data);
		this.communicator.broadcast(Json.stringify(jsonMessage));
	}
	
	private Map<String, Object> createJsonMessage(String channel, Object data) {
		
		Map<String, Object> jsonMessage = new HashMap<String, Object>();
		jsonMessage.put("c", channel);
		jsonMessage.put("d", data);
		
		return jsonMessage;
	}
}
