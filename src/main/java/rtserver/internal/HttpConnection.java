package rtserver.internal;

import static org.jboss.netty.handler.codec.http.HttpHeaders.setContentLength;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.*;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Values.NO_CACHE;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.util.CharsetUtil;
import rtserver.Connection;

public abstract class HttpConnection implements Connection {
	
	protected ChannelFuture writeResponse(Channel writeChannel, Object data) {
		
		HttpResponse res = new DefaultHttpResponse(HTTP_1_1, OK);
		ChannelBuffer buffer = null;
		
		if (data instanceof String) {
			
			// TODO: Should probably read incoming HTTP request and set charset based on that.
			buffer = ChannelBuffers.copiedBuffer((String) data, CharsetUtil.UTF_8);
			res.setHeader(CACHE_CONTROL, NO_CACHE);
			res.setHeader("Content-Type", "text/html");
		
		} else if (data instanceof byte[]) {
			buffer = ChannelBuffers.copiedBuffer((byte[]) data);
		
		} else if (data instanceof ChannelBuffer) {
			
			buffer = (ChannelBuffer) data;
		}
		
		res.setContent(buffer);
		setContentLength(res, res.getContent().readableBytes());
		return writeChannel.write(res);
	}
	
	/**
	 * Writes an HTTP response to the specified channel. The content length header
	 * is set before the response is written.
	 * @param writeChannel
	 * @param data
	 */
	protected ChannelFuture writeResponse(Channel writeChannel, HttpResponse response) {
		
		if (response.getHeader(CONTENT_LENGTH) == null) {
			setContentLength(response, response.getContent().readableBytes());
		}
		
		return writeChannel.write(response);
	}
}
