package org.nextrtc.server.factory;

import org.nextrtc.server.domain.Conversation;
import org.nextrtc.server.domain.provider.BroadcastConversation;

public class BroadcastConversationFactory implements ConversationFactory {

	@Override
	public Conversation create() {
		return new BroadcastConversation();
	}

	@Override
	public Conversation create(String id) {
		return new BroadcastConversation(id);
	}
}
