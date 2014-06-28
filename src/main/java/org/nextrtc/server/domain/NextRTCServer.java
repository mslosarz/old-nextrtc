package org.nextrtc.server.domain;

import static org.nextrtc.server.domain.signal.SignalRegistry.DefaultSignal.left;

import java.io.IOException;

import javax.websocket.Session;

import org.apache.log4j.Logger;
import org.nextrtc.server.dao.Conversations;
import org.nextrtc.server.dao.Members;
import org.nextrtc.server.domain.signal.SenderRequest;
import org.nextrtc.server.domain.signal.SignalResponse;
import org.nextrtc.server.exception.MemberNotFoundException;
import org.nextrtc.server.exception.SessionCloseException;
import org.nextrtc.server.service.MessageSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;

@Component
@Scope("singleton")
public class NextRTCServer {
	private static final Logger log = Logger.getLogger(NextRTCServer.class);

	@Autowired
	private Members members;

	@Autowired
	private Conversations conversations;

	@Autowired
	private MessageSender messageSender;

	private final BiMap<Session, String> memberSession = Maps.synchronizedBiMap(//
			HashBiMap.<Session, String> create());

	public void register(Session session) {
		bindSessionToMember(session);
	}

	public void handle(Message message, Session session) {
		Member member = getMemberBy(session);

		SignalResponse messages = execute(message, member);

		messageSender.send(transform(messages));
	}

	public void unregister(Session session) {
		boolean sessionBoundToMember = memberSession.get(session) != null;
		if (sessionBoundToMember) {
			Member member = disconnectMemberFromConversation(session);

			removeMember(member);

			unbindSession(session);

			tryToCloseSession(session);
		}
	}

	private Member getMemberBy(Session session) {
		for (Member member : members.findBy(memberSession.get(session)).asSet()) {
			return member;
		}
		throw new MemberNotFoundException();
	}

	private void bindSessionToMember(Session session) {
		Member member = members.create();
		log.debug("New member: " + member + " has been created and bind to session: " + session.getId());
		memberSession.put(session, member.getId());
	}

	private SignalResponse execute(Message message, Member member) {
		SignalResponse messages = message.getSignal().execute(//
				member,//
				message,//
				new RequestContext(conversations, members)//
				);
		return messages;
	}

	private SenderRequest transform(SignalResponse response) {
		SenderRequest request = new SenderRequest(response.getMessage());
		for (Member recipient : response.getRecipients()) {
			request.add(memberSession.inverse().get(recipient.getId()));
		}
		return request;
	}

	private Member disconnectMemberFromConversation(Session session) {
		Member member = getMemberBy(session);

		boolean existsConversationWithMember = conversations.findBy(member).isPresent();

		if (existsConversationWithMember) {
			handle(Message.createWith(left).build(), session);
		}

		return member;
	}

	private void removeMember(Member member) {
		members.remove(member);
	}

	private void unbindSession(Session session) {
		memberSession.remove(session);
	}

	private void tryToCloseSession(Session session) {
		try {
			session.close();
		} catch (IOException e) {
			new SessionCloseException(e);
		}
	}

}
