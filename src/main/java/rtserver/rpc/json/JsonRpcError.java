package rtserver.rpc.json;

import rtserver.rpc.json.internal.JsonRpcErrorChunk;

public final class JsonRpcError {
	
	public final JsonRpcErrorChunk error;
	public final String id;
	
	public JsonRpcError(Integer code, String message, String id) {
		this.error = new JsonRpcErrorChunk(code, message);
		this.id = id;
	}
	
	public JsonRpcError(String message, String id) {
		this.error = new JsonRpcErrorChunk(message);
		this.id = id;
	}
}
