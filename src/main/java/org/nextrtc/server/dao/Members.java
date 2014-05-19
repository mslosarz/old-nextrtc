package org.nextrtc.server.dao;

import java.util.Optional;

import org.nextrtc.server.domain.Member;

public interface Members {

	Member create();

	Optional<Member> findBy(String id);

	void remove(Member member);

	void updateNick(Member member, String nick);

}
