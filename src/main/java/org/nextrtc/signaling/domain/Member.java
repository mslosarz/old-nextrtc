package org.nextrtc.signaling.domain;

import javax.websocket.Session;

public class Member {

	public Member(Message fromRequest) {
		this.session = fromRequest.getSession();
		this.mediaDescription = fromRequest.getContent();
	}

	private Session session;

	/**
	 * WebRTC media session description
	 */
	private String mediaDescription;

	public Session getSession() {
		return session;
	}

	public String getMediaDescription() {
		return mediaDescription;
	}

	public boolean hasSession(Session session) {
		return this.session.equals(session);
	}

}
