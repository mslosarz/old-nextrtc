package org.nextrtc.server.domain;

import java.util.HashSet;
import java.util.Set;

import javax.websocket.Session;

public class SenderRequest {

	private Message message;

	private Set<Session> sessions = new HashSet<>();

	public SenderRequest(Message message) {
		this.message = message;
	}

	public Message getMessage() {
		return message;
	}

	public Set<Session> getSessions() {
		return sessions;
	}

	public void add(Session session) {
		sessions.add(session);
	}

}
