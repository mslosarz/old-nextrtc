package org.nextrtc.server.dao;

import org.nextrtc.server.domain.Member;

public interface MemberDao {

	Member create();

	Member findBy(String id);

	void remove(Member member);

	void updateNick(Member member, String nick);

}
