package org.nextrtc.server.domain;

import static java.util.Collections.synchronizedSet;
import static org.nextrtc.server.domain.signal.DefaultSignals.answerRequest;
import static org.nextrtc.server.domain.signal.DefaultSignals.created;
import static org.nextrtc.server.domain.signal.DefaultSignals.finalize;
import static org.nextrtc.server.domain.signal.DefaultSignals.left;
import static org.nextrtc.server.domain.signal.DefaultSignals.offerRequest;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.nextrtc.server.exception.MemberNotFoundException;

public class DefaultConversation implements Conversation {

	private Set<Member> members = synchronizedSet(new HashSet<Member>());
	private String id;

	public DefaultConversation() {
		this.id = UUID.randomUUID().toString();
	}

	public SignalResponse broadcast(Message message) {
		SignalResponse signalResponse = new SignalResponse(message);
		signalResponse.addAll(members);
		return signalResponse;
	}

	@Override
	public SignalResponse disconnect(Member leaving) {
		members.remove(leaving);
		return broadcast(Message//
				.createWith(left)//
				.member(leaving)//
				.build());
	}

	@Override
	public SignalResponse joinOwner(Member owner) {
		members.add(owner);

		Message response = Message//
				.createWith(created)//
				.withContent(id)//
				.build();//

		return broadcast(response);
	}

	public SignalResponse join(Member member) {
		Message message = Message//
				.createWith(offerRequest)//
				.member(member)//
				.build();

		SignalResponse broadcast = broadcast(message);

		members.add(member);
		return broadcast;
	}

	public SignalResponse routeOffer(Member from, Message offer) {
		Member member = findById(offer.getMemberId());

		Message message = Message//
				.createWith(answerRequest)//
				.member(from)//
				.withContent(offer.getContent())//
				.build();

		return new SignalResponse(message, member);
	}

	public SignalResponse routeAnswer(Member from, Message answer) {
		Member member = findById(answer.getMemberId());

		Message message = Message//
				.createWith(finalize)//
				.member(from)//
				.withContent(answer.getContent())//
				.build();

		return new SignalResponse(message, member);
	}

	public boolean has(Member member) {
		return members.contains(member);
	}

	public String getId() {
		return id;
	}

	private Member findById(String memberId) {
		for (Member member : members) {
			if (memberId.equals(member.getId())) {
				return member;
			}
		}
		throw new MemberNotFoundException();
	}

}
