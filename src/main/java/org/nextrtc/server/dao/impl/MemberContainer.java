package org.nextrtc.server.dao.impl;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Resource;

import org.nextrtc.server.dao.MemberDao;
import org.nextrtc.server.domain.DefaultMember;
import org.nextrtc.server.domain.Member;
import org.springframework.context.annotation.Scope;

@Resource
@Scope("singleton")
public class MemberContainer implements MemberDao {

	private Set<Member> defaultMembers = new HashSet<>();

	@Override
	public Member create() {
		Member member = new DefaultMember();
		defaultMembers.add(member);
		return member;
	}

	@Override
	public void remove(Member member) {
		defaultMembers.remove(member);
	}

	@Override
	public void updateNick(Member member, String nick) {
		if (defaultMembers.contains(member)) {
			defaultMembers.remove(member);
			member.setNick(nick);
			defaultMembers.add(member);
		}
	}
}
