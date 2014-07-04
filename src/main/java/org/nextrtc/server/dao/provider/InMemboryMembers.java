package org.nextrtc.server.dao.provider;

import static java.util.Collections.synchronizedSet;

import java.util.HashSet;
import java.util.Set;

import org.nextrtc.server.dao.Members;
import org.nextrtc.server.domain.Member;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.base.Optional;

@Component
@Scope("singleton")
public class InMemboryMembers implements Members {

	private Set<Member> defaultMembers = synchronizedSet(new HashSet<Member>());

	@Override
	public void save(Member member) {
		defaultMembers.add(member);
	}

	@Override
	public Optional<Member> findBy(String id) {
		Member result = null;
		for(Member member : defaultMembers){
			if(member.getId().equals(id)){
				result = member;
				break;
			}
		}
		return Optional.fromNullable(result);
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
