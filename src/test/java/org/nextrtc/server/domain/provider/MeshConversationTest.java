package org.nextrtc.server.domain.provider;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertNotNull;
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

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.nextrtc.server.domain.Conversation;
import org.nextrtc.server.domain.Member;
import org.nextrtc.server.domain.Message;
import org.nextrtc.server.domain.signal.DefaultSignal;
import org.nextrtc.server.domain.signal.Signal;
import org.nextrtc.server.domain.signal.SignalResponse;
import org.nextrtc.server.exception.MemberNotFoundException;

public class MeshConversationTest {

	private Conversation conv;

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Before
	public void setupConversation() {
		conv = new MeshConversation();
	}

	@Test
	public void shouldHaveOwhId() {
		// given
		Conversation c1 = new MeshConversation();
		Conversation c2 = new MeshConversation();

		// when
		String id1 = c1.getId();
		String id2 = c2.getId();

		// then
		assertNotNull(id1);
		assertNotNull(id2);
		assertThat(id1, is(not(id2)));
	}

	@Test
	public void shouldAllowToAddNewMember() {
		// given
		Member member = mock(Member.class);

		// when
		conv.join(member);

		// then
		assertTrue(conv.has(member));
	}

	@Test
	public void shouldSendConversationCreatedForJoinOwnerToConversation() {
		// given
		Member owner = mock(Member.class);

		// when
		SignalResponse response = conv.joinOwner(owner);

		// then
		Map<Message, List<Member>> recipients = response.getRecipients();
		Message message = recipients.keySet().iterator().next();

		assertThat(message.getSignal(), is((Signal) created));
		assertThat(message.getContent(), is(conv.getId()));
		assertThat(recipients.get(message), contains(owner));
	}

	@Test
	public void shouldSendOfferRequestFromNewMemberToOwner() {
		// given
		Member owner = mock(Member.class);
		Member member = mockMember("id", "Wladzio");
		conv.joinOwner(owner);

		// when
		SignalResponse response = conv.join(member);

		// then
		Message message = fetchMessageFromRequest(response, offerRequest);
		assertThat(message.getContent(), isEmptyOrNullString());
		assertThat(message.getMemberId(), is("id"));
		assertThat(message.getMemberName(), is("Wladzio"));

		Map<Message, List<Member>> recipients = response.getRecipients();
		assertThat(recipients.get(message), contains(owner));
		assertThat(recipients.get(message), not(contains(member)));
	}

	@Test
	public void shouldSendOfferRequestFromNewMemberToOwnerAndOtherMembers() {
		// given
		Member owner = mock(Member.class);
		Member member = mockMember("id", "Wladzio");
		Member joining = mockMember("joining", "Stefan");

		conv.joinOwner(owner);
		conv.join(member);

		// when
		SignalResponse response = conv.join(joining);

		// then
		Message message = fetchMessageFromRequest(response, offerRequest);
		assertThat(message.getContent(), isEmptyOrNullString());
		assertThat(message.getMemberId(), is("joining"));
		assertThat(message.getMemberName(), is("Stefan"));

		Map<Message, List<Member>> recipients = response.getRecipients();
		assertThat(recipients.get(message), containsInAnyOrder(member, owner));
		assertThat(recipients.get(message), not(contains(joining)));
	}

	@Test
	public void shouldSendAnswerRequestOnOfferResponseWithValidSenderAndReceiver() {
		// given
		Member mock = mock(Member.class);
		Member member = mockMember("id", "Wladzio");
		Member joining = mockMember("joining", "Stefan");

		conv.joinOwner(member);
		conv.join(joining);
		conv.join(mock);

		// when
		SignalResponse response = conv.routeOffer(member, //
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
	}

	@Test
	public void shouldSendOnOfferResponseWithValidSenderAndReceiver() {
		// given
		Member mock = mock(Member.class);
		Member member = mockMember("id", "Wladzio");
		Member joining = mockMember("joining", "Stefan");

		conv.joinOwner(member);
		conv.join(joining);
		conv.join(mock);

		// when
		SignalResponse response = conv.routeAnswer(joining, //
				createWith(answerResponse)//
						.withMember(member)//
						.withContent("local Stefan sdp")//
						.build());

		// then
		Map<Message, List<Member>> recipients = response.getRecipients();
		Message message = recipients.keySet().iterator().next();

		assertThat(message.getSignal(), is((Signal) finalize));
		assertThat(message.getContent(), is("local Stefan sdp"));
		assertThat(message.getMemberId(), is("joining"));
		assertThat(message.getMemberName(), is("Stefan"));
		assertThat(recipients.get(message), contains(member));
	}

	@Test
	public void shouldThrowExceptionIfInvalidMemberIdOccurOnRequest() {
		// given
		Member member = mockMember("id", "Wladzio");
		Member invalid = mockMember("invalid", "Andrzej");
		conv.joinOwner(member);

		// then
		exception.expect(MemberNotFoundException.class);

		// when
		conv.routeAnswer(member, //
				createWith(answerResponse)//
						.withMember(invalid)//
						.withContent("local Wladzio sdp")//
						.build());
	}

	@Test
	public void shouldInformEveryoneOfSomeoneLeave() {
		// given
		Member member = mockMember("id", "Wladzio");
		Member member2 = mockMember("id2", "Piotr");
		Member leaving = mockMember("leaving", "Karolina");
		conv.joinOwner(member);
		conv.join(member2);
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
		assertThat(recipients.get(message), containsInAnyOrder(member, member2));
	}

	private Member mockMember(String id, String name) {
		Member mock = mock(Member.class);
		when(mock.getId()).thenReturn(id);
		when(mock.getName()).thenReturn(name);
		return mock;
	}

	private Message fetchMessageFromRequest(SignalResponse response, DefaultSignal signal) {
		Message message = null;
		for (Message msg : response.getRecipients().keySet()) {
			if (msg.getSignal().equals(signal)) {
				message = msg;
				break;
			}
		}
		return message;
	}

}
