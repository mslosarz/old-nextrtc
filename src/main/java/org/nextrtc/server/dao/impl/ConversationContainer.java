package org.nextrtc.server.dao.impl;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.annotation.Resource;

import org.nextrtc.server.dao.ConversationDao;
import org.nextrtc.server.domain.Conversation;
import org.nextrtc.server.domain.DefaultConversation;
import org.nextrtc.server.domain.Member;
import org.springframework.context.annotation.Scope;

@Resource
@Scope("singleton")
public class ConversationContainer implements ConversationDao {

	private Set<Conversation> conversations = new CopyOnWriteArraySet<>();
	
	@Override
	public Conversation create() {
		Conversation conv = new DefaultConversation();
		conversations.add(conv);
		return conv;
	}

	@Override
	public Conversation findBy(String conversationId) {
		for (Conversation conv : conversations) {
			if (conv.getId().equals(conversationId)) {
				return conv;
			}
		}
		return null;
	}

	@Override
	public Conversation findBy(Member member) {
		for (Conversation conv : conversations) {
			if (conv.has(member)) {
				return conv;
			}
		}
		return null;
	}

	@Override
	public void remove(Conversation conversation) {
		conversations.remove(conversation);
	}

}
