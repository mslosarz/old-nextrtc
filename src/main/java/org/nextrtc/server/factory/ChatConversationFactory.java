package org.nextrtc.server.factory;

import org.nextrtc.server.domain.Conversation;
import org.nextrtc.server.domain.provider.ChatConversation;

public class ChatConversationFactory implements ConversationFactory {

	@Override
	public Conversation create() {
		return new ChatConversation();
	}

	@Override
	public Conversation create(String id) {
		return new ChatConversation(id);
	}

}
