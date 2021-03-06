package io.rtserver.rpc.json.internal;

import io.rtserver.SocketMessageHandler;
import io.rtserver.Request;
import io.rtserver.Response;
import io.rtserver.json.Json;
import io.rtserver.reflection.ClassFinder;
import io.rtserver.rpc.json.DefaultJsonRpcResponseWriter;
import io.rtserver.rpc.json.JsonRpcError;
import io.rtserver.rpc.json.JsonRpcMethod;
import io.rtserver.rpc.json.JsonRpcMethodHandler;
import io.rtserver.rpc.json.JsonRpcRequest;
import io.rtserver.rpc.json.JsonRpcResponseWriter;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;


/**
 * A message handler for JSON RPC.
 * @author Brian DiCasa
 */
public class JsonRpcMessageHandler extends SocketMessageHandler {
	
	private static final String JSON_RPC_METHOD_HANDLER_PACKAGE = "jsonRpcMethodHandlerPackage";
	
	private final ImmutableMap<String, JsonRpcMethodHandler> jsonRpcMethods;
	private final JsonRpcRequestParser requestParser;
	
	/**
	 * Creates an instance of a message handler for JSON RPC.
	 * @param serverConfig The configuration specified for the server.
	 */
	public JsonRpcMessageHandler(Map<String, Object> serverConfig) {
		
		super(serverConfig);
		
		String packageName = null;
		
		if (serverConfig.containsKey(JSON_RPC_METHOD_HANDLER_PACKAGE)) {
			packageName = (String)serverConfig.get(JSON_RPC_METHOD_HANDLER_PACKAGE);
		}
		
		if (packageName == null) {
			System.err.print("When using the JsonRpcMessageHandler you must specify the " +
				"config option " + JSON_RPC_METHOD_HANDLER_PACKAGE + ". It should specify " +
				"the base package that contains all your JsonRpcMethodHandlers.");
			System.exit(0);
		}
		this.jsonRpcMethods = createJsonRpcMethodsMap(packageName);
		this.requestParser = new JsonRpcRequestParser(this.jsonRpcMethods);
	}
	
	@Override
	public void messageReceived(Request request, Response response) {
		
		// TO DO: Should be able to specify encoding somehow, maybe in config?
		
		String jsonMessage;
		try {
			jsonMessage = new String(request.content, "UTF-8");
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException(ex);
		}
		
		Object result = requestParser.parse(jsonMessage);
		
		if (result instanceof JsonRpcError) {
			String json = Json.stringify(result);
			response.write(json);
		} else {
			JsonRpcRequest jsonRpcRequest = (JsonRpcRequest)result;
			JsonRpcMethodHandler methodHandler = jsonRpcMethods.get(jsonRpcRequest.method);
			JsonRpcResponseWriter writer = new DefaultJsonRpcResponseWriter(jsonRpcRequest, request.socket);
			methodHandler.execute(request.socket, jsonRpcRequest, writer);
		}
	}
	
	private ImmutableMap<String, JsonRpcMethodHandler> createJsonRpcMethodsMap(String packageName) {
		
		ImmutableMap.Builder<String, JsonRpcMethodHandler> builder =
			new ImmutableMap.Builder<String, JsonRpcMethodHandler>();
		
		List<Class> classes = ClassFinder.find(packageName, JsonRpcMethod.class);
		
		for (Class c : classes) {
			JsonRpcMethod ann = (JsonRpcMethod)c.getAnnotation(JsonRpcMethod.class);
			
			Class superClass = c.getSuperclass();
			
			if (!superClass.equals(JsonRpcMethodHandler.class)) {
				System.err.print("The class " + c.getSimpleName() + " does not implement the " +
					"interface JsonRpcMethodHandler. All classes marked with the JsonRpcMethod " +
					"annotation must implement the JsonRpcMethodHandler interface.");
				System.exit(0);
			}
			
			String name = ann.value();
			JsonRpcMethodHandler handler = null;
			
			try {
				handler = (JsonRpcMethodHandler)c.newInstance();
			} catch (Exception ex) {
				System.err.print("Error creating an instance of the JsonRpcMethodHandler: " + c.getSimpleName());
				System.exit(0);
			}
					
			builder.put(name, handler);
			System.out.println("Added handler: " + c.getSimpleName());
		}
		
		return builder.build();
	}
}
