package org.nextrtc.signaling;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.nextrtc.signaling.domain.Message.create;
import static org.nextrtc.signaling.domain.Signals.conversationCreated;
import static org.nextrtc.signaling.domain.Signals.newConversation;

import javax.websocket.RemoteEndpoint.Async;
import javax.websocket.Session;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.nextrtc.signaling.domain.Conversation;
import org.nextrtc.signaling.domain.ConversationContainer;
import org.nextrtc.signaling.domain.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(classes = Config.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class ConversationContainerTest {

	@Autowired
	private ConversationContainer container;

	@Test
	public void shouldAllowToCreateNewConversation() {
		// given
		Message conversationRequest = createConversationRequest();
		Async async = conversationRequest.getAsyncRemote();

		// when
		Message response = container.createNewConversation(conversationRequest);

		// then
		assertNotNull(response);
		assertThat(response.getSignal(), equalTo(conversationCreated.name()));
		assertNotNull("Conversation ID has to be returned", response.getConversationId());
		verify(async).sendObject(Mockito.anyObject());
	}

	@Test
	public void shouldAllowToFindConversationByID() {
		// given
		String conversationId = container.createNewConversation(createConversationRequest()).getConversationId();

		// when
		Conversation conversation = container.findConversationById(conversationId);

		// then
		assertNotNull(conversation);
	}

	@Test
	public void shouldRemoveMemberWithGivenSession() {
		// given
		Message request = createConversationRequest();
		Session sessionToRemove = request.getSession();

		String conversationId = container.createNewConversation(request).getConversationId();
		Conversation conversation = container.findConversationById(conversationId);
		assertThat(conversation.members(), is(1));

		// when
		container.disconnectAllMembersWith(sessionToRemove);

		// then
		assertThat(conversation.members(), is(0));
	}

	private Message createConversationRequest() {
		Session session = mock(Session.class);
		Async async = mock(Async.class);
		when(session.getAsyncRemote()).thenReturn(async);
		return create()//
				.withSignal(newConversation)//
				.withContent("sdp")//
				.withSession(session)//
				.build();
	}
}
