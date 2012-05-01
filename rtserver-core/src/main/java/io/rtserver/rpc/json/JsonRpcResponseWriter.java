package io.rtserver.rpc.json;

/**
 * Writes JSON RPC response objects.
 * @author Brian DiCasa
 */
public interface JsonRpcResponseWriter {
	
	/**
	 * Writes the specified JSON RPC result to the client.
	 * @param result The object to write as a result.
	 * @return The ChannelFuture associated with this write. Note that this value
	 * may be null if you are writing to browsers that require a long polling
	 * strategy.
	 */
	public void writeResult(Object result);
	
	/**
	 * Writes the specified JSON RPC error to the client.
	 * @param code A code for the error.
	 * @param message The error message.
	 * @return The ChannelFuture associated with this write. Note that this value
	 * may be null if you are writing to browsers that require a long polling
	 * strategy.
	 */
	public void writeError(Integer code, String message);
	
	/**
	 * Writes the specified JSON RPC error to the client.
	 * @param message The error message.
	 * @return The ChannelFuture associated with this write. Note that this value
	 * may be null if you are writing to browsers that require a long polling
	 * strategy.
	 */
	public void writeError(String message);
	
	/**
	 * Writes an empty response to the client. This method should always be used when
	 * you don't want to write a response. It is required to support HTTP connections,
	 * which always expect a response. If this method is not used when you have no response
	 * for a client, HTTP requests will be held open on the client.
	 */
	public void writeEmptyResponse();
}
