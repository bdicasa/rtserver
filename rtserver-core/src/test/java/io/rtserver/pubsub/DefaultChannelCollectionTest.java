package io.rtserver.pubsub;

import static org.junit.Assert.*;

import io.rtserver.concurrent.Callback;
import io.rtserver.pubsub.ChannelCollection;
import io.rtserver.pubsub.DefaultChannelCollection;

import java.util.Collection;

import org.junit.*;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;


public class DefaultChannelCollectionTest {
	
	private static final String TEST_CHANNEL = "testChannel";
	
	private ChannelCollection channelCollection;
	
	@Before
	public void setUp() {
		channelCollection = new DefaultChannelCollection();
	}
	
	@Test
	public void addsCollectionForChannelWhenChannelDoesntExist() {
		
		channelCollection.add(TEST_CHANNEL, "testSocketId");
		
		assertNotNull(channelCollection.getSocketsSubscribedTo(TEST_CHANNEL));
		assertNotNull(channelCollection.getChannelsSubscribedTo("testSocketId"));
		assertTrue(channelCollection.getSocketsSubscribedTo(TEST_CHANNEL).size() == 1);
		assertTrue(channelCollection.getChannelsSubscribedTo("testSocketId").size() == 1);
		assertTrue(channelCollection.getSocketsSubscribedTo(TEST_CHANNEL).contains("testSocketId"));
		assertTrue(channelCollection.getChannelsSubscribedTo("testSocketId").contains(TEST_CHANNEL));
	}
	
	@Test
	public void addsSocketIdToExistingChannel() {
		
		channelCollection.add(TEST_CHANNEL, "socketId1");
		channelCollection.add(TEST_CHANNEL, "socketId2");
		
		// Ensure the collections exist
		assertNotNull(channelCollection.getSocketsSubscribedTo(TEST_CHANNEL));
		assertNotNull(channelCollection.getChannelsSubscribedTo("socketId1"));
		assertNotNull(channelCollection.getChannelsSubscribedTo("socketId2"));
		
		// Ensure collections are correct sizes
		assertTrue(channelCollection.getSocketsSubscribedTo(TEST_CHANNEL).size() == 2);
		assertTrue(channelCollection.getChannelsSubscribedTo("socketId1").size() == 1);
		assertTrue(channelCollection.getChannelsSubscribedTo("socketId2").size() == 1);
		
		// Ensure collections contain correct items
		assertTrue(channelCollection.getSocketsSubscribedTo(TEST_CHANNEL).contains("socketId1"));
		assertTrue(channelCollection.getSocketsSubscribedTo(TEST_CHANNEL).contains("socketId2"));
		assertTrue(channelCollection.getChannelsSubscribedTo("socketId1").contains(TEST_CHANNEL));
		assertTrue(channelCollection.getChannelsSubscribedTo("socketId2").contains(TEST_CHANNEL));
	}
	
	/**
	 * Ensures that a channel collection is removed when the last socket is removed,
	 * and that a socket collection is removed when the last channel is removed
	 * from the socket.
	 */
	@Test
	public void removesCollectionsWhenLastResoucesRemoved() {
		
		channelCollection.add(TEST_CHANNEL, "testSocketId");
		channelCollection.remove(TEST_CHANNEL, "testSocketId");
		
		channelCollection.getSocketsSubscribedTo(TEST_CHANNEL);
		
		assertNull(channelCollection.getSocketsSubscribedTo(TEST_CHANNEL));
		assertNull(channelCollection.getChannelsSubscribedTo("testSocketId"));
	}
	
	/**
	 * Ensures that when a socket is still subscribed to a channel, the
	 * channel collection still exists.
	 */
	@Test
	public void channelCollectionStillExistsWhenAtLeastOneSocketRemains() {
		
		channelCollection.add(TEST_CHANNEL, "socket1");
		channelCollection.add(TEST_CHANNEL, "socket2");
		channelCollection.remove(TEST_CHANNEL, "socket2");
		
		assertNotNull(channelCollection.getSocketsSubscribedTo(TEST_CHANNEL));
		assertTrue(channelCollection.getSocketsSubscribedTo(TEST_CHANNEL).size() == 1);
		assertTrue(channelCollection.getSocketsSubscribedTo(TEST_CHANNEL).contains("socket1"));
		assertFalse(channelCollection.getSocketsSubscribedTo(TEST_CHANNEL).contains("socket2"));
	}
	
	@Test
	public void socketCollectionStillExistsWhenAtLeastOneChannelRemains() {
		
		channelCollection.add("channel1", "testSocket");
		channelCollection.add("channel2", "testSocket");
		channelCollection.remove("channel1", "testSocket");
		
		Collection<String> channels = channelCollection.getChannelsSubscribedTo("testSocket");
		assertNotNull(channels);
		assertTrue(channels.size() == 1);
		assertTrue(channels.contains("channel2"));
	}
	
	@Test
	public void socketRemovedFromAllChannelsWhenSubscriptionsRemoved() {
		
		channelCollection.add("channel1", "testSocket");
		channelCollection.add("channel2", "testSocket");
		
		channelCollection.removeSubscriptionsFrom("testSocket");
		
		assertNull(channelCollection.getChannelsSubscribedTo("testSocket"));
		assertNull(channelCollection.getSocketsSubscribedTo("channel1"));
		assertNull(channelCollection.getSocketsSubscribedTo("channel2"));
	}
	
	@Test
	public void retrievesAppropriateSocketsWhenSocketsSubscribedToChannel() {
		
		channelCollection.add(TEST_CHANNEL, "socket1");
		channelCollection.add(TEST_CHANNEL, "socket2");
		channelCollection.add(TEST_CHANNEL, "socket3");
		
		channelCollection.getSocketsSubscribedToAsync(TEST_CHANNEL, new Callback<ImmutableList<String>>() {

			public void execute(ImmutableList<String> socketIds, Throwable t) {
				assertNotNull(socketIds);
				assertTrue(socketIds.size() == 3);
				assertNull(t);
			}
		});
	}
	
	@Test
	public void retrievesEmptyCollectionWhenNoSocketsSubscribedToChannel() {
		
		channelCollection.getSocketsSubscribedToAsync(TEST_CHANNEL, new Callback<ImmutableList<String>>() {

			public void execute(ImmutableList<String> socketIds, Throwable t) {
				assertNotNull(socketIds);
				assertTrue(socketIds.isEmpty());
				assertNull(t);
			}
		});
	}
	
	@Test
	public void retrievesAppropriateChannelsAsynchronouslyWhenSocketSubscribedToChannels() {
		
		channelCollection.add("channel1", "socket");
		channelCollection.add("channel2", "socket");
		channelCollection.add("channel3", "socket");
		
		channelCollection.getChannelsSubscribedToAsync("socket", new Callback<ImmutableList<String>>() {

			public void execute(ImmutableList<String> socketIds, Throwable t) {
				assertNotNull(socketIds);
				assertTrue(socketIds.size() == 3);
				assertNull(t);
			}
		});
	}
	
	@Test
	public void retrievesEmptyCollectionAsynchronouslyWhenSocketSubscribedToNoChannels() {
		
		channelCollection.getChannelsSubscribedToAsync("socket", new Callback<ImmutableList<String>>() {

			public void execute(ImmutableList<String> socketIds, Throwable t) {
				assertNotNull(socketIds);
				assertTrue(socketIds.isEmpty());
				assertNull(t);
			}
		});
	}
}
