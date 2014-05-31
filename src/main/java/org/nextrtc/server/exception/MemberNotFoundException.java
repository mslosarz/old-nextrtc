package org.nextrtc.server.exception;

@SuppressWarnings("serial")
public class MemberNotFoundException extends RuntimeException {

	public MemberNotFoundException() {
		super("Member do not exists!");
	}
}
