package org.nextrtc.server.domain.provider;

import static com.google.common.base.Preconditions.checkArgument;
import static org.nextrtc.server.domain.signal.DefaultSignal.answerRequest;
import static org.nextrtc.server.domain.signal.DefaultSignal.created;
import static org.nextrtc.server.domain.signal.DefaultSignal.finalize;
import static org.nextrtc.server.domain.signal.DefaultSignal.joined;
import static org.nextrtc.server.domain.signal.DefaultSignal.left;
import static org.nextrtc.server.domain.signal.DefaultSignal.offerRequest;

import java.util.Collection;

import org.nextrtc.server.domain.Conversation;
import org.nextrtc.server.domain.Member;
import org.nextrtc.server.domain.Message;
import org.nextrtc.server.domain.signal.SignalResponse;

import com.google.common.base.Optional;

public class BroadcastConversation extends AbstractConversation implements Conversation {

	private Optional<Member> owner = Optional.<Member> absent();

	public BroadcastConversation() {
		super();
	}

	public BroadcastConversation(String id) {
		super(id);
	}

	@Override
	public synchronized SignalResponse joinOwner(Member owner) {
		this.owner = Optional.<Member> of(owner);

		Message response = Message//
				.createWith(created)//
				.withContent(id)//
				.build();//

		return new SignalResponse(response, owner);
	}

	@Override
	public synchronized SignalResponse join(Member member) {
		checkArgument(owner.isPresent(), "This conversation doesn't have owner!");
		members.add(member);

		SignalResponse signalResponse = new SignalResponse(Message//
				.createWith(offerRequest)//
				.withMember(member)//
				.build(), owner.get());
		signalResponse.add(Message//
				.createWith(joined)//
				.withContent(getId())//
				.build(), member);

		return signalResponse;
	}

	@Override
	public SignalResponse routeOffer(Member from, Message offer) {
		Member to = findById(offer.getMemberId());

		Message message = Message//
				.createWith(answerRequest)//
				.withMember(owner.get())//
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

		return new SignalResponse(message, owner.get());
	}

	@Override
	public SignalResponse disconnect(Member leaving) {
		members.remove(leaving);
		if (owner.get().equals(leaving)) {
			SignalResponse informAll = informAll();
			owner = Optional.<Member> absent();
			members.clear();
			return informAll;
		}
		return informOwnerAbout(leaving);
	}

	@Override
	public boolean has(Member member) {
		return super.has(member) || isOwner(member);
	}

	@Override
	public Collection<Member> members() {
		Collection<Member> members = super.members();
		for (Member member : owner.asSet()) {
			members.add(member);
		}
		return members;
	}

	private boolean isOwner(Member member) {
		if (owner.isPresent()) {
			return owner.get().equals(member);
		}
		return false;
	}

	private SignalResponse informOwnerAbout(Member leaving) {
		return new SignalResponse(Message//
				.createWith(left)//
				.withMember(leaving)//
				.build(), owner.get());
	}

	private SignalResponse informAll() {
		return broadcast(Message//
				.createWith(left)//
				.withMember(owner.get())//
				.build());
	}

}
