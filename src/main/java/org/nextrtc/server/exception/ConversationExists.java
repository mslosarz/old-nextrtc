package org.nextrtc.server.exception;

@SuppressWarnings("serial")
public class ConversationExists extends RuntimeException {

	public ConversationExists(String message) {
		super(message);
	}

}
