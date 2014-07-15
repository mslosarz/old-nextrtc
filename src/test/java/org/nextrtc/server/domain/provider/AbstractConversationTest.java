package org.nextrtc.server.domain.provider;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.nextrtc.server.domain.Member;
import org.nextrtc.server.domain.Message;
import org.nextrtc.server.domain.signal.DefaultSignal;
import org.nextrtc.server.domain.signal.SignalResponse;

public abstract class AbstractConversationTest {

	protected Member mockMember(String id, String name) {
		Member mock = mock(Member.class);
		when(mock.getId()).thenReturn(id);
		when(mock.getName()).thenReturn(name);
		return mock;
	}

	protected Message fetchMessageFromRequest(SignalResponse response, DefaultSignal signal) {
		Message message = null;
		for (Message msg : response.getRecipients().keySet()) {
			if (msg.getSignal().equals(signal)) {
				message = msg;
				break;
			}
		}
		return message;
	}

}
