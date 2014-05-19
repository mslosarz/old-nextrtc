package org.nextrtc.server.dao.provider;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.nextrtc.server.dao.Conversations;
import org.nextrtc.server.domain.Conversation;
import org.nextrtc.server.domain.Member;
import org.nextrtc.server.domain.provider.DefaultConversation;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class InMemoryConversations implements Conversations {

	private Set<Conversation> conversations = new CopyOnWriteArraySet<>();

	@Override
	public Conversation create() {
		Conversation conv = new DefaultConversation();
		conversations.add(conv);
		return conv;
	}

	@Override
	public Conversation create(String id) {
		Conversation conv = new DefaultConversation(id);
		conversations.add(conv);
		return conv;
	}

	@Override
	public Optional<Conversation> findBy(String conversationId) {
		return conversations.stream()//
				.filter(conv -> conv.getId().equals(conversationId))//
				.findFirst();
	}

	@Override
	public Optional<Conversation> findBy(Member member) {
		return conversations.stream()//
				.filter(conv -> conv.has(member))//
				.findFirst();
	}

	@Override
	public void remove(Conversation conversation) {
		conversations.remove(conversation);
	}

}
