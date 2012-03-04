package rst.connections;

import static org.junit.Assert.*;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.junit.Before;
import org.junit.Test;

import rtserver.DefaultConnectionsCollection;
import rtserver.internal.connections.ConnectionType;
import rtserver.internal.connections.ExistingConnectionIdException;

public class ConnectionsTest {
	
	@Test
	public void add_TwoConnectionsWithSameId_ExistingConnectionIdExceptionThrown() {
		
		DefaultConnectionsCollection connections = new DefaultConnectionsCollection();
		String id = connections.createRandomId();
		ChannelHandlerContext context = new StubChannelHandlerContext();
		connections.add(context, ConnectionType.WEB_SOCKET, id);
		
		try {
			connections.add(context, ConnectionType.WEB_SOCKET, id);
			// Tests fails if we don't go into catch clause
			fail("Expected error not thrown.");
		} catch(ExistingConnectionIdException ex) {
			// Expected exception
		}
		
	}
	
	private class StubChannelHandlerContext implements ChannelHandlerContext {

		public Channel getChannel() {
			// TODO Auto-generated method stub
			return null;
		}

		public ChannelPipeline getPipeline() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getName() {
			// TODO Auto-generated method stub
			return null;
		}

		public ChannelHandler getHandler() {
			// TODO Auto-generated method stub
			return null;
		}

		public boolean canHandleUpstream() {
			// TODO Auto-generated method stub
			return false;
		}

		public boolean canHandleDownstream() {
			// TODO Auto-generated method stub
			return false;
		}

		public void sendUpstream(ChannelEvent e) {
			// TODO Auto-generated method stub
			
		}

		public void sendDownstream(ChannelEvent e) {
			// TODO Auto-generated method stub
			
		}

		public Object getAttachment() {
			// TODO Auto-generated method stub
			return null;
		}

		public void setAttachment(Object attachment) {
			// TODO Auto-generated method stub
			
		}
		
	}
}
