package org.nextrtc.server.domain.provider;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.nextrtc.server.domain.Message.createWith;
import static org.nextrtc.server.domain.signal.DefaultSignal.answerRequest;
import static org.nextrtc.server.domain.signal.DefaultSignal.answerResponse;
import static org.nextrtc.server.domain.signal.DefaultSignal.created;
import static org.nextrtc.server.domain.signal.DefaultSignal.finalize;
import static org.nextrtc.server.domain.signal.DefaultSignal.left;
import static org.nextrtc.server.domain.signal.DefaultSignal.offerRequest;
import static org.nextrtc.server.domain.signal.DefaultSignal.offerResponse;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.nextrtc.server.domain.Conversation;
import org.nextrtc.server.domain.Member;
import org.nextrtc.server.domain.Message;
import org.nextrtc.server.domain.signal.Signal;
import org.nextrtc.server.domain.signal.SignalResponse;

public class BroadcastConversationTest {

	private Conversation conv;

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Before
	public void setupConversation() {
		conv = new BroadcastConversation();
	}

	@Test
	public void shouldNotAllowToAddNewMemberWithoutBroadcaster() {
		// given
		Member member = mock(Member.class);

		// then
		exception.expectMessage(containsString("This conversation doesn't have owner!"));

		// when
		conv.join(member);
	}

	@Test
	public void shouldAllowToAddNewMemberWhenBroadcasterIsPresent() {
		// given
		Member broadcaster = mock(Member.class);
		Member listener = mock(Member.class);
		conv.joinOwner(broadcaster);

		// when
		conv.join(listener);

		// then
		assertTrue(conv.has(broadcaster));
		assertTrue(conv.has(listener));
	}

	@Test
	public void shouldSendConversationNameToBroadcaster() {
		// given
		Member broadcaster = mock(Member.class);

		// when
		SignalResponse response = conv.joinOwner(broadcaster);

		// then
		Message message = response.getMessage();

		assertThat(message.getSignal(), is((Signal) created));
		assertThat(message.getContent(), is(conv.getId()));
		assertThat(response.getRecipients(), contains(broadcaster));
	}

	@Test
	public void shouldSendOfferRequestFromListenerToBroadcaster() {
		// given
		Member broadcaster = mock(Member.class);
		Member listener = mockMember("id", "Wladzio");
		conv.joinOwner(broadcaster);

		// when
		SignalResponse response = conv.join(listener);

		// then
		Message message = response.getMessage();

		assertThat(message.getSignal(), is((Signal) offerRequest));
		assertThat(message.getContent(), isEmptyOrNullString());
		assertThat(message.getMemberId(), is("id"));
		assertThat(message.getMemberName(), is("Wladzio"));
		assertThat(response.getRecipients(), contains(broadcaster));
		assertThat(response.getRecipients(), not(contains(listener)));
	}

	@Test
	public void shouldSendOfferRequestFromListenerToBroadcasterOnly() {
		// given
		Member broadcaster = mock(Member.class);
		Member existing = mockMember("id", "Wladzio");
		Member joining = mockMember("joining", "Stefan");

		conv.joinOwner(broadcaster);
		conv.join(existing);

		// when
		SignalResponse response = conv.join(joining);

		// then
		Message message = response.getMessage();

		assertThat(message.getSignal(), is((Signal) offerRequest));
		assertThat(message.getContent(), isEmptyOrNullString());
		assertThat(message.getMemberId(), is("joining"));
		assertThat(message.getMemberName(), is("Stefan"));
		assertThat(response.getRecipients(), contains(broadcaster));
		assertThat(response.getRecipients(), not(contains(existing)));
		assertThat(response.getRecipients(), not(contains(joining)));
	}

	@Test
	public void shouldSendAnswerRequestOnOfferResponseWithValidSenderAndReceiver() {
		// given
		Member mock = mock(Member.class);
		Member broadcaster = mockMember("id", "Wladzio");
		Member joining = mockMember("joining", "Stefan");

		conv.joinOwner(broadcaster);
		conv.join(joining);
		conv.join(mock);

		// when
		SignalResponse response = conv.routeOffer(broadcaster, //
				createWith(offerResponse)//
						.withMember(joining)//
						.withContent("local Wladzio sdp")//
						.build());

		// then
		Message message = response.getMessage();

		assertThat(message.getSignal(), is((Signal) answerRequest));
		assertThat(message.getContent(), is("local Wladzio sdp"));
		assertThat(message.getMemberId(), is("id"));
		assertThat(message.getMemberName(), is("Wladzio"));
		assertThat(response.getRecipients(), contains(joining));
		assertThat(response.getRecipients(), not(contains(mock)));
	}

	@Test
	public void shouldSendOnOfferResponseWithValidSenderAndReceiver() {
		// given
		Member mock = mock(Member.class);
		Member broadcaster = mockMember("id", "Wladzio");
		Member joining = mockMember("joining", "Stefan");

		conv.joinOwner(broadcaster);
		conv.join(joining);
		conv.join(mock);

		// when
		SignalResponse response = conv.routeAnswer(joining, //
				createWith(answerResponse)//
						.withMember(broadcaster)//
						.withContent("local Stefan sdp")//
						.build());

		// then
		Message message = response.getMessage();

		assertThat(message.getSignal(), is((Signal) finalize));
		assertThat(message.getContent(), is("local Stefan sdp"));
		assertThat(message.getMemberId(), is("joining"));
		assertThat(message.getMemberName(), is("Stefan"));
		assertThat(response.getRecipients(), contains(broadcaster));
		assertThat(response.getRecipients(), not(contains(joining)));
		assertThat(response.getRecipients(), not(contains(mock)));
	}

	@Test
	public void shouldInformBroadcasterAboutSomeonesLeave() {
		// given
		Member broadcaster = mockMember("id", "Wladzio");
		Member member = mockMember("id2", "Piotr");
		Member leaving = mockMember("leaving", "Karolina");
		conv.joinOwner(broadcaster);
		conv.join(member);
		conv.join(leaving);

		// when
		SignalResponse response = conv.disconnect(leaving);

		// then
		Message message = response.getMessage();

		assertThat(message.getSignal(), is((Signal) left));
		assertThat(message.getContent(), isEmptyOrNullString());
		assertThat(message.getMemberId(), is("leaving"));
		assertThat(message.getMemberName(), is("Karolina"));
		assertThat(response.getRecipients(), contains(broadcaster));
		assertThat(response.getRecipients(), not(contains(member)));

	}

	private Member mockMember(String id, String name) {
		Member mock = mock(Member.class);
		when(mock.getId()).thenReturn(id);
		when(mock.getName()).thenReturn(name);
		return mock;
	}

}
