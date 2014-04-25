package org.nextrtc.signaling;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.nextrtc.signaling.codec.MessageDecoder;
import org.nextrtc.signaling.codec.MessageEncoder;
import org.nextrtc.signaling.domain.ConversationContainer;
import org.nextrtc.signaling.domain.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.server.standard.SpringConfigurator;

@Component
@Scope("singleton")
@ServerEndpoint(value = "/signaling", decoders = MessageDecoder.class, encoders = MessageEncoder.class, configurator = SpringConfigurator.class)
public class SignalingEndpoint {

	private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());

	@Autowired
	private ConversationContainer container;

	@OnOpen
	public void onOpen(Session session) {
		sessions.add(session);
	}

	@OnMessage
	public void onMessage(Message message, Session session) {
		message.setSession(session);
		System.out.println("" + container);
		message.getSignalAsEnum().execute(message, container);
	}

	@OnClose
	public void onClose(Session session) {
		sessions.remove(session);
	}

}
