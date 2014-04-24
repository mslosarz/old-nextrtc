package org.nextrtc.signaling.domain;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.nextrc.signaling.domain.Message.create;
import static org.nextrc.signaling.domain.Operations.joinConversation;
import static org.nextrc.signaling.domain.Operations.mediaAnswer;
import static org.nextrc.signaling.domain.Operations.mediaOffer;
import static org.nextrc.signaling.domain.Operations.newConversation;

import javax.websocket.RemoteEndpoint.Async;
import javax.websocket.Session;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Test;
import org.mockito.Mockito;
import org.nextrc.signaling.domain.Conversation;
import org.nextrc.signaling.domain.Message;
import org.nextrc.signaling.domain.Operations;

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

		verify(ownerRemote).sendObject(Mockito.argThat(new MatchOperationAndContent(mediaAnswer, "join")));
		verify(joiningRemote).sendObject(Mockito.argThat(new MatchOperationAndContent(mediaOffer, "owner")));

	}

	private Message createConversationRequest(Operations op, String content) {
		Session member = mock(Session.class);
		Async async = mock(Async.class);
		when(member.getAsyncRemote()).thenReturn(async);

		Message request = create()//
				.withOperation(op)//
				.withContent(content)//
				.withSession(member)//
				.build();
		return request;
	}

}

class MatchOperationAndContent extends BaseMatcher<Message> {
	private String content;
	private Operations operation;

	public MatchOperationAndContent(Operations operation, String content) {
		this.content = content;
		this.operation = operation;
	}

	@Override
	public boolean matches(Object object) {
		Message msg = (Message) object;
		return msg.getContent().equals(content) && msg.getOperationAsEnum() == operation;
	}

	@Override
	public void describeTo(Description arg0) {
	}
}
