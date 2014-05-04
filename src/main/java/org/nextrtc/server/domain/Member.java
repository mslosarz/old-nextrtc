package org.nextrtc.server.domain;

public interface Member {

	public abstract String getId();

	public abstract String getNick();

	public abstract void setNick(String nick);

}