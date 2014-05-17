package org.nextrtc.server.domain.signal;

import org.nextrtc.server.domain.Member;
import org.nextrtc.server.domain.Message;
import org.nextrtc.server.domain.RequestContext;

public interface Signal {

	String name();

	SignalResponse execute(Member member, Message message, RequestContext requestContext);

}
