package org.nextrtc.server.domain.signal;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.nextrtc.server.domain.signal.DefaultSignal.answerResponse;
import static org.nextrtc.server.domain.signal.DefaultSignal.create;
import static org.nextrtc.server.domain.signal.DefaultSignal.created;
import static org.nextrtc.server.domain.signal.DefaultSignal.join;
import static org.nextrtc.server.domain.signal.DefaultSignal.left;
import static org.nextrtc.server.domain.signal.DefaultSignal.offerResponse;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.nextrtc.server.dao.provider.InMemboryMembers;
import org.nextrtc.server.dao.provider.InMemoryConversations;
import org.nextrtc.server.domain.Conversation;
import org.nextrtc.server.domain.Member;
import org.nextrtc.server.domain.Message;
import org.nextrtc.server.domain.RequestContext;
import org.nextrtc.server.domain.provider.DefaultMember;
import org.nextrtc.server.exception.ConversationExists;
import org.nextrtc.server.factory.provider.DefaultConversationFactoryResolver;

public class DefaultSignalsTest {

	@Rule
	public ExpectedException exception = ExpectedException.none();

	private RequestContext context;

	@Before
	public void setupContext() {
		context = new RequestContext(new InMemoryConversations(), new InMemboryMembers(),
				new DefaultConversationFactoryResolver());
	}

	@Test
	public void shouldExecuteSignalCreate() {
		// given
		Member member = stubMember("Wladzio");
		Message message = Message.createWith(create).withMember(member).build();

		// when
		SignalResponse response = create.execute(member, message, context);

		// then
		assertNotNull(response);
	}

	@Test
	public void shouldUpdateMemberNameOnSignalCreate() {
		// given
		Member member = stubMember(null);
		Message message = Message.createWith(create).withMember(new DefaultMember(member.getId(), "Wladzio")).build();
		assertThat(member.getName(), isEmptyOrNullString());

		// when
		SignalResponse response = create.execute(member, message, context);

		// then
		assertNotNull(response);
		assertThat(member.getName(), is("Wladzio"));
	}

	@Test
	public void shouldExecuteSignalJoin() {
		// given
		Member member = stubMember(null);
		Conversation conv = stubConversation();
		Message message = Message.createWith(join)//
				.withMember(new DefaultMember(member.getId(), "Joining"))//
				.withContent(conv.getId())//
				.build();
		assertThat(member.getName(), isEmptyOrNullString());

		// when
		SignalResponse response = join.execute(member, message, context);

		// then
		assertNotNull(response);
	}

	@Test
	public void shouldUpdateMemberNameOnSignalJoin() {
		// given
		Member member = stubMember(null);
		Conversation conv = stubConversation();
		Message message = Message.createWith(join)//
				.withMember(new DefaultMember(member.getId(), "Joining"))//
				.withContent(conv.getId())//
				.build();
		assertThat(member.getName(), isEmptyOrNullString());

		// when
		SignalResponse response = join.execute(member, message, context);

		// then
		assertNotNull(response);
		assertThat(member.getName(), is("Joining"));
	}

	@Test
	public void shouldCreateConversationWhenRequestedJoinWithoutId() {
		// given
		Member member = stubMember(null);
		Message message = Message.createWith(join)//
				.withMember(new DefaultMember(member.getId(), "Creating"))//
				.withContent(null)//
				.build();

		// when
		SignalResponse response = join.execute(member, message, context);

		// then
		assertNotNull(response);
		Message responseMessage = response.getMessage();
		assertThat(responseMessage.getContent(), not(isEmptyOrNullString()));
		assertThat(responseMessage.getSignal(), is((Signal) created));
		assertThat(response.getRecipients(), contains(member));
	}

	@Test
	public void shouldCreateConversationWithGivenIdWhenConversationDoNotExists() {
		// given
		Member member = stubMember(null);
		Message message = Message.createWith(join)//
				.withMember(new DefaultMember(member.getId(), "Creating"))//
				.withContent("my-conversation-name")//
				.build();

		// when
		SignalResponse response = join.execute(member, message, context);

		// then
		assertNotNull(response);
		Message responseMessage = response.getMessage();
		assertThat(responseMessage.getContent(), is("my-conversation-name"));
		assertThat(responseMessage.getSignal(), is((Signal) created));
		assertThat(response.getRecipients(), contains(member));
	}

	@Test
	public void shouldThrowExceptionForCreateConversationWithExistingId() {
		// given
		Member member = stubMember(null);
		Message message = Message.createWith(create)//
				.withMember(new DefaultMember(member.getId(), "Wladzio"))//
				.withContent("wladzio-room")//
				.build();
		create.execute(member, message, context);

		message = Message.createWith(create)//
				.withMember(new DefaultMember(member.getId(), "Creating"))//
				.withContent("wladzio-room")//
				.build();

		// then
		exception.expect(ConversationExists.class);

		// when
		create.execute(member, message, context);

	}

	@Test
	public void shouldExecuteSignalOfferResponse() {
		// given
		Member member = stubMember("Member");
		Conversation conv = stubConversation();
		conv.join(member);

		Message message = Message.createWith(offerResponse)//
				.withMember(new DefaultMember(member.getId(), "Member"))//
				.withContent(conv.getId())//
				.build();
		// when
		SignalResponse response = offerResponse.execute(member, message, context);

		// then
		assertNotNull(response);
	}

	@Test
	public void shouldExecuteSignalAnswerResponse() {
		// given
		Member member = stubMember("Member");
		Conversation conv = stubConversation();
		conv.join(member);

		Message message = Message.createWith(answerResponse)//
				.withMember(new DefaultMember(member.getId(), "Member"))//
				.withContent(conv.getId())//
				.build();
		// when
		SignalResponse response = answerResponse.execute(member, message, context);

		// then
		assertNotNull(response);
	}

	@Test
	public void shouldExecuteSignalLeft() {
		// given
		Member member = stubMember("Member");
		Conversation conv = stubConversation();
		conv.join(member);

		// when
		SignalResponse response = left.execute(member, null, context);

		// then
		assertNotNull(response);
	}

	@Test
	public void shouldRemoveConversationWhenLastMemberLeft() {
		// given
		Member member = stubMember("Member");
		Conversation conv = stubConversation();
		conv.join(member);

		// when
		SignalResponse response = left.execute(member, null, context);

		// then
		assertNotNull(response);
		assertFalse(context.getConversations().findBy(conv.getId()).isPresent());
	}

	private Conversation stubConversation() {
		Conversation created = context.getConversationFactoryResolver().getDefaultOrBy(null).create();
		context.getConversations().add(created);
		return created;
	}

	private Member stubMember(String name) {
		Member member = context.getMembers().create();
		if (StringUtils.isEmpty(name)) {
			return member;
		}
		member.setName(name);
		return member;
	}

}
