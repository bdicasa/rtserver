# RTServer

RTServer is framework that makes real time communication over the web easy.

Todays applications need to be able to communicate in a way they never have before.
It is not unusual to develop a product that has numerous clients. For example, you may offer
a native iPhone client and a web based client for your customers. Ideally you want the customers who
are using the iPhone client to communicate with the customers using the web client in real time.

This is exactly what RTServer allows you to do. It abstracts away the complexities of the various
methods used for real time communication in todays application. The iPhone application
can use a raw TCP socket, where as your web application can use WebSockets when supported,
and a long polling solution when WebSockets are not supported.

Please not that RTServer is still under development and is not ready for production use. More
documentation will be added as the project progresses.

# An example application

To get a feeling for how RTServer works, an example echo application is shown below.
This application simply writes a received message to everyone connected to the server.
In order to avoid bloat, only the code which uses the RTServer and RTClient API is shown.

RTServer's default way of communicating is to use JSON (it can be extended to communicate using
other strategies, more on this later). JSON RPC is used to send requests to the server, where as
listeners are used to handle incoming messages from the server.

## The echo web client code

```javascript
// Create a socket for communication. This instructs the browser to use WebSockets when available
// and mimics a socket using long polling when WebSockets is not available.
var socket = new RTClient.Socket({
	host: 'localhost',
	path: '/websocket'
});

// The JSON Communicator is used to send JSON RPC requests, and listen for
// JSON messages coming from the server.
var jsonComm = new RTClient.JsonCommunicator(socket);

// Instruct the communicator to listen for the messageReceived message.
// Whenever a messageReceived message is received from the server, the provided function is run.
jsonComm.listen('chatMessageReceived', function(data) {
	displayMessage(data.message);
});

// This instructs the communicator to send a JSON RPC call to the server.
jsonComm.sendRpc({
	method: 'sendMessage',
	params: {
		message: 'Hello world!'
	},
	success: function(data) {
		// Callback that can be executed when a successful RPC call was made. data
		// is a JavaScript object that contains any data received from the server.
	},
	error: function(message, code) {
		// Executed when RPC call resulted in an error.
	}
});
```

## The echo server code

The server code is handled in a single Java class that is annotated with JsonRpcMethod.
Whenever a client sends a JSON RPC request with the sendMessage method specified, this
class' handle method will be called.

```java
@JsonRpcMethod("sendMessage")
public class SendChatMessageHandler extends JsonRpcMethodHandler {

	public void handle(JsonRpcRequest request, JsonRpcResponseWriter writer) {
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("message", request.params.get("message"));
		// The broadcast method writes the JSON message to all connections
		this.jsonCommunicator.broadcast("messageReceived", params);
		writer.writeEmptyResponse();
	}
}
```





