package io.rtserver.rpc.json.internal;

import io.rtserver.json.Json;
import io.rtserver.rpc.json.JsonRpcError;
import io.rtserver.rpc.json.JsonRpcMethodHandler;
import io.rtserver.rpc.json.JsonRpcRequest;

import java.util.HashMap;
import java.util.Map;


import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Parses JSON RPC requests.
 * @author Brian DiCasa
 */
public final class JsonRpcRequestParser {
	
	private static final String CODE = "code";
	private static final String MESSAGE = "message";
	
	private static final int PARSE_ERROR_CODE = -100;
	private static final String PARSE_ERROR_MESSAGE =
		"Error parsing JSON request.";
	private static final int METHOD_NOT_FOUND_CODE = -200;
	private static final int METHOD_ID_NOT_SPECIFIED_CODE = -300;
	private static final String METHOD_ID_NOT_SPECIFIED_MESSAGE =
		"The JSON RPC request does not have an id specified.";
	
	private final ImmutableMap<String, JsonRpcMethodHandler> jsonRpcMethods;
	private final Gson gson;
	
	/**
	 * Parses JSON RPC requests.
	 * @param jsonRpcMethods A list of available JsonRpcMethodHandler's.
	 */
	public JsonRpcRequestParser(ImmutableMap<String, JsonRpcMethodHandler> jsonRpcMethods) {
		this.jsonRpcMethods = jsonRpcMethods;
		this.gson = new GsonBuilder()
			.registerTypeAdapter(JsonRpcRequest.class, new JsonRpcRequestTypeAdapter())
			.create();
	}
	
	/**
	 * Parses the given string into a JSON object. If the json is parsed successfully
	 * and is a valid request a JsonRpcRequest object will be returned. Otherwise,
	 * a JsonRpcError will be returned containing the appropriate error.
	 * @param json The JSON to parse into a JsonRpcRequest.
	 * @return A JsonRpcRequest object if a valid request, otherwise a JsonRpcError
	 * object containing the error response.
	 */
	public Object parse(String json) {
		
		JsonRpcRequest request;
		JsonRpcMethodHandler methodHandler;
		
		try {
			request = gson.fromJson(json, JsonRpcRequest.class);
		} catch (Exception ex) {
			return new JsonRpcError(PARSE_ERROR_CODE, PARSE_ERROR_MESSAGE, null);
		}
		
		if (!jsonRpcMethods.containsKey(request.method)) {
			return new JsonRpcError(METHOD_NOT_FOUND_CODE, getMethodNotFoundMessage(request.method), request.id);
		}
		
		if (request.id == null || request.id.isEmpty()) {
			return new JsonRpcError(METHOD_ID_NOT_SPECIFIED_CODE, METHOD_ID_NOT_SPECIFIED_MESSAGE, null);
		}
		
		return request;
	}
	
	private String getMethodNotFoundMessage(String method) {
		return "The method JSON RPC '" + method + "' does not exist.";
	}
}
