package org.nextrtc.server.domain;

import org.nextrtc.server.dao.ConversationDao;
import org.nextrtc.server.dao.MemberDao;

public class RequestContext {

	private ConversationDao conversationDao;

	private MemberDao memberDao;

	public RequestContext(ConversationDao conversationDao, MemberDao memberDao) {
		this.conversationDao = conversationDao;
		this.memberDao = memberDao;
	}

	public ConversationDao getConversationDao() {
		return conversationDao;
	}

	public MemberDao getMemberDao() {
		return memberDao;
	}

}
