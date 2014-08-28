package org.nextrtc.server.domain;

import static org.nextrtc.server.domain.signal.DefaultSignal.left;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.websocket.Session;

import org.apache.log4j.Logger;
import org.nextrtc.server.dao.Conversations;
import org.nextrtc.server.dao.Members;
import org.nextrtc.server.domain.signal.SenderRequest;
import org.nextrtc.server.domain.signal.SignalResponse;
import org.nextrtc.server.exception.MemberNotFoundException;
import org.nextrtc.server.exception.SessionCloseException;
import org.nextrtc.server.factory.ConversationFactoryResolver;
import org.nextrtc.server.factory.MemberFactory;
import org.nextrtc.server.service.MessageSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.base.Optional;
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

	@Autowired
	private ConversationFactoryResolver conversationFactoryResolver;

	@Autowired
	private MemberFactory memberFactory;

	private final BiMap<Session, String> memberSession = Maps.synchronizedBiMap(//
			HashBiMap.<Session, String> create());

	public void register(Session session) {
		bindSessionToMember(session, memberFactory.create());
	}

	public void handle(Message message, Session session) {
		Member member = getMemberBy(session);

		SignalResponse messages = execute(message, member);

		messageSender.send(transform(messages));
	}

	public void unregister(Session session) {
		boolean sessionBoundToMember = memberSession.get(session) != null;
		if (sessionBoundToMember) {
			Optional<Member> member = disconnectMemberFromConversation(session);
			if (member.isPresent()) {
				removeMember(member.get());
			}

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

	private void bindSessionToMember(Session session, Member member) {
		members.save(member);
		log.debug("New member: " + member + " has been created and bind to session: " + session.getId());
		memberSession.put(session, member.getId());
	}

	private SignalResponse execute(Message message, Member member) {
		SignalResponse messages = message.getSignal().execute(//
				member,//
				message,//
				new RequestContext(conversations, members, conversationFactoryResolver)//
				);
		return messages;
	}

	private SenderRequest transform(SignalResponse response) {
		SenderRequest request = new SenderRequest();
		Map<Message, List<Member>> recipients = response.getRecipients();
		for(Message message : recipients.keySet()){
			for(Member recipient : recipients.get(message)){
				request.add(message, memberSession.inverse().get(recipient.getId()));
			}
		}
		return request;
	}

	private Optional<Member> disconnectMemberFromConversation(Session session) {
		Optional<Member> member = members.findBy(memberSession.get(session));

		boolean existsConversationWithMember = member.isPresent() && conversations.findBy(member.get()).isPresent();

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
