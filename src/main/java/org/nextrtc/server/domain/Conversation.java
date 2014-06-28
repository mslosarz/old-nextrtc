package org.nextrtc.server.domain;

import org.nextrtc.server.domain.signal.SignalResponse;


public interface Conversation {

	String getId();

	SignalResponse joinOwner(Member owner);

	SignalResponse join(Member member);

	SignalResponse routeOffer(Member from, Message offer);

	SignalResponse routeAnswer(Member from, Message answer);

	SignalResponse broadcast(Message message);

	boolean has(Member member);

	SignalResponse disconnect(Member member);

	boolean isEmpty();
}
