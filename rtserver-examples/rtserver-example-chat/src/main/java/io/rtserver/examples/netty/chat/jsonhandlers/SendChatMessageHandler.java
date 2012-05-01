package io.rtserver.examples.netty.chat.jsonhandlers;

import java.util.HashMap;
import java.util.Map;

import io.rtserver.rpc.json.*;
import io.rtserver.sockets.Socket;

@JsonRpcMethod("sendChatMessage")
public class SendChatMessageHandler extends JsonRpcMethodHandler {

	public void handle(Socket connection, JsonRpcRequest request, JsonRpcResponseWriter writer) {
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("chatMessage", request.params.get("chatMessage"));
		
		String chatRoom = request.params.get("chatRoom").toString();
		this.jsonComm.publish(chatRoom, params);
	}
}
