package io.rtserver.pubsub;

import io.rtserver.concurrent.Callback;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableList;

public interface PubSub {
	
	/**
	 * Subscribes the socket with the given id to the specified channel.
	 * @param channel The channel the socket is to be subscribed to.
	 * @param socketId The id of the socket to be subscribed.
	 */
	public void subscribe(String channel, String socketIdId);
	
	/**
	 * Subscribes the sockets with the given id's to the specified channel.
	 * @param channel The channel the socket is to be subscribed to.
	 * @param socketIds The id's of the sockets to be subscribed.
	 */
	public void subscribe(String channel, String ... socketIds);
	
	/**
	 * Subscribes the sockets with the given id's to the specified channel.
	 * @param channel The channel the socket is to be subscribed to.
	 * @param socketIds The id's of the sockets to be subscribed.
	 */
	public void subscribe(String channel, Collection<String> socketIds);

	/**
	 * Unsubscribe's the socket with the given id from the specified channel.
	 * @param channel The channel the socket is to be unsubscribed from.
	 * @param socketId The id of the socket to be unsubscribed.
	 */
	public void unsubscribe(String channel, String socketId);
	
	/**
	 * Unsubscribe's the sockets with the given id's from the specified channel.
	 * @param channel The channel the sockets are to be unsubscribed from.
	 * @param socketIds The id's of the sockets to be unsubscribed.
	 */
	public void unsubscribe(String channel, String ... socketIds);
	
	/**
	 * Unsubscribe's the sockets with the given id's from the specified channel.
	 * @param channel The channel the sockets are to be unsubscribed from.
	 * @param socketIds The id's of the sockets to be unsubscribed.
	 */
	public void unsubscribe(String channel, Collection<String> socketIds);
	
	/**
	 * Publishes the specified message to the specified channel.
	 * @param channel The channel to publish to.
	 * @param message The message to be published.
	 */
	public void publish(String channel, byte[] message);
	
	/**
	 * Publishes the specified message to the specified channel.
	 * @param channel The channel to publish to.
	 * @param message The message to be published.
	 */
	public void publish(String channel, String message);
	
	/**
	 * Retrieves an immutable list of sockets that are subscribed to a channel.
	 * @param channel The channel to retrieve sockets for.
	 */
	public ImmutableList<String> getSocketsSubscribedTo(String channel);
	
	/**
	 * Retrieves an immutable list of sockets that are subscribed to a channel asynchronously.
	 * @param channel The channel to retrieve sockets for.
	 * @param callback The callback to be executed when the socket ids have been retrieved.
	 */
	public void getSocketsSubscribedToAsync(String channel, Callback<ImmutableList<String>> callback);
	
	/**
	 * Retrieves an immutable list of channels that the socket is subscribed to.
	 * @param socketId The socket id to retrieve channels for.
	 */
	public ImmutableList<String> getChannelsSubscribedTo(String socketId);
	
	/**
	 * Retrieves an immutable list of channels that the socket is subscribed to asynchronously.
	 * @param socketId The socket id to retrieve channels for.
	 * @param callback The callback to be exwecuted when the channel's have been retrieved.
	 */
	public void getChannelsSubscribedToAsync(String socketId, Callback<ImmutableList<String>> callback);
	
	/**
	 * Unsubscribe's a socket from all channels the socket is subscribed to.
	 * @param socketId The id of the socket to unsubscribe.
	 */
	public void unsubscribeSocket(String socketId);
}
