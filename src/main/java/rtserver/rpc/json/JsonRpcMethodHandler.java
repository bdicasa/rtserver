package rtserver.rpc.json;

import rtserver.Communicator;
import rtserver.Server;

public abstract class JsonRpcMethodHandler {
	
	protected final Communicator communicator;
	protected final JsonCommunicator jsonCommunicator;
	
	public JsonRpcMethodHandler() {
		this.communicator = Server.getCommunicator();
		this.jsonCommunicator = new JsonCommunicator(communicator);
	}
	
	public JsonRpcMethodHandler(Communicator communicator) {
		this.communicator = communicator;
		this.jsonCommunicator = new JsonCommunicator(communicator);
	}
	
	public abstract void handle(JsonRpcRequest request, JsonRpcResponseWriter writer);
}
