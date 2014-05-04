package org.nextrtc.server.domain;

import static java.util.Collections.synchronizedList;
import static org.nextrtc.server.domain.signal.DefaultSignals.answerRequest;
import static org.nextrtc.server.domain.signal.DefaultSignals.finalize;
import static org.nextrtc.server.domain.signal.DefaultSignals.offerRequest;

import java.util.LinkedList;
import java.util.List;

import javax.websocket.Session;

import org.nextrtc.server.exception.MemberNotFoundException;

public class Conversation {

	private List<Member> members = synchronizedList(new LinkedList<Member>());
	private String id;

	public Conversation(String id, Session ownerSession) {
	}

	public String getId() {
		return id;
	}

	public SignalResponse broadcast(Message message) {
		SignalResponse signalResponse = new SignalResponse(message);
		signalResponse.addAll(members);
		return signalResponse;
	}

	public void disconnect(Member member) {
		// TODO Auto-generated method stub

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

	private Member findById(String memberId) {
		for (Member member : members) {
			if (memberId.equals(member.getId())) {
				return member;
			}
		}
		throw new MemberNotFoundException();
	}

}
