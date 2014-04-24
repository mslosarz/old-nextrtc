package org.nextrtc.signaling;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.nextrc.signaling.Operations.conversationCreated;
import static org.nextrc.signaling.Operations.newConversation;
import static org.nextrc.signaling.domain.Message.create;

import javax.websocket.RemoteEndpoint.Async;
import javax.websocket.Session;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.nextrc.signaling.SignalingEndpoint;
import org.nextrc.signaling.domain.Conversation;
import org.nextrc.signaling.domain.ConversationContainer;
import org.nextrc.signaling.domain.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
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
		Async async = conversationRequest.getSession().getAsyncRemote();

		// when
		Message response = container.createNewConversation(conversationRequest);

		// then
		assertNotNull(response);
		assertThat(response.getOperation(), equalTo(conversationCreated.name()));
		assertNotNull("Conversation ID has to be returned", response.getContent());
		verify(async).sendObject(Mockito.anyObject());
	}

	@Test
	public void shouldAllowToFindConversationByID() {
		// given
		String conversationId = container.createNewConversation(createConversationRequest()).getContent();

		// when
		Conversation conversation = container.findConversationById(conversationId);

		// then
		assertNotNull(conversation);
	}

	private Message createConversationRequest() {
		Session session = mock(Session.class);
		Async async = mock(Async.class);
		when(session.getAsyncRemote()).thenReturn(async);
		return create()//
				.withOperation(newConversation)//
				.withContent("sdp")//
				.withSession(session)//
				.build();
	}
}

@Configuration
@ComponentScan(basePackageClasses = SignalingEndpoint.class)
class Config {
}
