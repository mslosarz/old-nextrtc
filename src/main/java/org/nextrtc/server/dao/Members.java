package org.nextrtc.server.dao;


import java.util.Collection;

import org.nextrtc.server.domain.Member;

import com.google.common.base.Optional;

public interface Members {

	void save(Member member);

	Optional<Member> findBy(String id);

	void remove(Member member);

	void update(Member member);

	Collection<Member> getAll();

}
