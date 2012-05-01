package io.rtserver.pubsub;

import io.rtserver.concurrent.Callback;

import java.util.Collection;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;

import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.util.concurrent.FutureListener;

import com.google.common.collect.ImmutableList;


public class DefaultChannelCollection implements ChannelCollection {
	
	// Associates channels with subscribed connections.
	private final Cache<String, Collection<String>> channelCache;
	//private final Map<String, Collection<String>> channelMap;
	
	// Associates sockets with the channels the socket is subscribed to.
	// This collection is necessary in order to determine what channels the 
	// socket needs to be unsubscribed from when the socket is disconnected.
	private final Cache<String, Collection<String>> socketCache;
	//private final Map<String, Collection<String>> connectionMap;
	
	public DefaultChannelCollection() {
		
		this.channelCache = new DefaultCacheManager().getCache();
		this.socketCache = new DefaultCacheManager().getCache();
		//this.channelMap = new ConcurrentHashMap<String, Collection<String>>();
		//this.connectionMap = new ConcurrentHashMap<String, Collection<String>>();
	}
	
	public void add(String channel, String socketId) {
		
		// Add the socket to the channel collection
		//if (this.channelMap.containsKey(channel)) {
		if (this.channelCache.containsKey(channel)) {	
			//Collection<String> socketList = channelMap.get(channel);
			Collection<String> socketList = channelCache.get(channel);
			if (!socketList.contains(socketId)) {
				socketList.add(socketId);
			}
			
		} else {
			
			Collection<String> socketList = new ConcurrentLinkedQueue<String>();
			socketList.add(socketId);
			//channelMap.put(channel, connectionList);
			channelCache.put(channel, socketList);
		}
		
		// Add the channel to the socket collection
		//if (this.connectionMap.containsKey(socketId)) {
		if (this.socketCache.containsKey(socketId)) {
			
			//Collection<String> channelList = connectionMap.get(socketId);
			Collection<String> channelList = socketCache.get(socketId);
			if (!channelList.contains(channel)) {
				channelList.add(channel);
			}
		} else {
			
			Collection<String> channelList = new ConcurrentLinkedQueue<String>();
			channelList.add(channel);
			//this.connectionMap.put(socketId, channelList);
			this.socketCache.put(socketId, channelList);
		}
	}

	public void add(String channel, String... socketIds) {
		
		for (String socketId : socketIds) {
			add(channel, socketId);
		}
	}
	
	public void add(String channel, Collection<String> socketIds) {
		
		for (String socketId : socketIds) {
			add(channel, socketId);
		}
	}
	
	public void remove(String channel, String socketId) {
		
		// Remove the socket from the channel collection
		//if (this.channelMap.containsKey(channel)) {
		if (this.channelCache.containsKey(channel)) {
			
			//Collection<String> connectionList = channelMap.get(channel);
			Collection<String> socketList = channelCache.get(channel);
			socketList.remove(socketId);
			if (socketList.isEmpty()) {
				//channelMap.remove(channel);
				channelCache.remove(channel);
			}
		}
		
		// Remove the channel from the socket collection
		//if (this.connectionMap.containsKey(socketId)) {
		if (this.socketCache.containsKey(socketId)) {
			
			//Collection<String> channelList = connectionMap.get(socketId);
			Collection<String> channelList = socketCache.get(socketId);
			channelList.remove(channel);
			if (channelList.isEmpty()) {
				//connectionMap.remove(socketId);
				socketCache.remove(socketId);
			}
		}
		
	}

	public void remove(String channel, String... socketIds) {
		
		for (String socketId : socketIds) {
			remove(channel, socketId);
		}
	}
	
	public void remove(String channel, Collection<String> socketIds) {
		
		for (String socketId : socketIds) {
			remove(channel, socketId);
		}
	}
	
	public ImmutableList<String> getSocketsSubscribedTo(String channel) {
		
		//Collection<String> connections = channelMap.get(channel);
		Collection<String> sockets = channelCache.get(channel);
		if (sockets == null) {
			return null;
		} else {
			//return ImmutableList.copyOf(channelMap.get(channel));
			return ImmutableList.copyOf(channelCache.get(channel));
		}
	}
	
	public void getSocketsSubscribedToAsync(String channel, final Callback<ImmutableList<String>> callback) {
		
		channelCache.getAsync(channel).attachListener(new FutureListener<Collection<String>>() {

			public void futureDone(Future<Collection<String>> future) {
				
				try {
					Collection<String> socketIds = future.get();
					ImmutableList<String> socketList;
					if (socketIds == null) {
						socketList = ImmutableList.of();
					} else {
						socketList = ImmutableList.copyOf(socketIds);
					}
					callback.execute(socketList, null);
				} catch (Throwable t) {
					callback.execute(null, t);
				}
			}
		});
	}
	
	public ImmutableList<String> getChannelsSubscribedTo(String socketId) {
		
		//Collection<String> channels = connectionMap.get(socketId);
		Collection<String> channels = socketCache.get(socketId);
		if (channels == null) {
			return null;
		} else {
			return ImmutableList.copyOf(channels);
		}
	}
	
	public void getChannelsSubscribedToAsync(String socketId, final Callback<ImmutableList<String>> callback) {
		
		socketCache.getAsync(socketId).attachListener(new FutureListener<Collection<String>>() {

			public void futureDone(Future<Collection<String>> future) {
				
				try {
					Collection<String> socketIds = future.get();
					ImmutableList<String> socketList;
					if (socketIds == null) {
						socketList = ImmutableList.of();
					} else {
						socketList = ImmutableList.copyOf(socketIds);
					}
					callback.execute(socketList, null);
				} catch (Throwable t) {
					callback.execute(null, t);
				}
			}
		});
	}
	
	public void removeSubscriptionsFrom(String socketId) {
		Collection<String> channels = getChannelsSubscribedTo(socketId);
		
		if (channels != null) {
			for (String channel : channels) {
				remove(channel, socketId);
			}
		}
	}
}
