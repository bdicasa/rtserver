package rtserver.rpc.json;

public final class JsonRpcResult {
	
	public final Object result;
	public final String id;
	
	public JsonRpcResult(Object result, String id) {
		this.result = result;
		this.id = id;
	}
}
