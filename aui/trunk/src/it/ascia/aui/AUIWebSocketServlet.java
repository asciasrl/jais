package it.ascia.aui;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

@SuppressWarnings("serial")
public class AUIWebSocketServlet extends WebSocketServlet {

	@Override
	public void configure(WebSocketServletFactory factory) {
		factory.register(AUIStreamingWebSocket.class);		
	}

}
