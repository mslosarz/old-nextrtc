package org.nextrtc.server;

import java.util.Set;
import java.util.UUID;

import javax.websocket.*;

import org.apache.log4j.Logger;
import org.nextrtc.server.domain.Message;
import org.nextrtc.server.domain.NextRTCServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;

@Component
public class NextRTCEndpoint {
	private static final Logger log = Logger.getLogger(NextRTCEndpoint.class);

	private String id = UUID.randomUUID().toString();

	private static Set<NextRTCEndpoint> endpoints = Sets.newConcurrentHashSet();

	public NextRTCEndpoint() {
		endpoints.add(this);
		log.info("Created " + this);
		for (NextRTCEndpoint endpoint : endpoints) {
			if (endpoint.server != null) {
				this.setServer(endpoint.server);
				break;
			}
		}
	}

	public static Set<NextRTCEndpoint> getEndpoints() {
		return endpoints;
	}

	private NextRTCServer server;

	@Autowired
	public void setServer(NextRTCServer server) {
		log.info("Setted server: " + server + " to " + this);
		this.server = server;
	}

	@OnOpen
	public void onOpen(Session session, EndpointConfig config) {
		log.info("Opening " + server + " in " + this);
		log.debug("Opening: " + session.getId());
		server.register(session);
	}

	@OnMessage
	public void onMessage(Message message, Session session) {
		log.debug("Handling message from: " + session.getId());
		server.handle(message, session);
	}

	@OnClose
	public void onClose(Session session, CloseReason reason) {
		log.debug("Closing: " + session.getId() + " with reason: " + reason.getReasonPhrase());
		server.unregister(session);
	}

	@OnError
	public void onError(Session session, Throwable t) {
		t.printStackTrace();
		session.getAsyncRemote().sendObject(Message.createWith("error")//
				.withContent(t.getMessage())//
				.build());
		server.unregister(session);
	}

	@Override
	public String toString() {
		return "NextRTCEndpoint (" + id + ")";
	}
}
