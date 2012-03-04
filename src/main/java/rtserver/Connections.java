package rtserver;

import java.util.Map;
import java.util.Set;

import org.jboss.netty.channel.ChannelHandlerContext;

import rtserver.internal.connections.ConnectionType;

public interface Connections {
	
	public Connection add(ChannelHandlerContext context, ConnectionType connectionType);
	
	public Connection add(ChannelHandlerContext context, ConnectionType connectionType, String id);
	
	public String createRandomId();
	
	public Map<String, Connection> getAll();
	
	public Set<String> getAllIds();
	
	public Connection get(String id);
	
	public void remove(String connectionId);
}
