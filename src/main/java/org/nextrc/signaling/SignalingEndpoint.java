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
import org.nextrc.signaling.domain.ConversationContainer;
import org.nextrc.signaling.domain.Message;
import org.springframework.beans.factory.annotation.Autowired;
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

	@Autowired
	private ConversationContainer container;

	@OnOpen
	public void onOpen(Session session) {
		sessions.add(session);
	}

	@OnMessage
	public void onMessage(Message message, Session session) {
		message.initSession(session);
		message.getOperationAsEnum().execute(message, container);
	}

	@OnClose
	public void onClose(Session session) {
		sessions.remove(session);
	}

}
