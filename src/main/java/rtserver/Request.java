package rtserver;


public class Request {
	
	public final Connection connection;
	public final Object content;
	
	public Request(Connection connection, Object content) {
		this.connection = connection;
		this.content = content;
	}
}
