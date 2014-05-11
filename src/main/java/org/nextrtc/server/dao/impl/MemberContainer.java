package org.nextrtc.server.dao.impl;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.nextrtc.server.dao.MemberDao;
import org.nextrtc.server.domain.DefaultMember;
import org.nextrtc.server.domain.Member;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class MemberContainer implements MemberDao {

	private Set<Member> defaultMembers = new CopyOnWriteArraySet<>();

	@Override
	public Member create() {
		Member member = new DefaultMember();
		defaultMembers.add(member);
		return member;
	}

	@Override
	public Member findBy(String id) {
		for (Member member : defaultMembers) {
			if (member.getId().equals(id)) {
				return member;
			}
		}
		return null;
	}

	@Override
	public void remove(Member member) {
		defaultMembers.remove(member);
	}

	@Override
	public void updateNick(Member member, String nick) {
		if (defaultMembers.contains(member)) {
			defaultMembers.remove(member);
			member.setName(nick);
			defaultMembers.add(member);
		}
	}

}
