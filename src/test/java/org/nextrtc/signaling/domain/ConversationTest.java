package org.nextrtc.signaling.domain;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.nextrc.signaling.domain.Message.create;
import static org.nextrc.signaling.domain.Signals.joinConversation;
import static org.nextrc.signaling.domain.Signals.mediaAnswer;
import static org.nextrc.signaling.domain.Signals.mediaOffer;
import static org.nextrc.signaling.domain.Signals.newConversation;

import javax.websocket.RemoteEndpoint.Async;
import javax.websocket.Session;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Test;
import org.mockito.Mockito;
import org.nextrc.signaling.domain.Conversation;
import org.nextrc.signaling.domain.Message;
import org.nextrc.signaling.domain.Signals;

public class ConversationTest {

	@Test
	public void should() {
		// given
		Message owner = createConversationRequest(newConversation, "owner");
		Async ownerRemote = owner.getAsyncRemote();
		Conversation conv = new Conversation(owner);

		Message join = createConversationRequest(joinConversation, "join");
		Async joiningRemote = join.getAsyncRemote();

		// when
		conv.join(join);

		// then

		verify(ownerRemote).sendObject(Mockito.argThat(new MatchSignalAndContent(mediaAnswer, "join")));
		verify(joiningRemote).sendObject(Mockito.argThat(new MatchSignalAndContent(mediaOffer, "owner")));

	}

	private Message createConversationRequest(Signals op, String content) {
		Session member = mock(Session.class);
		Async async = mock(Async.class);
		when(member.getAsyncRemote()).thenReturn(async);

		Message request = create()//
				.withSignal(op)//
				.withContent(content)//
				.withSession(member)//
				.build();
		return request;
	}

}

class MatchSignalAndContent extends BaseMatcher<Message> {
	private String content;
	private Signals signal;

	public MatchSignalAndContent(Signals signal, String content) {
		this.content = content;
		this.signal = signal;
	}

	@Override
	public boolean matches(Object object) {
		Message msg = (Message) object;
		return msg.getContent().equals(content) && msg.getSignalAsEnum() == signal;
	}

	@Override
	public void describeTo(Description arg0) {
	}
}
