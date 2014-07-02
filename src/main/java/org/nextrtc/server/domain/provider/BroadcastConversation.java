package org.nextrtc.server.domain.provider;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.nextrtc.server.domain.signal.DefaultSignal.answerRequest;
import static org.nextrtc.server.domain.signal.DefaultSignal.created;
import static org.nextrtc.server.domain.signal.DefaultSignal.finalize;
import static org.nextrtc.server.domain.signal.DefaultSignal.left;
import static org.nextrtc.server.domain.signal.DefaultSignal.offerRequest;

import org.nextrtc.server.domain.Conversation;
import org.nextrtc.server.domain.Member;
import org.nextrtc.server.domain.Message;
import org.nextrtc.server.domain.signal.SignalResponse;

public class BroadcastConversation extends AbstractConversation implements Conversation {

	private Member owner;

	public BroadcastConversation() {
		super();
	}

	public BroadcastConversation(String id) {
		super(id);
	}

	@Override
	public synchronized SignalResponse joinOwner(Member owner) {
		this.owner = owner;

		Message response = Message//
				.createWith(created)//
				.withContent(id)//
				.build();//

		return new SignalResponse(response, owner);
	}

	@Override
	public synchronized SignalResponse join(Member member) {
		checkNotNull(owner, "This conversation doesn't have owner!");

		Message message = Message//
				.createWith(offerRequest)//
				.withMember(member)//
				.build();

		members.add(member);

		return new SignalResponse(message, owner);
	}

	@Override
	public SignalResponse routeOffer(Member from, Message offer) {
		Member to = findById(offer.getMemberId());

		Message message = Message//
				.createWith(answerRequest)//
				.withMember(owner)//
				.withContent(offer.getContent())//
				.build();

		return new SignalResponse(message, to);
	}

	@Override
	public SignalResponse routeAnswer(Member from, Message answer) {
		Message message = Message//
				.createWith(finalize)//
				.withMember(from)//
				.withContent(answer.getContent())//
				.build();

		return new SignalResponse(message, owner);
	}

	@Override
	public SignalResponse disconnect(Member leaving) {
		members.remove(leaving);
		if (leaving.equals(owner)) {
			return informAll();
		}
		return informOwnerAbout(leaving);
	}

	@Override
	public boolean has(Member member) {
		boolean isOwner = false;
		if(member != null){
			isOwner = member.equals(owner);
		}
		return super.has(member) || isOwner;
	}

	private SignalResponse informOwnerAbout(Member leaving) {
		return new SignalResponse(Message//
				.createWith(left)//
				.withMember(leaving)//
				.build(), owner);
	}

	private SignalResponse informAll() {
		return broadcast(Message//
				.createWith(left)//
				.withMember(owner)//
				.build());
	}

}
