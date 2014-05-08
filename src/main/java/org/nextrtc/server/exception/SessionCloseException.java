package org.nextrtc.server.exception;

@SuppressWarnings("serial")
public class SessionCloseException extends RuntimeException {

	public SessionCloseException(Throwable e) {
		super(e);
	}

}
