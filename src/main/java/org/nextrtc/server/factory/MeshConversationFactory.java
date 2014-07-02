package org.nextrtc.server.factory;

import org.nextrtc.server.domain.Conversation;
import org.nextrtc.server.domain.provider.MeshConversation;

public class MeshConversationFactory implements ConversationFactory {

	@Override
	public Conversation create() {
		return new MeshConversation();
	}

	@Override
	public Conversation create(String id) {
		return new MeshConversation(id);
	}

}
