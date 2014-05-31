package org.nextrtc.server.exception;

@SuppressWarnings("serial")
public class ConversationNotFoundException extends RuntimeException {

	public ConversationNotFoundException() {
		super("Conversation do not exists!");
	}
}
