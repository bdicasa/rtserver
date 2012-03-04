package rtserver.rpc.json.internal;

public final class JsonRpcErrorChunk {
	
	public final Integer code;
	public final String message;
	
	public JsonRpcErrorChunk(Integer code, String message) {
		
		this.code = code;
		this.message = message;
	}
	
	public JsonRpcErrorChunk(String message) {
		this.code = null;
		this.message = message;
	}
}
