package org.nextrtc.server.dao;


import org.nextrtc.server.domain.Member;

import com.google.common.base.Optional;

public interface Members {

	void save(Member member);

	Optional<Member> findBy(String id);

	void remove(Member member);

	void updateNick(Member member, String nick);

}
