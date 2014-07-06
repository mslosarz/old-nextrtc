package org.nextrtc.server.domain.provider;

import static org.nextrtc.server.domain.signal.DefaultSignal.answerRequest;
import static org.nextrtc.server.domain.signal.DefaultSignal.created;
import static org.nextrtc.server.domain.signal.DefaultSignal.finalize;
import static org.nextrtc.server.domain.signal.DefaultSignal.joined;
import static org.nextrtc.server.domain.signal.DefaultSignal.left;
import static org.nextrtc.server.domain.signal.DefaultSignal.offerRequest;

import org.nextrtc.server.domain.Conversation;
import org.nextrtc.server.domain.Member;
import org.nextrtc.server.domain.Message;
import org.nextrtc.server.domain.signal.SignalResponse;

public class MeshConversation extends AbstractConversation implements Conversation {

	public MeshConversation() {
		super();
	}

	public MeshConversation(String id) {
		super(id);
	}

	@Override
	public synchronized SignalResponse joinOwner(Member owner) {
		members.add(owner);

		Message response = Message//
				.createWith(created)//
				.withContent(id)//
				.build();//

		return broadcast(response);
	}

	public synchronized SignalResponse join(Member member) {

		SignalResponse broadcast = broadcast(Message//
				.createWith(offerRequest)//
				.withMember(member)//
				.build());
		broadcast.add(Message//
				.createWith(joined)//
				.withContent(getId())//
				.build(), member);

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

	@Override
	public SignalResponse disconnect(Member leaving) {
		members.remove(leaving);
		return broadcast(Message//
				.createWith(left)//
				.withMember(leaving)//
				.build());
	}

}
