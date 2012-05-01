package io.rtserver.pubsub;

import io.rtserver.Communicator;
import io.rtserver.concurrent.Callback;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;


public class DefaultPubSub implements PubSub {
	
	private final Communicator communicator;
	private final ChannelCollection channelCollection;
	
	public DefaultPubSub(Communicator communicator, ChannelCollection channelCollection) {
		
		this.communicator = communicator;
		this.channelCollection = channelCollection;
	}
	
	public void subscribe(String channel, String connectionId) {
		
		channelCollection.add(channel, connectionId);
	}
	
	public void subscribe(String channel, String ... connectionIds) {
		
		channelCollection.add(channel, connectionIds);
	}
	
	public void subscribe(String channel, Collection<String> connectionIds) {
		channelCollection.add(channel, connectionIds);
	}
	
	public void unsubscribe(String channel, String connectionId) {
		
		channelCollection.remove(channel, connectionId);
	}
	
	public void unsubscribe(String channel, String ... connectionIds) {
		
		channelCollection.remove(channel, connectionIds);
	}
	
	public void unsubscribe(String channel, Collection<String> connectionIds) {
		
		channelCollection.remove(channel, connectionIds);
	}
	
	public void publish(String channel, final String message) {
		
		channelCollection.getSocketsSubscribedToAsync(channel, new Callback<ImmutableList<String>>() {

			public void execute(ImmutableList<String> socketIds, Throwable t) {
				communicator.send(message, socketIds);
			}
		});
	}
	
	public void publish(String channel, final byte[] message) {
		
		channelCollection.getSocketsSubscribedToAsync(channel, new Callback<ImmutableList<String>>() {

			public void execute(ImmutableList<String> socketIds, Throwable t) {
				communicator.send(message, socketIds);
			}
		});
	}
	
	public ImmutableList<String> getSocketsSubscribedTo(String channel) {
		
		return channelCollection.getSocketsSubscribedTo(channel);
	}
	
	public void getSocketsSubscribedToAsync(String channel, Callback<ImmutableList<String>> callback) {
		
		channelCollection.getSocketsSubscribedToAsync(channel, callback);
	}
	
	public ImmutableList<String> getChannelsSubscribedTo(String connectionId) {
		
		return channelCollection.getChannelsSubscribedTo(connectionId);
	}
	
	public void getChannelsSubscribedToAsync(String socketId, Callback<ImmutableList<String>> callback) {
		
		channelCollection.getChannelsSubscribedToAsync(socketId, callback);
	}
	
	public void unsubscribeSocket(String connectionId) {
		
		channelCollection.removeSubscriptionsFrom(connectionId);
	}
}
