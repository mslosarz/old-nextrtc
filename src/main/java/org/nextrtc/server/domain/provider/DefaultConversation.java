package org.nextrtc.server.domain.provider;

import static java.util.Collections.synchronizedSet;
import static org.nextrtc.server.domain.signal.SignalRegistry.DefaultSignal.answerRequest;
import static org.nextrtc.server.domain.signal.SignalRegistry.DefaultSignal.created;
import static org.nextrtc.server.domain.signal.SignalRegistry.DefaultSignal.finalize;
import static org.nextrtc.server.domain.signal.SignalRegistry.DefaultSignal.left;
import static org.nextrtc.server.domain.signal.SignalRegistry.DefaultSignal.offerRequest;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.nextrtc.server.domain.Conversation;
import org.nextrtc.server.domain.Member;
import org.nextrtc.server.domain.Message;
import org.nextrtc.server.domain.signal.SignalResponse;
import org.nextrtc.server.exception.MemberNotFoundException;

public class DefaultConversation implements Conversation {

	private Set<Member> members = synchronizedSet(new HashSet<Member>());
	private String id;

	public DefaultConversation() {
		this.id = UUID.randomUUID().toString();
	}

	public DefaultConversation(String id) {
		if (StringUtils.isEmpty(id)) {
			throw new IllegalArgumentException("Conversation id must be set.");
		}
		this.id = id;
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
				.withMember(leaving)//
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
				.withMember(member)//
				.build();

		SignalResponse broadcast = broadcast(message);

		members.add(member);
		return broadcast;
	}

	public SignalResponse routeOffer(Member from, Message offer) {
		Member member = findById(offer.getMemberId());

		Message message = Message//
				.createWith(answerRequest)//
				.withMember(from)//
				.withContent(offer.getContent())//
				.build();

		return new SignalResponse(message, member);
	}

	public SignalResponse routeAnswer(Member from, Message answer) {
		Member member = findById(answer.getMemberId());

		Message message = Message//
				.createWith(finalize)//
				.withMember(from)//
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

	public boolean isEmpty() {
		return members.isEmpty();
	}

}
