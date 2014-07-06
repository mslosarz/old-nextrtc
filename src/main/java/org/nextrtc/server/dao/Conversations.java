package org.nextrtc.server.dao;

import java.util.Collection;

import org.nextrtc.server.domain.Conversation;
import org.nextrtc.server.domain.Member;

import com.google.common.base.Optional;

public interface Conversations {

	void save(Conversation conversation);

	Optional<Conversation> findBy(String conversationId);

	Optional<Conversation> findBy(Member member);

	void remove(Conversation conversation);

	Collection<Conversation> getAll();

}
