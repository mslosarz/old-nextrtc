package org.nextrtc.server.domain;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class SignalResponse {

	private Message message;

	private Set<Member> recipients = new HashSet<>();

	public SignalResponse(Message message) {
		this.message = message;
	}

	public SignalResponse(Message message, Member member) {
		this(message);
		add(member);
	}

	public void add(Member member) {
		recipients.add(member);
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

	public static final SignalResponse EMPTY = new SignalResponse(null);
}
