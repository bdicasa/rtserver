package rtserver;

import org.jboss.netty.channel.ChannelFuture;
import rtserver.internal.LongPollingConnection;


public class Response {
	
	public final Request request;
	public final Connection connection;
	
	public Response(Connection connection, Request request) {
		
		this.connection = connection;
		this.request = request;
	}
	
	public ChannelFuture write(String data) {
		
		return connection.write(data);
	}
	
	public ChannelFuture write(byte[] data) {
		
		return connection.write(data);
	}
	
	/**
	 * In order to support long polling every request needs a response. If you don't want to send
	 * a response with a request, use this method. If the client is connected using WebSockets or
	 * a TCP socket, nothing will be written to the client. If the client is connected using long
	 * polling, an empty response with the header X-Empty-Response: true will be written to the client.
	 * It is very important to use this method when you don't want to respond to a request as HTTP
	 * requests for long polling will remain open if you don't.
	 * @return Null for TcpSocket/WebSocket connections, the ChannelFuture associated with the write for
	 * long polling connections.
	 */
	public ChannelFuture write() {
		
		if (this.connection instanceof LongPollingConnection) {
			return ((LongPollingConnection)this.connection).write();
		}
		
		return null;
	}
}
