package org.nextrtc.server.domain;

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
import static org.nextrtc.server.domain.signal.DefaultSignals.answerRequest;
import static org.nextrtc.server.domain.signal.DefaultSignals.answerResponse;
import static org.nextrtc.server.domain.signal.DefaultSignals.created;
import static org.nextrtc.server.domain.signal.DefaultSignals.finalize;
import static org.nextrtc.server.domain.signal.DefaultSignals.left;
import static org.nextrtc.server.domain.signal.DefaultSignals.offerRequest;
import static org.nextrtc.server.domain.signal.DefaultSignals.offerResponse;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.nextrtc.server.domain.signal.Signal;
import org.nextrtc.server.exception.MemberNotFoundException;

public class DefaultConversationTest {

	private Conversation conv;

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Before
	public void setupConversation() {
		conv = new DefaultConversation();
	}

	@Test
	public void shouldHaveOwhId() {
		// given
		Conversation c1 = new DefaultConversation();
		Conversation c2 = new DefaultConversation();

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
		Message message = response.getMessage();

		assertThat(message.getSignal(), is((Signal) created));
		assertThat(message.getContent(), is(conv.getId()));
		assertThat(response.getRecipients(), contains(owner));
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
		Message message = response.getMessage();

		assertThat(message.getSignal(), is((Signal) offerRequest));
		assertThat(message.getContent(), isEmptyOrNullString());
		assertThat(message.getMemberId(), is("id"));
		assertThat(message.getMemberName(), is("Wladzio"));
		assertThat(response.getRecipients(), contains(owner));
		assertThat(response.getRecipients(), not(contains(member)));
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
		Message message = response.getMessage();

		assertThat(message.getSignal(), is((Signal) offerRequest));
		assertThat(message.getContent(), isEmptyOrNullString());
		assertThat(message.getMemberId(), is("joining"));
		assertThat(message.getMemberName(), is("Stefan"));
		assertThat(response.getRecipients(), containsInAnyOrder(member, owner));
		assertThat(response.getRecipients(), not(contains(joining)));
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
						.member(joining)//
						.withContent("local Wladzio sdp")//
						.build());

		// then
		Message message = response.getMessage();

		assertThat(message.getSignal(), is((Signal) answerRequest));
		assertThat(message.getContent(), is("local Wladzio sdp"));
		assertThat(message.getMemberId(), is("id"));
		assertThat(message.getMemberName(), is("Wladzio"));
		assertThat(response.getRecipients(), contains(joining));
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
						.member(member)//
						.withContent("local Stefan sdp")//
						.build());

		// then
		Message message = response.getMessage();

		assertThat(message.getSignal(), is((Signal) finalize));
		assertThat(message.getContent(), is("local Stefan sdp"));
		assertThat(message.getMemberId(), is("joining"));
		assertThat(message.getMemberName(), is("Stefan"));
		assertThat(response.getRecipients(), contains(member));
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
						.member(invalid)//
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
		Message message = response.getMessage();

		assertThat(message.getSignal(), is((Signal) left));
		assertThat(message.getContent(), isEmptyOrNullString());
		assertThat(message.getMemberId(), is("leaving"));
		assertThat(message.getMemberName(), is("Karolina"));
		assertThat(response.getRecipients(), containsInAnyOrder(member, member2));
	}

	private Member mockMember(String id, String name) {
		Member mock = mock(Member.class);
		when(mock.getId()).thenReturn(id);
		when(mock.getName()).thenReturn(name);
		return mock;
	}

}
