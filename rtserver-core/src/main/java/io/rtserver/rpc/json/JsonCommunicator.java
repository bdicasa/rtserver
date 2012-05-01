package io.rtserver.rpc.json;

import io.rtserver.Communicator;
import io.rtserver.json.Json;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public interface JsonCommunicator {
	
	/**
	 * Subscribes the connection with the given id to the specified channel.
	 * @param channel The channel the connection is to be subscribed to.
	 * @param connectionId The id of the connection to be subscribed.
	 */
	public void subscribe(String channel, String connectionId);
	
	/**
	 * Subscribes the connections with the given id's to the specified channel.
	 * @param channel The channel the connection is to be subscribed to.
	 * @param connectionIds The id's of the connections to be subscribed.
	 */
	public void subscribe(String channel, String ... connectionIds);
	
	/**
	 * Subscribes the connections with the given id's to the specified channel.
	 * @param channel The channel the connection is to be subscribed to.
	 * @param connectionIds The id's of the connections to be subscribed.
	 */
	public void subscribe(String channel, Collection<String> connectionIds);

	/**
	 * Unsubscribe's the connection with the given id from the specified channel.
	 * @param channel The channel the connection is to be unsubscribed from.
	 * @param connectionId The id of the connection to be unsubscribed.
	 */
	public void unsubscribe(String channel, String connectionId);
	
	/**
	 * Unsubscribe's the connections with the given id's from the specified channel.
	 * @param channel The channel the connections are to be unsubscribed from.
	 * @param connectionIds The id's of the connections to be unsubscribed.
	 */
	public void unsubscribe(String channel, String ... connectionIds);
	
	/**
	 * Unsubscribe's the connections with the given id's from the specified channel.
	 * @param channel The channel the connections are to be unsubscribed from.
	 * @param connectionIds The id's of the connections to be unsubscribed.
	 */
	public void unsubscribe(String channel, Collection<String> connectionIds);
	
	/**
	 * Publishes a JSON message to everyone subscribed to the specified channel.
	 * @param channel The channel to publish the message to.
	 * @param data The data to publish the the channel.
	 */
	public void publish(String channel, Object data);
	
	/**
	 * Sends a JSON message to the connection with the specified id.
	 * The channel named used is connection.
	 * @param messageName The name of the message to send.
	 * @param params Any data to send with the message.
	 * @param connectionId The id of the connection to send the message to.
	 */
	public void send(String connectionId, Object data);
	
	/**
	 * Sends a JSON message to all connected clients. The channel name used is broadcast.
	 * @param messageName The name of the message to send.
	 * @param params Any data to send with the message.
	 */
	public void broadcast(Object data);
}
