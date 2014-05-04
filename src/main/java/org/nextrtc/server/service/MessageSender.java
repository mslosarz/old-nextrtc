package org.nextrtc.server.service;

import org.nextrtc.server.domain.SenderRequest;

public interface MessageSender {

	void send(SenderRequest messages);

}
