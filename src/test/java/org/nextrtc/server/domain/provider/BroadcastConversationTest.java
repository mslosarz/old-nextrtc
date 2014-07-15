package org.nextrtc.server.domain.provider;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.nextrtc.server.domain.Message.createWith;
import static org.nextrtc.server.domain.signal.DefaultSignal.answerRequest;
import static org.nextrtc.server.domain.signal.DefaultSignal.answerResponse;
import static org.nextrtc.server.domain.signal.DefaultSignal.created;
import static org.nextrtc.server.domain.signal.DefaultSignal.finalize;
import static org.nextrtc.server.domain.signal.DefaultSignal.left;
import static org.nextrtc.server.domain.signal.DefaultSignal.offerRequest;
import static org.nextrtc.server.domain.signal.DefaultSignal.offerResponse;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.nextrtc.server.domain.Conversation;
import org.nextrtc.server.domain.Member;
import org.nextrtc.server.domain.Message;
import org.nextrtc.server.domain.signal.Signal;
import org.nextrtc.server.domain.signal.SignalResponse;

public class BroadcastConversationTest extends AbstractConversationTest {

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
		Map<Message, List<Member>> recipients = response.getRecipients();
		Message message = recipients.keySet().iterator().next();

		assertThat(message.getSignal(), is((Signal) created));
		assertThat(message.getContent(), is(conv.getId()));
		assertThat(recipients.get(message), contains(broadcaster));
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
		Message message = fetchMessageFromRequest(response, offerRequest);
		assertThat(message.getContent(), isEmptyOrNullString());
		assertThat(message.getMemberId(), is("id"));
		assertThat(message.getMemberName(), is("Wladzio"));

		Map<Message, List<Member>> recipients = response.getRecipients();
		assertThat(recipients.get(message), contains(broadcaster));
		assertThat(recipients.get(message), not(contains(listener)));
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
		Message message = fetchMessageFromRequest(response, offerRequest);
		assertThat(message.getContent(), isEmptyOrNullString());
		assertThat(message.getMemberId(), is("joining"));
		assertThat(message.getMemberName(), is("Stefan"));

		Map<Message, List<Member>> recipients = response.getRecipients();
		assertThat(recipients.get(message), contains(broadcaster));
		assertThat(recipients.get(message), not(contains(existing)));
		assertThat(recipients.get(message), not(contains(joining)));
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
		Map<Message, List<Member>> recipients = response.getRecipients();
		Message message = recipients.keySet().iterator().next();

		assertThat(message.getSignal(), is((Signal) answerRequest));
		assertThat(message.getContent(), is("local Wladzio sdp"));
		assertThat(message.getMemberId(), is("id"));
		assertThat(message.getMemberName(), is("Wladzio"));
		assertThat(recipients.get(message), contains(joining));
		assertThat(recipients.get(message), not(contains(mock)));
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
		Map<Message, List<Member>> recipients = response.getRecipients();
		Message message = recipients.keySet().iterator().next();

		assertThat(message.getSignal(), is((Signal) finalize));
		assertThat(message.getContent(), is("local Stefan sdp"));
		assertThat(message.getMemberId(), is("joining"));
		assertThat(message.getMemberName(), is("Stefan"));
		assertThat(recipients.get(message), contains(broadcaster));
		assertThat(recipients.get(message), not(contains(joining)));
		assertThat(recipients.get(message), not(contains(mock)));
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
		Map<Message, List<Member>> recipients = response.getRecipients();
		Message message = recipients.keySet().iterator().next();

		assertThat(message.getSignal(), is((Signal) left));
		assertThat(message.getContent(), isEmptyOrNullString());
		assertThat(message.getMemberId(), is("leaving"));
		assertThat(message.getMemberName(), is("Karolina"));
		assertThat(recipients.get(message), contains(broadcaster));
		assertThat(recipients.get(message), not(contains(member)));

	}

	@Test
	public void shouldDisconnectAllListenersOnLeaveBroadcaster() {
		// given
		Member broadcaster = mockMember("id", "Wladzio");
		Member member = mockMember("id2", "Piotr");
		Member member2 = mockMember("leaving", "Karolina");
		conv.joinOwner(broadcaster);
		conv.join(member);
		conv.join(member2);

		// when
		SignalResponse response = conv.disconnect(broadcaster);

		// then
		Map<Message, List<Member>> recipients = response.getRecipients();
		Message message = recipients.keySet().iterator().next();

		assertThat(message.getSignal(), is((Signal) left));
		assertThat(message.getContent(), isEmptyOrNullString());
		assertThat(message.getMemberId(), is("id"));
		assertThat(message.getMemberName(), is("Wladzio"));
		assertThat(recipients.get(message), containsInAnyOrder(member, member2));
		assertFalse(conv.has(member2));
		assertFalse(conv.has(member));
		assertFalse(conv.has(broadcaster));
	}

	@Test
	public void shouldReturnOneMember() {
		Member broadcaster = mockMember("id", "Wladzio");
		conv.joinOwner(broadcaster);

		// when
		Collection<Member> members = conv.members();

		// then
		assertThat(members.size(), is(1));
	}

	@Test
	public void shouldReturnTwoMembers() {
		Member broadcaster = mockMember("id", "Wladzio");
		Member member = mockMember("id2", "Piotr");
		conv.joinOwner(broadcaster);
		conv.join(member);

		// when
		Collection<Member> members = conv.members();

		// then
		assertThat(members.size(), is(2));
	}

	@Test
	public void shouldNotModifyNumberOfMember() {
		Member broadcaster = mockMember("id", "Wladzio");
		Member member = mockMember("id2", "Piotr");
		conv.joinOwner(broadcaster);
		conv.join(member);
		Collection<Member> members = conv.members();

		// when
		members.remove(broadcaster);

		// then
		assertThat(members.size(), is(1));
		assertThat(conv.members().size(), is(2));
	}

}
