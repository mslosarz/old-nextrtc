package org.nextrtc.server.factory;

import org.nextrtc.server.domain.Conversation;

public interface ConversationFactory {

	Conversation create();

	Conversation create(String conversationId);

}
