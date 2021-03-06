package org.nextrtc.server.service.provider;

import java.util.Map;
import java.util.Set;

import javax.websocket.Session;

import org.apache.log4j.Logger;
import org.nextrtc.server.domain.Message;
import org.nextrtc.server.domain.signal.SenderRequest;
import org.nextrtc.server.service.MessageSender;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class DefaultMessageSender implements MessageSender {
	static final Logger log = Logger.getLogger(DefaultMessageSender.class);

	@Override
	public void send(SenderRequest request) {
		if (request != null) {
			Map<Message, Set<Session>> sessions = request.getSessions();
			for (Message message : sessions.keySet()) {
				for (Session session : sessions.get(message)) {
					log.debug("SENT: " + message + " TO: " + session.getId());
					session.getAsyncRemote().sendObject(message);
				}
			}
		}
	}

}
