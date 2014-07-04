package org.nextrtc.server.factory.provider;

import org.nextrtc.server.domain.Member;
import org.nextrtc.server.domain.provider.DefaultMember;
import org.nextrtc.server.factory.MemberFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class DefaultMemberFactory implements MemberFactory {

	@Override
	public Member create() {
		return new DefaultMember();
	}

}
