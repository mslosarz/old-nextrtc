package org.nextrtc.server.dao.provider;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.nextrtc.server.dao.Members;
import org.nextrtc.server.domain.Member;
import org.nextrtc.server.domain.provider.DefaultMember;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class InMemboryMembers implements Members {

	private Set<Member> defaultMembers = new CopyOnWriteArraySet<>();

	@Override
	public Member create() {
		Member member = new DefaultMember();
		defaultMembers.add(member);
		return member;
	}

	@Override
	public Optional<Member> findBy(String id) {
		return defaultMembers.stream()//
				.filter(member -> member.getId().equals(id))//
				.findFirst();
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
