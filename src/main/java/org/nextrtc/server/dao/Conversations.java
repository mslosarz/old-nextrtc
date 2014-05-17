package org.nextrtc.server.dao;

import org.nextrtc.server.domain.Conversation;
import org.nextrtc.server.domain.Member;

public interface Conversations {

	Conversation create();

	Conversation findBy(String conversationId);

	Conversation findBy(Member member);

	void remove(Conversation conversation);

}
