package org.nextrtc.server.domain;

import java.util.UUID;

public class DefaultMember implements Member {

	private String id;

	private String nick;

	public DefaultMember() {
		this.id = UUID.randomUUID().toString();
	}

	public DefaultMember(String id, String nick) {
		this.id = id;
		this.nick = nick;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getNick() {
		return nick;
	}

	@Override
	public void setNick(String nick) {
		this.nick = nick;
	}

	@Override
	public String toString() {
		return "Member (" + id + " " + nick + ")";
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof DefaultMember)) {
			return false;
		}
		DefaultMember other = (DefaultMember) obj;
		return id.equals(other.id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}
}
