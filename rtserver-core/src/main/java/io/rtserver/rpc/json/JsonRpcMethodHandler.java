package io.rtserver.rpc.json;

import io.rtserver.ServerBootstrap;
import io.rtserver.sockets.Socket;

public abstract class JsonRpcMethodHandler {
	
	/**
	 * Provides functionality for communicating with other connections on the server using JSON.
	 */
	protected final JsonCommunicator jsonComm;
	
	public JsonRpcMethodHandler() {
		
		this.jsonComm = new DefaultJsonCommunicator(ServerBootstrap.getCommunicator(), ServerBootstrap.getPubSub());
	}
	
	public JsonRpcMethodHandler(JsonCommunicator jsonCommunicator) {
		
		this.jsonComm = jsonCommunicator;
	}
	
	/**
	 * Override this abstract method to implement required functionality for this RPC method.
	 * @param connection The connection that called the RPC method.
	 * @param request The JSON RPC request sent.
	 * @param writer A response writer used to write a response to the request.
	 */
	public abstract void handle(Socket conn, JsonRpcRequest req, JsonRpcResponseWriter writer);
	
	/**
	 * Executes the given JSON RPC method. This method is used internally and should not be called.
	 */
	public void execute(Socket conn, JsonRpcRequest req, JsonRpcResponseWriter writer) {
		
		handle(conn, req, writer);
		
		// If the writer has not been written to, we are going to write an empty response.
		// This is to prevent requests from being left open when long polling is used.
		boolean writtenTo = ((DefaultJsonRpcResponseWriter)writer).getWrittenTo();
		if (!writtenTo) writer.writeEmptyResponse();
	}
}
