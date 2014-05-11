package org.nextrtc.server.domain.signal;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.nextrtc.server.domain.signal.DefaultSignals.answerResponse;
import static org.nextrtc.server.domain.signal.DefaultSignals.create;
import static org.nextrtc.server.domain.signal.DefaultSignals.join;
import static org.nextrtc.server.domain.signal.DefaultSignals.offerResponse;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.nextrtc.server.dao.impl.ConversationContainer;
import org.nextrtc.server.dao.impl.MemberContainer;
import org.nextrtc.server.domain.Conversation;
import org.nextrtc.server.domain.DefaultMember;
import org.nextrtc.server.domain.Member;
import org.nextrtc.server.domain.Message;
import org.nextrtc.server.domain.RequestContext;
import org.nextrtc.server.domain.SignalResponse;

public class DefaultSignalsTest {

	private RequestContext context;

	@Before
	public void setupContext() {
		context = new RequestContext(new ConversationContainer(), new MemberContainer());
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
		assertTrue(false);
	}

	private Conversation stubConversation() {
		return context.getConversationDao().create();
	}

	private Member stubMember(String name) {
		Member member = context.getMemberDao().create();
		if (StringUtils.isEmpty(name)) {
			return member;
		}
		member.setName(name);
		return member;
	}

}
