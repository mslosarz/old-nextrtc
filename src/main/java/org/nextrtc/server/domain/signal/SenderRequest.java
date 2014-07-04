package org.nextrtc.server.domain.signal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.websocket.Session;

import org.nextrtc.server.domain.Message;

public class SenderRequest {

	private Map<Message, Set<Session>> sessions = new HashMap<>();

	public void add(Message message, Session session) {
		if (sessions.containsKey(message) == false) {
			sessions.put(message, new HashSet<Session>());
		}
		if (session != null) {
			sessions.get(message).add(session);
		}
	}

	public Map<Message, Set<Session>> getSessions() {
		return sessions;
	}
}
