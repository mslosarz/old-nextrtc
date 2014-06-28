package org.nextrtc.server;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import org.apache.log4j.Logger;
import org.nextrtc.server.domain.Message;
import org.nextrtc.server.domain.NextRTCServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

@Component
public class NextRTCEndpoint {
	private static final Logger log = Logger.getLogger(NextRTCEndpoint.class);

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
		log.debug("Opening: " + session.getId());
		server.register(session);
	}

	@OnMessage
	public void onMessage(Message message, Session session) {
		log.debug("Handling message from: " + session.getId());
		server.handle(message, session);
	}

	@OnClose
	public void onClose(Session session) {
		log.debug("Closing: " + session.getId());
		server.unregister(session);
	}

	@OnError
	public void onError(Session session, Throwable t) {
		session.getAsyncRemote().sendObject(Message.createWith("error")//
				.withContent(t.getMessage())//
				.build());
	}
}
