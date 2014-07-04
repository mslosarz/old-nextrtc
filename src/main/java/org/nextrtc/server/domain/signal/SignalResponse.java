package org.nextrtc.server.domain.signal;

import static java.util.Arrays.asList;
import static org.nextrtc.server.domain.signal.DefaultSignal.finalize;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.nextrtc.server.domain.Member;
import org.nextrtc.server.domain.Message;

public class SignalResponse {

	private Map<Message, List<Member>> recipients = new HashMap<>();

	public SignalResponse(Message message, Member... members) {
		this(message, asList(members));
	}

	public SignalResponse(Message message, Collection<Member> members) {
		if (message == null) {
			throw new IllegalArgumentException("Message has to be set!");
		}
		addAll(message, members);
	}

	public void add(Message message, Member member) {
		if (recipients.containsKey(message) == false) {
			recipients.put(message, new LinkedList<Member>());
		}
		if (member != null) {
			recipients.get(message).add(member);
		}
	}

	private void addAll(Message message, Collection<Member> members) {
		for (Member member : members) {
			add(message, member);
		}
	}

	public Map<Message, List<Member>> getRecipients() {
		return recipients;
	}

	/**
	 * This implementation disallow add recipient. This message wont be send to
	 * any member
	 */
	public static final SignalResponse EMPTY = new SignalResponse(Message.createWith(finalize).build()) {
		public void add(Message message, Member member) {
		};
	};
}
