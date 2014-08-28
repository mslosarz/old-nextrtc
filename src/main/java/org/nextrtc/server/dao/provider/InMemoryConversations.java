package org.nextrtc.server.dao.provider;

import static java.util.Collections.synchronizedSet;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.nextrtc.server.dao.Conversations;
import org.nextrtc.server.domain.Conversation;
import org.nextrtc.server.domain.Member;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.base.Optional;

@Component
@Scope("singleton")
public class InMemoryConversations implements Conversations {

	private Set<Conversation> conversations = synchronizedSet(new HashSet<Conversation>());

	@Override
	public void save(Conversation conversation) {
		if (conversation != null) {
			conversations.add(conversation);
		}
	}

	@Override
	public Optional<Conversation> findBy(String conversationId) {
		Conversation result = null;
		for (Conversation conversation : conversations) {
			if (conversation.getId().equals(conversationId)) {
				result = conversation;
				break;
			}
		}
		return Optional.fromNullable(result);
	}

	@Override
	public Optional<Conversation> findBy(Member member) {
		Conversation result = null;
		for (Conversation conversation : conversations) {
			if (conversation.has(member)) {
				result = conversation;
				break;
			}
		}
		return Optional.fromNullable(result);
	}

	@Override
	public void remove(Conversation conversation) {
		conversations.remove(conversation);
	}

	@Override
	public Collection<Conversation> getAll() {
		return conversations;
	}
}
