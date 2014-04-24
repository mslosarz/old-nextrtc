package org.nextrc.signaling;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.nextrc.signaling.codec.MessageDecoder;
import org.nextrc.signaling.codec.MessageEncoder;
import org.nextrc.signaling.domain.Message;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.server.standard.SpringConfigurator;

@Component
@ServerEndpoint(//
value = "/signaling",//
decoders = MessageDecoder.class,//
encoders = MessageEncoder.class,//
configurator = SpringConfigurator.class)
public class SignalingEndpoint {
	private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());

	@OnOpen
	public void onOpen(Session session) {
		sessions.add(session);
	}

	@OnMessage
	public void onMessage(Message message, Session session) {
		// for (Session otherSession : sessions) {
		// if (session.equals(otherSession)) {
		// continue;
		// }
		// otherSession.getAsyncRemote().sendText(message);
		// }
	}

	@OnClose
	public void onClose(Session session) {
		sessions.remove(session);
	}

}
