package org.nextrtc.signaling;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.nextrtc.signaling.domain.Signals.newConversation;

import javax.websocket.RemoteEndpoint.Async;
import javax.websocket.Session;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nextrtc.signaling.domain.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(classes = Config.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class SignalingEndpointTest {

	@Autowired
	private SignalingEndpoint endpoint;

	@Test
	public void shouldCreateSession() {
		// given
		Session session = mock(Session.class);
		Async async = mock(Async.class);
		when(session.getAsyncRemote()).thenReturn(async);
		Message message = Message.create()//
				.withSignal(newConversation)//
				.withContent("spd")//
				.withSession(session)//
				.build();

		// when
		endpoint.onMessage(message, session);

		// then
	}

}
