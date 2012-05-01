package io.rtserver.sockets;

//import java.util.concurrent.ConcurrentHashMap;
import io.rtserver.concurrent.Callback;

import java.util.Collection;
import java.util.List;
//import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;

import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.util.concurrent.FutureListener;
import org.slf4j.*;

import com.google.common.collect.ImmutableList;

public class DefaultSocketCollection implements SocketCollection {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSocketCollection.class);
	
	//private Map<String, Socket> socketMap = null;
	
	private final Cache<String, Socket> socketCache;
	
	public DefaultSocketCollection() {
		
		//socketMap = new ConcurrentHashMap<String, Socket>(100);
		socketCache = new DefaultCacheManager().getCache();
	}
	
	/**
	 * Adds a connection with a new random id.
	 * @param context The channel handler context associated with this connection. 
	 * @param connectionType The type of connection to add.
	 * @return The added connection.
	 */
	public void add(Socket socket) {
		
		//socketMap.put(socket.getId(), socket);
		socketCache.put(socket.getId(), socket);
	}
	
	/**
	 * Creates a random connection id. A random connection id is just a UUID with the dashes removed.
	 * @return The connection id.
	 */
	public String createRandomId() {
		
		return UUID.randomUUID().toString().replaceAll("-", "");
	}
	
	public Collection<String> getAllIds() {
		
		//return socketMap.keySet();
		return socketCache.keySet();
	}
	
	public Collection<Socket> getAll() {
		
		//return socketMap.values();
		return socketCache.values();
	}
	
	public Socket get(String id) {
		
		//return socketMap.get(id);
		return socketCache.get(id);
	}
	
	public void getAsync(String id, final Callback<Socket> callback) {
		
		socketCache.getAsync(id).attachListener(new FutureListener<Socket>() {

			public void futureDone(Future<Socket> socketFuture) {
				Socket socket = null; Throwable t = null;
				try {
					socket = socketFuture.get();
				} catch(Throwable ex) {
					t = ex;
				}
				
				callback.execute(socket, t);
			}
		});
	}
	
	public Collection<Socket> get(Collection<String> ids) {
		
		ImmutableList.Builder<Socket> builder = new ImmutableList.Builder<Socket>();
		
		for (String id : ids) {
			//builder.add(socketMap.get(id));
			builder.add(socketCache.get(id));
		}
		
		return builder.build();
	}
	
	public void getAsync(List<String> ids, final Callback<Collection<Socket>> callback) {
		
		String[] idArr = ids.toArray(new String[ids.size()]);
		int index = 0;
		ImmutableList.Builder<Socket> sockets = new ImmutableList.Builder<Socket>();
		retrieveSockets(idArr, sockets, index, callback);
		
	}
	
	public void remove(String id) {
		
		//socketMap.remove(connectionId);
		socketCache.remove(id);
		LOGGER.debug("Removed connection " + id);
	}
	
	private void retrieveSockets(final String[] ids, final ImmutableList.Builder<Socket> sockets,
		final int index, final Callback<Collection<Socket>> callback) {
		
		if (ids.length == index) {
			callback.execute(sockets.build(), null);
			return;
		}
		
		String id = ids[index];
		
		socketCache.getAsync(id).attachListener(new FutureListener<Socket>() {

			public void futureDone(Future<Socket> future) {
				
				Socket socket = null;
				try {
					socket = future.get();
				} catch (Throwable t) {
					callback.execute(null, t);
				}
				
				if (socket != null) sockets.add(socket);
				retrieveSockets(ids, sockets, index + 1, callback);
			}
			
		});
	}
}
