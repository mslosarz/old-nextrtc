package org.nextrtc.server.dao.impl;

import javax.annotation.Resource;

import org.nextrtc.server.dao.ConversationDao;
import org.nextrtc.server.domain.Conversation;
import org.nextrtc.server.domain.Member;
import org.springframework.context.annotation.Scope;

@Resource
@Scope("singleton")
public class ConversationContainer implements ConversationDao {

	@Override
	public Conversation create() {
		return null;
	}

	@Override
	public Conversation findBy(String conversationId) {
		return null;
	}

	@Override
	public Conversation findBy(Member member) {
		return null;
	}

	@Override
	public void remove(Conversation conversation) {
	}

}
