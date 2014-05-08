package org.nextrtc.server.domain;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SignalResponseTest {

	@Rule
	public ExpectedException exeption = ExpectedException.none();

	@Test
	public void shouldNotAllowToCreateResponseWithNullMessage() {
		// given

		// then
		exeption.expect(IllegalArgumentException.class);

		// when
		new SignalResponse(null);
	}

	@Test
	public void shouldAllowCreateRequestWithMessage() {
		// given
		Message message = mock(Message.class);

		// then
		SignalResponse response = new SignalResponse(message);

		// when
		assertThat(response.getMessage(), is(message));
	}

	@Test
	public void shouldAddNotNullRecipient() {
		// given
		Message message = mock(Message.class);
		Member member = mock(Member.class);
		SignalResponse response = new SignalResponse(message);

		// then
		response.add(member);

		// when
		assertThat(response.getRecipients().size(), is(1));
	}

	@Test
	public void shouldAddAllRecipient() {
		// given
		Message message = mock(Message.class);
		Member member = mock(Member.class);
		SignalResponse response = new SignalResponse(message);

		// then
		response.addAll(asList(member));

		// when
		assertThat(response.getRecipients().size(), is(1));
	}

	@Test
	public void shouldNotAddNullRecipient() {
		// given
		Message message = mock(Message.class);
		SignalResponse response = new SignalResponse(message);

		// then
		response.add(null);

		// when
		assertThat(response.getRecipients().size(), is(0));
	}

	@Test
	public void emptyImplementationShouldNotAllowToAddRecipients() {
		// given
		Member member = mock(Member.class);
		SignalResponse empty = SignalResponse.EMPTY;

		// when
		empty.add(member);

		// then
		assertThat(empty.getRecipients().size(), is(0));
	}

	@Test
	public void emptyImplementationShouldNotAllowToAddAllRecipients() {
		// given
		Member member = mock(Member.class);
		SignalResponse empty = SignalResponse.EMPTY;

		// when
		empty.addAll(asList(member));

		// then
		assertThat(empty.getRecipients().size(), is(0));
	}

}
