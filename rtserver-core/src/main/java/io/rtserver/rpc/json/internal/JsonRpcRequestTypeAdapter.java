package io.rtserver.rpc.json.internal;

import io.rtserver.rpc.json.JsonRpcRequest;

import java.io.IOException;
import com.google.common.collect.ImmutableMap;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public final class JsonRpcRequestTypeAdapter extends ImmutableCollectionTypeAdapter<JsonRpcRequest> {

	@Override
	public void write(JsonWriter out, JsonRpcRequest request) throws IOException {
		
	}

	@Override
	public JsonRpcRequest read(JsonReader reader) throws IOException {
		
		String method = null;
		String id = null;
		ImmutableMap<String, Object> params = null;
		
		reader.beginObject();
		while(reader.hasNext()) {
			String name = reader.nextName();
			if (name.equals("m")) {
				method = reader.nextString();
			} else if (name.equals("i")) {
				id = reader.nextString();
			} else if (name.equals("p")) {
				params = readObjectToImmMap(reader);
			}
		}
		reader.endObject();
		
		return new JsonRpcRequest(method, params, id);
	}
}
