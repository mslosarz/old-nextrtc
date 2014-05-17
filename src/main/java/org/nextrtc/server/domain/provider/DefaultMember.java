package org.nextrtc.server.domain.provider;

import java.util.UUID;

import org.nextrtc.server.domain.Member;

public class DefaultMember implements Member {

	private String id;

	private String name;

	public DefaultMember() {
		this.id = UUID.randomUUID().toString();
	}

	public DefaultMember(String id, String name) {
		this.id = id;
		this.name = name;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "Member (" + id + " " + name + ")";
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
