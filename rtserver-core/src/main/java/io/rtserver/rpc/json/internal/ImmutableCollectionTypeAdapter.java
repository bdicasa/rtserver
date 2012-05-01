package io.rtserver.rpc.json.internal;

import java.io.IOException;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

public abstract class ImmutableCollectionTypeAdapter<T> extends TypeAdapter<T> {
	
	protected ImmutableMap<String, Object> readObjectToImmMap(JsonReader reader) throws IOException {
		
		ImmutableMap.Builder<String, Object> builder =
			new ImmutableMap.Builder<String, Object>();
		
		reader.beginObject();
		while (reader.hasNext()) {
			String name = reader.nextName();
			JsonToken token = reader.peek();
			if (token == JsonToken.BEGIN_OBJECT) {
				builder.put(name, readObjectToImmMap(reader));
			} else if (token == JsonToken.BEGIN_ARRAY) {
				builder.put(name, readArrayToImmList(reader));
			} else if (token == JsonToken.BOOLEAN) {
				builder.put(name, reader.nextBoolean());
			} else if (token == JsonToken.STRING || token == JsonToken.NUMBER) {
				builder.put(name, readStringOrNumber(reader));
			} else if (token == JsonToken.NULL) {
				builder.put(name, null);
			}
		}
		reader.endObject();
		
		return builder.build();
	}
	
	protected ImmutableList<Object> readArrayToImmList(JsonReader reader) throws IOException {
		
		ImmutableList.Builder<Object> builder = new ImmutableList.Builder<Object>();
		
		reader.beginArray();
		while (reader.hasNext()) {
			String name = reader.nextName();
			JsonToken token = reader.peek();
			if (token == JsonToken.BEGIN_OBJECT) {
				builder.add(readObjectToImmMap(reader));
			} else if (token == JsonToken.BEGIN_ARRAY) {
				builder.add(readArrayToImmList(reader));
			} else if (token == JsonToken.BOOLEAN) {
				builder.add(reader.nextBoolean());
			} else if (token == JsonToken.STRING || token == JsonToken.NUMBER) {
				builder.add(readStringOrNumber(reader));
			} else if (token == JsonToken.NULL) {
				builder.add(null);
			}
		}
		reader.endArray();
		
		return builder.build();
	}
	
	protected Object readStringOrNumber(JsonReader reader) throws IOException {
		
		String value = reader.nextString();
		
		if (value.length() == 0 || !startsWithNumber(value)) {
			return value;
		} else {
			if (value.contains(".")) {
				try {
					double d = Double.parseDouble(value);
					return d;
				} catch (NumberFormatException ex) {
					return value;
				}
			} else {
				
				try {
					int i = Integer.parseInt(value);
					return i;
				} catch (NumberFormatException ex) {
					try {
						long l = Long.parseLong(value);
						return l;
					} catch (NumberFormatException ex2) {
						return value;
					}
				}
			}
		}
	}
	
	private boolean startsWithNumber(String value) {
		
		char c = value.charAt(0);
		if (c == '0' || c == '1' || c == '2' || c == '3' || c == '4' || c == '5' ||
			c == '6' || c == '7' || c == '8' || c == '9'  || c == '.') {
			return true;
		}
		
		return false;
	}
}
