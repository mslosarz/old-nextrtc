package org.nextrtc.server.domain.signal;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.nextrtc.server.domain.Member;
import org.nextrtc.server.domain.Message;

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
	public void shouldNotAllowCreateRequestWithMessage() {
		// given
		Message message = mock(Message.class);

		// then
		SignalResponse response = new SignalResponse(message);

		// when
		assertThat(response.getRecipients().size(), is(0));
	}

	@Test
	public void shouldAddNotNullRecipient() {
		// given
		Message message = mock(Message.class);
		Member member = mock(Member.class);
		SignalResponse response = new SignalResponse(message);

		// then
		response.add(message, member);

		// when
		assertThat(response.getRecipients().size(), is(1));
	}

	@Test
	public void shouldAddAllRecipient() {
		// given
		Message message = mock(Message.class);
		Message message2 = mock(Message.class);
		Member member = mock(Member.class);
		SignalResponse response = new SignalResponse(message);

		// then
		response.add(message, member);
		response.add(message2, member);

		// when
		Map<Message, List<Member>> recipients = response.getRecipients();
		assertThat(recipients.size(), is(2));
		assertThat(recipients.keySet().size(), is(2));
		assertThat(recipients.keySet(), containsInAnyOrder(message, message2));
		assertThat(recipients.get(message), contains(member));
		assertThat(recipients.get(message2), contains(member));
	}

	@Test
	public void shouldNotAddNullRecipient() {
		// given
		Message message = mock(Message.class);
		SignalResponse response = new SignalResponse(message);

		// then
		response.add(message, null);

		// when
		assertThat(response.getRecipients().size(), is(1));
		assertThat(response.getRecipients().get(message).size(), is(0));

	}

	@Test
	public void emptyImplementationShouldNotAllowToAddRecipients() {
		// given
		Message message = mock(Message.class);
		Member member = mock(Member.class);
		SignalResponse empty = SignalResponse.EMPTY;

		// when
		empty.add(message, member);

		// then
		assertThat(empty.getRecipients().size(), is(0));
	}

}
