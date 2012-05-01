package io.rtserver.concurrent;

public abstract class Actor {
	
//	private final Fiber fiber;
//	private final Channel<Object> channel;
//	
//	public Actor() {
//		this.fiber = Server.getFiberFactory().create();
//		this.channel = new MemoryChannel<Object>();
//		setup();
//	}
//	
//	public Actor(Fiber fiber) {
//		this.fiber = fiber;
//		this.channel = new MemoryChannel<Object>();
//		setup();
//	}
//	
//	private void setup() {
//		this.fiber.start();
//		
//		Callback<Object> callback = new Callback<Object>() {
//			public void onMessage(Object message) {
//				onReceive(message);
//			}
//		};
//		
//		this.channel.subscribe(fiber, callback);
//	}
//	public void tell(Object message) {
//		this.channel.publish(message);
//	}
//	
//	protected abstract void onReceive(Object message);
}
