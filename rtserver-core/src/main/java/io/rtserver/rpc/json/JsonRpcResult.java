package io.rtserver.rpc.json;

import com.google.gson.annotations.SerializedName;

public final class JsonRpcResult {
	
	@SerializedName("r")
	public final Object result;
	
	@SerializedName("i")
	public final String id;
	
	public JsonRpcResult(Object result, String id) {
		this.result = result;
		this.id = id;
	}
}
