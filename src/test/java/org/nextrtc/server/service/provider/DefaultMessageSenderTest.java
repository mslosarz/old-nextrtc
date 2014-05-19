package org.nextrtc.server.service.provider;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.websocket.RemoteEndpoint.Async;
import javax.websocket.Session;

import org.junit.Test;
import org.nextrtc.server.domain.Message;
import org.nextrtc.server.domain.signal.SenderRequest;
import org.nextrtc.server.service.MessageSender;
import org.nextrtc.server.service.provider.DefaultMessageSender;


public class DefaultMessageSenderTest {

	MessageSender sender = new DefaultMessageSender();

	@Test
	public void shouldWorkForNullRequest() {
		// given
		SenderRequest request = null;

		// when
		sender.send(request);

		// then
	}

	@Test
	public void shouldWorkForExistingRequestWithMessage() {
		// given
		SenderRequest request = new SenderRequest(mock(Message.class));

		// when
		sender.send(request);

		// then
	}

	@Test
	public void shouldSendMessageToAddedSession() {
		// given
		Message message = mock(Message.class);
		Session session = mockSession();
		SenderRequest request = setupRequest(message, session);

		// when
		sender.send(request);

		// then
		verify(session.getAsyncRemote()).sendObject(message);
	}

	@Test
	public void shouldSendMessageToMoreThanOneSession() {
		// given
		Message message = mock(Message.class);
		Session session1 = mockSession();
		Session session2 = mockSession();
		SenderRequest request = setupRequest(message, session1, session2);

		// when
		sender.send(request);

		// then
		verify(session1.getAsyncRemote()).sendObject(message);
		verify(session2.getAsyncRemote()).sendObject(message);
	}

	private SenderRequest setupRequest(Message message, Session... sessions) {
		SenderRequest request = new SenderRequest(message);
		for (Session session : sessions) {
			request.add(session);
		}
		return request;
	}

	private Session mockSession() {
		Session session = mock(Session.class);
		Async async = mock(Async.class);
		when(session.getAsyncRemote()).thenReturn(async);
		return session;
	}

}
