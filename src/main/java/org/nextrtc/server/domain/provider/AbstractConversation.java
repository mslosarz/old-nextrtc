package org.nextrtc.server.domain.provider;

import static java.util.Collections.synchronizedSet;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.nextrtc.server.domain.Conversation;
import org.nextrtc.server.domain.Member;
import org.nextrtc.server.domain.Message;
import org.nextrtc.server.domain.signal.SignalResponse;
import org.nextrtc.server.exception.MemberNotFoundException;

public abstract class AbstractConversation implements Conversation {

	protected Set<Member> members = synchronizedSet(new HashSet<Member>());

	protected String id;

	public AbstractConversation() {
		id = UUID.randomUUID().toString();
	}

	public AbstractConversation(String id) {
		if (StringUtils.isEmpty(id)) {
			throw new IllegalArgumentException("Conversation id must be set.");
		}
		this.id = id;
	}

	protected SignalResponse broadcast(Message message) {
		return new SignalResponse(message, members);
	}

	protected Member findById(String memberId) {
		for (Member member : members) {
			if (memberId.equals(member.getId())) {
				return member;
			}
		}
		throw new MemberNotFoundException();
	}

	@Override
	public boolean has(Member member) {
		return members.contains(member);
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public boolean isEmpty() {
		return members.isEmpty();
	}

	@Override
	public Collection<Member> members() {
		return synchronizedSet(new HashSet<Member>(members));
	}

}
