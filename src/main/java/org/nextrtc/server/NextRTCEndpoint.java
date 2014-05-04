package org.nextrtc.server;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.nextrtc.server.codec.MessageDecoder;
import org.nextrtc.server.codec.MessageEncoder;
import org.nextrtc.server.domain.Message;
import org.nextrtc.server.domain.NextRTCServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@ServerEndpoint(value = "/signaling", decoders = MessageDecoder.class, encoders = MessageEncoder.class)
public class NextRTCEndpoint {

	@Autowired
	private NextRTCServer server;

	@OnOpen
	public void onOpen(Session session) {
		server.register(session);
	}

	@OnMessage
	public void onMessage(Message message, Session session) {
		server.handle(message, session);
	}

	@OnClose
	public void onClose(Session session) {
		server.unregister(session);
	}

}
