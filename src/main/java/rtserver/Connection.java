package rtserver;

import org.jboss.netty.channel.ChannelFuture;


public interface Connection {
	
	/**
	 * Gets the id associated with this connection.
	 * @return The id associated with this connection.
	 */
	public String getId();
	
	/**
	 * Writes the specified data to the channel associated with this connection.
	 * @param data The data to write.
	 * @return The ChannelFuture associated with the write event.
	 */
	public abstract ChannelFuture write(String data);
	
	/**
	 * Writes the specified data to the channel associated with this connection.
	 * @param data The data to write.
	 * @return The ChannelFuture associated with the write event.
	 */
	public abstract ChannelFuture write(byte[] data);
}
