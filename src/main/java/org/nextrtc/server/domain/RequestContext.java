package org.nextrtc.server.domain;

import org.nextrtc.server.dao.Conversations;
import org.nextrtc.server.dao.Members;
import org.nextrtc.server.factory.ConversationFactoryResolver;

public class RequestContext {

	private Conversations conversations;

	private Members members;

	private ConversationFactoryResolver conversationFactoryResolver;

	public RequestContext(Conversations conversations, Members members, ConversationFactoryResolver conversationFactoryResolver) {
		this.conversations = conversations;
		this.members = members;
		this.conversationFactoryResolver = conversationFactoryResolver;
	}

	public Conversations getConversations() {
		return conversations;
	}

	public Members getMembers() {
		return members;
	}

	public ConversationFactoryResolver getConversationFactoryResolver() {
		return conversationFactoryResolver;
	}

}
