package rtserver;

import java.util.Map;

public abstract class MessageHandler {
	
	protected Map<String, Object> serverConfig;
	
	public MessageHandler(Map<String, Object> serverConfig) {
		this.serverConfig = serverConfig;
	}
	/**
	 * Override this to perform any required logic when the server is started.
	 */
	public void serverStarted(Map<String, Object> config) { }
	
	/**
	 * Implement this method to handle incoming messages.
	 * @param request Contains request information.
	 * @param res The response object write to.
	 */
	public abstract void messageReceived(final Request request, final Response response);
}
