package org.nextrtc.server.dao;

import java.util.Optional;

import org.nextrtc.server.domain.Conversation;
import org.nextrtc.server.domain.Member;

public interface Conversations {

	Conversation create();

	Conversation create(String conversationId);

	Optional<Conversation> findBy(String conversationId);

	Optional<Conversation> findBy(Member member);

	void remove(Conversation conversation);


}
