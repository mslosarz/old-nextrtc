package org.nextrtc.server.domain.signal;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import javax.websocket.Session;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.nextrtc.server.domain.Message;

public class SenderRequestTest {

	@Rule
	public ExpectedException exeption = ExpectedException.none();

	@Test
	public void shouldAllowCreateRequestWithMessage() {
		// given
		Message message = mock(Message.class);
		Session session = mock(Session.class);
		
		// then
		SenderRequest request = new SenderRequest();
		request.add(message, session);

		// when
		assertThat(request.getSessions().size(), is(1));
		Message result = request.getSessions().keySet().iterator().next();
		assertThat(result, is(message));
		assertThat(request.getSessions().get(result).size(), is(1));
	}

	@Test
	public void shouldNotAddNullSessionAsReceiver() {
		// given
		Message message = mock(Message.class);

		// then
		SenderRequest request = new SenderRequest();
		request.add(message, null);

		// when
		assertThat(request.getSessions().size(), is(1));
		Message result = request.getSessions().keySet().iterator().next();
		assertThat(request.getSessions().get(result).size(), is(0));
	}

}
