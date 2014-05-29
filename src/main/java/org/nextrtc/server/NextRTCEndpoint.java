package org.nextrtc.server;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import org.nextrtc.server.domain.Message;
import org.nextrtc.server.domain.NextRTCServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

@Component
public class NextRTCEndpoint {

	@Autowired
	private NextRTCServer server;

	public NextRTCEndpoint() {
		WebApplicationContext ctx = ContextLoader.getCurrentWebApplicationContext();
		if (ctx != null) {
			server = ctx.getBean(NextRTCServer.class);
		}
	}

	@OnOpen
	public void onOpen(Session session) {
		server.register(session);
	}

	@OnMessage
	public void onMessage(Message message, Session session) {
		server.handle(message, session);
	}

	@OnClose
	public void onClose(Session session) {
		server.unregister(session);
	}

	@OnError
	public void onError(Session session, Throwable t) {
		session.getAsyncRemote().sendObject(Message.createWith("error")//
				.withContent(t.getMessage())//
				.build());
	}

	public void setServer(NextRTCServer server) {
		this.server = server;
	}
}
