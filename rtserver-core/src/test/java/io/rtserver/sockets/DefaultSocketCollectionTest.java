package io.rtserver.sockets;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.rtserver.concurrent.Callback;
import io.rtserver.sockets.DefaultSocketCollection;
import io.rtserver.sockets.Socket;
import io.rtserver.sockets.SocketCollection;

import org.junit.*;

public class DefaultSocketCollectionTest {
	
	private SocketCollection socketCollection;
	
	@Before
	public void setUp() {
		socketCollection = new DefaultSocketCollection();
	}
	
	@Test
	public void retrievesSocketAsynchronouslyWhenSocketExistsInCollection() {
		
		Socket socket = new StubSocket("testSocketId");
		socketCollection.add(socket);
		
		socketCollection.getAsync("testSocketId", new Callback<Socket>() {
			
			public void execute(Socket socket, Throwable t) {
				assertNotNull(socket);
				assertEquals("testSocketId", socket.getId());
			}
		});
	}
	
	@Test
	public void retrievesNullWhenSocketDoesNotExistInCollection() {
		
		socketCollection.getAsync("testSocketId", new Callback<Socket>() {
			
			public void execute(Socket socket, Throwable t) {
				assertNull(socket);
				assertNull(t);
			}
		});
	}
	
	@Test
	public void retrievesSocketsAsynchronouslyWhenSocketsExistInCollection() {
		
		Socket socket1 = new StubSocket("socket1");
		socketCollection.add(socket1);
		
		Socket socket2 = new StubSocket("socket2");
		socketCollection.add(socket2);
		
		Socket socket3 = new StubSocket("socket3");
		socketCollection.add(socket3);
		
		List<String> socketIds = new ArrayList<String>();
		socketIds.add("socket1"); socketIds.add("socket2"); socketIds.add("socket3");
		
		socketCollection.getAsync(socketIds, new Callback<Collection<Socket>>() {

			public void execute(Collection<Socket> sockets, Throwable t) {
				assertNotNull(sockets);
				assertTrue(sockets.size() == 3);
				assertNull(t);
			}
		});
	}
	
	@Test
	public void retrievesEmptyListWhenSocketsDoNotExistInCollection() {
		
		List<String> socketIds = new ArrayList<String>();
		socketIds.add("socket1"); socketIds.add("socket2"); socketIds.add("socket3");
		
		socketCollection.getAsync(socketIds, new Callback<Collection<Socket>>() {

			public void execute(Collection<Socket> sockets, Throwable t) {
				assertNotNull(sockets);
				assertTrue(sockets.isEmpty());
				assertNull(t);
			}
		});
	}
	
	private class StubSocket implements Socket {
		
		private final String id;
		
		public StubSocket(String id) {
			this.id = id;
		}
		
		public String getId() {
			
			return id;
		}

		public void write(String data) {
			
		}

		public void write(byte[] data) {
			
		}
	}
}
