package io.rtserver.examples.netty.chat;

import java.util.HashMap;
import java.util.Map;

import io.rtserver.netty.Server;

public class App {
	
	public static void main(String[] args) {
		
		Map<String, Object> config = new HashMap<String, Object>();
		config.put("jsonRpcMethodHandlerPackage", "io.rtserver.examples.netty.chat.jsonhandlers");
		
		Server server = new Server(9000, config);
		server.start();
	}
}
