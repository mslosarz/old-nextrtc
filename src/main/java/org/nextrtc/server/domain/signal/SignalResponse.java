package org.nextrtc.server.domain.signal;

import static org.nextrtc.server.domain.signal.DefaultSignal.finalize;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.nextrtc.server.domain.Member;
import org.nextrtc.server.domain.Message;

public class SignalResponse {

	private Message message;

	private Set<Member> recipients = new HashSet<>();

	public SignalResponse(Message message) {
		if (message == null) {
			throw new IllegalArgumentException("Message has to be set!");
		}
		this.message = message;
	}

	public SignalResponse(Message message, Member member) {
		this(message);
		add(member);
	}

	public void add(Member member) {
		if (member != null) {
			recipients.add(member);
		}
	}

	public void addAll(Collection<Member> members) {
		recipients.addAll(members);
	}

	public Message getMessage() {
		return message;
	}

	public Set<Member> getRecipients() {
		return recipients;
	}

	/**
	 * This implementation disallow add recipient. This message wont be send to
	 * any member
	 */
	public static final SignalResponse EMPTY = new SignalResponse(Message.createWith(finalize).build()) {
		public void add(Member member) {
		};

		public void addAll(java.util.Collection<Member> members) {
		};
	};
}
