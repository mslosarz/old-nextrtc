package org.nextrtc.server.dao.provider;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.nextrtc.server.dao.Conversations;
import org.nextrtc.server.domain.Conversation;
import org.nextrtc.server.domain.Member;
import org.nextrtc.server.domain.provider.DefaultConversation;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.base.Optional;

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
		Conversation result = null;
		for(Conversation conversation : conversations){
			if(conversation.getId().equals(conversationId)){
				result = conversation;
				break;
			}
		}
		return Optional.fromNullable(result);
	}

	@Override
	public Optional<Conversation> findBy(Member member) {
		Conversation result = null;
		for(Conversation conversation : conversations){
			if(conversation.has(member)){
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

}
