package org.nextrc.signaling;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/signaling")
public class SignalingServlet {

	private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());

	@OnOpen
	public void onOpen(Session session) {
		sessions.add(session);
	}

	@OnMessage
	public void onMessage(String message, Session session) {
		for (Session otherSession : sessions) {
			if (session.equals(otherSession)) {
				continue;
			}
			otherSession.getAsyncRemote().sendText(message);
		}
	}

	@OnClose
	public void onClose(Session session) {
		sessions.remove(session);
	}

}
