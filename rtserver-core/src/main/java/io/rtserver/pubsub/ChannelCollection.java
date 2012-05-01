package io.rtserver.pubsub;

import io.rtserver.concurrent.Callback;

import java.util.Collection;

import com.google.common.collect.ImmutableList;

public interface ChannelCollection {
	
	/**
	 * Adds a socket to a channel.
	 * @param channel The channel the socket should be added to.
	 * @param socketId The id of the socket to add.
	 */
	public void add(String channel, String socketId);
	
	/**
	 * Adds the sockets to the channel.
	 * @param channel The channel the sockets should be added to.
	 * @param socketIds The id's of the sockets to be added.
	 */
	public void add(String channel, String ... socketIds);
	
	/**
	 * Adds the sockets to the channel.
	 * @param channel The channel the sockets should be added to.
	 * @param socketIds The id's of the sockets to be added.
	 */
	public void add(String channel, Collection<String> socketIds);
	
	/**
	 * Removes the socket from the channel.
	 * @param channel The channel the socket should be removed from.
	 * @param socketId The id of the socket to be removed.
	 */
	public void remove(String channel, String socketId);
	
	/**
	 * Removes the sockets from the channel.
	 * @param channel The channel the sockets should be removed from.
	 * @param socketIds The id's of the sockets to be removed.
	 */
	public void remove(String channel, String ... socketIds);
	
	/**
	 * Removes the sockets from the channel.
	 * @param channel The channel the sockets should be removed from.
	 * @param socketIds The id's of the sockets to be removed.
	 */
	public void remove(String channel, Collection<String> socketIds);
	
	/**
	 * Retrieves an immutable collection of socket id's that are subscribed to a channel.
	 * @param channel The channel to retrieve sockets for.
	 */
	public ImmutableList<String> getSocketsSubscribedTo(String channel);
	
	/**
	 * Retrieves an immutable collection of socket id's that subscribed to a channel asynchronously.
	 * @param channel The channel to retrieve sockets for.
	 * @param callback The callback to be executed once the socket id's are retrieved.
	 */
	public void getSocketsSubscribedToAsync(String channel, Callback<ImmutableList<String>> callback);
	
	/**
	 * Retrieves an immutable collection of channels that the socket is subscribed to.
	 * @param socketId The id of the socket to retrieve channels for.
	 */
	public ImmutableList<String> getChannelsSubscribedTo(String socketId);
	
	/**
	 * Retrieves an immutable collection of channels that the socket is subscribed to asynchronously.
	 * @param socketId The id of the socket to retrieve channels for.
	 * @param callback The callback to be executed once the channel id's are retrieved.
	 */
	public void getChannelsSubscribedToAsync(String socketId, Callback<ImmutableList<String>> callback);
	
	/**
	 * Removes a socket from all channels it is subscribed to.
	 * @param socketId The id of the socket to remove subscriptions from.
	 */
	public void removeSubscriptionsFrom(String socketId);
}
