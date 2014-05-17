package org.nextrtc.server.domain.signal;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.nextrtc.server.domain.Message;
import org.nextrtc.server.domain.signal.SenderRequest;

public class SenderRequestTest {

	@Rule
	public ExpectedException exeption = ExpectedException.none();

	@Test
	public void shouldNotAllowToCreateRequestWithNullMessage() {
		// given

		// then
		exeption.expect(IllegalArgumentException.class);

		// when
		new SenderRequest(null);
	}

	@Test
	public void shouldAllowCreateRequestWithMessage() {
		// given
		Message message = mock(Message.class);

		// then
		SenderRequest request = new SenderRequest(message);

		// when
		assertThat(request.getMessage(), is(message));
	}

	@Test
	public void shouldNotAddNullSessionAsReceiver() {
		// given
		Message message = mock(Message.class);
		SenderRequest request = new SenderRequest(message);

		// then
		request.add(null);

		// when
		assertThat(request.getSessions().size(), is(0));
	}

}
