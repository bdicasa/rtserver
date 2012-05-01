package io.rtserver.rpc.json;

import io.rtserver.json.Json;
import io.rtserver.sockets.LongPollingSocket;
import io.rtserver.sockets.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Writes JSON RPC response objects.
 * @author Brian DiCasa
 */
public final class DefaultJsonRpcResponseWriter implements JsonRpcResponseWriter {
	
	private final JsonRpcRequest request;
	private final Socket connection;
	private boolean writtenTo;
	private Logger logger = LoggerFactory.getLogger(DefaultJsonRpcResponseWriter.class);
	
	/**
	 * Writes JSON RPC response objects.
	 * @param request The request associated with this response.
	 * @param connection The connection the response should be written to.
	 */
	public DefaultJsonRpcResponseWriter(JsonRpcRequest request, Socket connection) {
		
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
	public void writeResult(Object result) {
		
		if (checkWrittenTo()) return;
		
		JsonRpcResult jsonResult = new JsonRpcResult(result, request.id);
		String json = Json.stringify(jsonResult);
		connection.write(json);
	}
	
	/**
	 * Writes the specified JSON RPC error to the client.
	 * @param code A code for the error.
	 * @param message The error message.
	 * @return The ChannelFuture associated with this write. Note that this value
	 * may be null if you are writing to browsers that require a long polling
	 * strategy.
	 */
	public void writeError(Integer code, String message) {
		
		if (checkWrittenTo()) return;
		
		JsonRpcError jsonError = new JsonRpcError(code, message, request.id);
		String json = Json.stringify(jsonError);
		connection.write(json);
	}
	
	/**
	 * Writes the specified JSON RPC error to the client.
	 * @param message The error message.
	 * @return The ChannelFuture associated with this write. Note that this value
	 * may be null if you are writing to browsers that require a long polling
	 * strategy.
	 */
	public void writeError(String message) {
		
		if (checkWrittenTo()) return;
		
		JsonRpcError jsonError = new JsonRpcError(message, request.id);
		String json = Json.stringify(jsonError);
		connection.write(json);
	}
	
	/**
	 * Writes an empty response to the client. This method should always be used when
	 * you don't want to write a response. It is required to support HTTP connections,
	 * which always expect a response. If this method is not used when you have no response
	 * for a client, HTTP requests will be held open on the client.
	 */
	public void writeEmptyResponse() {
		
		if (checkWrittenTo()) return;
		
		if (connection instanceof LongPollingSocket) {
			((LongPollingSocket)connection).write();
		}
	}
	
	public boolean getWrittenTo() {
		
		return writtenTo;
	}
	private boolean checkWrittenTo() {
		
		if (writtenTo) {
			logger.warn("Trying to respond to a request more than once. " +
				"Responses written after the first are ignored.");
		}
		
		return writtenTo;
	}
}
