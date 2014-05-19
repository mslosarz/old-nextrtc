package org.nextrtc.server.service.provider;

import org.nextrtc.server.domain.Message;
import org.nextrtc.server.domain.signal.SenderRequest;
import org.nextrtc.server.service.MessageSender;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class DefaultMessageSender implements MessageSender {

	@Override
	public void send(SenderRequest request) {
		if (request != null) {
			Message message = request.getMessage();
			request.getSessions()//
					.forEach(session -> session.getAsyncRemote().sendObject(message));
		}
	}

}
