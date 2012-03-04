package rtserver.rpc.json;

import org.jboss.netty.channel.ChannelFuture;

import rtserver.Connection;
import rtserver.internal.Json;
import rtserver.internal.LongPollingConnection;

/**
 * Writes JSON RPC response objects.
 * @author Brian DiCasa
 */
public final class JsonRpcResponseWriter {
	
	private final JsonRpcRequest request;
	private final Connection connection;
	
	/**
	 * Writes JSON RPC response objects.
	 * @param request The request associated with this response.
	 * @param connection The connection the response should be written to.
	 */
	public JsonRpcResponseWriter(JsonRpcRequest request, Connection connection) {
		
		this.request = request;
		this.connection = connection;
	}
	
	/**
	 * Writes the specified JSON RPC result to the client.
	 * @param result The object to write as a result.
	 * @return The ChannelFuture associated with this write. Note that this value
	 * may be null if you are writing to browsers that require a long polling
	 * strategy.
	 */
	public ChannelFuture writeResult(Object result) {
		
		JsonRpcResult jsonResult = new JsonRpcResult(result, request.id);
		String json = Json.stringify(jsonResult);
		return connection.write(json);
	}
	
	/**
	 * Writes the specified JSON RPC error to the client.
	 * @param code A code for the error.
	 * @param message The error message.
	 * @return The ChannelFuture associated with this write. Note that this value
	 * may be null if you are writing to browsers that require a long polling
	 * strategy.
	 */
	public ChannelFuture writeError(Integer code, String message) {
		
		JsonRpcError jsonError = new JsonRpcError(code, message, request.id);
		String json = Json.stringify(jsonError);
		return connection.write(json);
	}
	
	/**
	 * Writes the specified JSON RPC error to the client.
	 * @param message The error message.
	 * @return The ChannelFuture associated with this write. Note that this value
	 * may be null if you are writing to browsers that require a long polling
	 * strategy.
	 */
	public ChannelFuture writeError(String message) {
		
		JsonRpcError jsonError = new JsonRpcError(message, request.id);
		String json = Json.stringify(jsonError);
		return connection.write(json);
	}
	
	/**
	 * Writes an empty response to the client. This method should always be used when
	 * you don't want to write a response. It is required to support HTTP connections,
	 * which always expect a response. If this method is not used when you have no response
	 * for a client, HTTP requests will be held open on the client.
	 */
	public void writeEmptyResponse() {
		
		if (connection instanceof LongPollingConnection) {
			((LongPollingConnection)connection).write();
		}
	}
}
