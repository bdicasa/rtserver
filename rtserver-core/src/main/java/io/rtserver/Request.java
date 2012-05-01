package io.rtserver;

import io.rtserver.sockets.Socket;


public class Request {
	
	public final Socket socket;
	public final byte[] content;
	
	public Request(Socket socket, byte[] content) {
		
		this.socket = socket;
		this.content = content;
	}
	
}
