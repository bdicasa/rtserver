package io.rtserver.examples.netty.chat.jsonhandlers;

import io.rtserver.rpc.json.JsonRpcMethod;
import io.rtserver.rpc.json.JsonRpcMethodHandler;
import io.rtserver.rpc.json.JsonRpcRequest;
import io.rtserver.rpc.json.JsonRpcResponseWriter;
import io.rtserver.sockets.Socket;

@JsonRpcMethod("joinChatRoom")
public class JoinChatRoomHandler extends JsonRpcMethodHandler {
	
	public void handle(Socket connection, JsonRpcRequest request, JsonRpcResponseWriter writer) {
		
		String chatRoom = (String)request.params.get("chatRoom");
		jsonComm.subscribe(chatRoom, connection.getId());
	}
}