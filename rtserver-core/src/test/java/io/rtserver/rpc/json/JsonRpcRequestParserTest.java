package io.rtserver.rpc.json;

import static org.junit.Assert.*;

import io.rtserver.json.Json;
import io.rtserver.rpc.json.JsonRpcMethodHandler;
import io.rtserver.rpc.json.JsonRpcRequest;
import io.rtserver.rpc.json.JsonRpcResponseWriter;
import io.rtserver.rpc.json.internal.JsonRpcRequestParser;
import io.rtserver.sockets.Socket;

import java.util.Map;

import org.junit.Test;


import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

public class JsonRpcRequestParserTest {
	
	private static final String VALID_JSON_RPC_REQUEST =
		"{\"m\":\"jsonRpcMethod\",\"p\":{\"param1\":10,\"param2\":\"hello\"},\"i\":\"1\"}";
	
	@Test
	public void parse_ValidRequest_JsonRpcRequestReturned() {
		
		JsonRpcRequestParser parser = getJsonRpcRequestParser();
		Object request = null;
		
		try {
			request = parser.parse(VALID_JSON_RPC_REQUEST);
		} catch (Throwable ex) {
			fail("Exception thrown: " + ex.getMessage());
		}
		
		if (!(request instanceof JsonRpcRequest)) {
			fail("JsonRpcRequest object not returned for valid JSON RPC request.");
		}
		
		JsonRpcRequest jsonReq = (JsonRpcRequest)request;
		
		assertEquals("jsonRpcMethod", jsonReq.method);
		assertEquals(10, jsonReq.params.get("param1"));
		assertEquals("hello", jsonReq.params.get("param2"));
	}
	
	private String getTestJsonRpcRequest(String method, ImmutableMap<String, Object> params) {
		
		JsonRpcRequest request = new JsonRpcRequest(method, params, "1");
		return Json.stringify(request);
	}
	
	private JsonRpcRequestParser getJsonRpcRequestParser() {
		
		ImmutableMap.Builder<String, JsonRpcMethodHandler> builder =
			new ImmutableMap.Builder<String, JsonRpcMethodHandler>();
		builder.put("jsonRpcMethod", new StubJsonRpcMethodHandler());
		return new JsonRpcRequestParser(builder.build());
	}
	
	private class StubJsonRpcMethodHandler extends JsonRpcMethodHandler {

		public void handle(Socket connection, JsonRpcRequest request, JsonRpcResponseWriter response) {
			
		}
	}
}
