package org.nextrtc.signaling.domain;

import static java.util.Collections.synchronizedList;
import static org.nextrtc.signaling.domain.Signals.mediaAnswer;
import static org.nextrtc.signaling.domain.Signals.mediaOffer;

import java.util.LinkedList;
import java.util.List;

import javax.websocket.Session;

public class Conversation {

	private List<Member> members = synchronizedList(new LinkedList<>());

	public Conversation(Message ownerRequest) {
		members.add(new Member(ownerRequest));
	}

	public void join(Message message) {
		Member joining = new Member(message);
		for (Member member : members) {
			Message.create()//
					.withSessionFrom(joining)//
					.withSignal(mediaOffer)//
					.withContent(member.getMediaDescription())//
					.build().send();
			Message.create()//
					.withSessionFrom(member)//
					.withSignal(mediaAnswer)//
					.withContent(joining.getMediaDescription())//
					.build().send();
		}
		members.add(joining);
	}

	public void disconnect(Session session) {
		Member toRemove = null;
		for (Member member : members) {
			if (member.hasSession(session)) {
				toRemove = member;
				break;
			}
		}
		members.remove(toRemove);
	}

	public int members() {
		return members.size();
	}

}
