package org.nextrtc.server.domain;

import org.nextrtc.server.dao.Conversations;
import org.nextrtc.server.dao.Members;

public class RequestContext {

	private Conversations conversations;

	private Members members;

	public RequestContext(Conversations conversations, Members members) {
		this.conversations = conversations;
		this.members = members;
	}

	public Conversations getConversations() {
		return conversations;
	}

	public Members getMembers() {
		return members;
	}

}
