package rtserver.rpc.json;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

/**
 * JSON RPC request received from a client.
 * @author Brian DiCasa
 */
public final class JsonRpcRequest {
	
	/**
	 * The JSON RPC request method name.
	 */
	public final String method;
	
	/**
	 * The parameters sent with the JSON RPC request.
	 */
	public final ImmutableMap<String, Object> params;
	
	/**
	 * The JSON RPC request id.
	 */
	public final String id;
	
	/**
	 * Creates a JSON RPC request.
	 * @param method The JSON RPC request method name.
	 * @param params The parameters for the request.
	 * @param id The JSON RPC request id.
	 */
	public JsonRpcRequest(String method, ImmutableMap<String, Object> params, String id) {
		
		this.method = method;
		this.params = params;
		this.id = id;
	}
}
