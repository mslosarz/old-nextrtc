package org.nextrtc.server.domain;

import static java.util.Optional.ofNullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.nextrtc.server.domain.signal.SignalRegistry.DefaultSignal.create;
import static org.nextrtc.server.domain.signal.SignalRegistry.DefaultSignal.join;

import javax.websocket.Session;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.nextrtc.server.dao.Conversations;
import org.nextrtc.server.dao.Members;
import org.nextrtc.server.domain.provider.DefaultMember;
import org.nextrtc.server.domain.signal.SenderRequest;
import org.nextrtc.server.domain.signal.SignalResponse;
import org.nextrtc.server.exception.MemberNotFoundException;
import org.nextrtc.server.service.MessageSender;

public class NextRTCServerTest {

	@InjectMocks
	private NextRTCServer server;

	@Mock
	private MessageSender sender;

	@Mock
	private Conversations conversations;

	@Mock
	private Members members;

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Before
	public void mockDependecy() {
		server = new NextRTCServer();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void shouldAllowToRegisterNewSession() {
		// given
		Session session = mock(Session.class);
		DefaultMember member = stubMember("id", "Wladzio");
		when(members.create()).thenReturn(member);

		// when
		server.register(session);

		// then
		verify(members).create();
	}

	@Test
	public void shouldThrowErrorOnRequestFromNotExistingMember() {
		// given
		Session session = mock(Session.class);
		when(members.findBy(Mockito.anyObject())).thenReturn(ofNullable(null));
		Message message = Message.createWith(create).withMember(mock(Member.class)).build();

		// then
		exception.expect(MemberNotFoundException.class);

		// when
		server.handle(message, session);
	}

	@Test
	public void shouldHandleMessageFromRegisterMember() {
		// given
		Session session = mock(Session.class);
		DefaultMember member = stubMember("id", "Wladzio");
		mockMemberDaoFor(member);
		Conversation conv = mock(Conversation.class);
		when(conv.getId()).thenReturn("cid");
		when(conv.joinOwner(member)).thenReturn(SignalResponse.EMPTY);
		mockConversationDaoFor(conv, member);
		server.register(session);

		// when
		server.handle(Message.createWith(create).withMember(member).build(), session);

		// then
		verify(sender).send((SenderRequest) Mockito.any());
	}
	
	@Test
	public void shouldAllowToUnregisterSession() {
		// given
		Session session = mock(Session.class);
		DefaultMember member = stubMember("id", "Wladzio");
		mockMemberDaoFor(member);
		server.register(session);

		// when
		server.unregister(session);

		// then
	}

	@Test
	public void shouldAllowToJoinToNotExistingConversation() {
		// given
		Session session = mock(Session.class);
		DefaultMember member = stubMember("id", "Wladzio");
		when(members.create()).thenReturn(member);
		when(members.findBy("id")).thenReturn(ofNullable(member));

		when(conversations.findBy(Mockito.anyObject())).thenReturn(ofNullable(null));
		Conversation conv = mock(Conversation.class);
		when(conv.getId()).thenReturn("cid");
		when(conv.joinOwner(member)).thenReturn(SignalResponse.EMPTY);
		mockConversationDaoFor(conv, member);
		server.register(session);

		// when
		server.handle(Message.createWith(join).withMember(member).build(), session);

		// then
		verify(members).create();
		verify(conversations).create();
		verify(sender).send((SenderRequest) Mockito.any());
	}

	private void mockMemberDaoFor(Member member) {
		when(members.create()).thenReturn(member);
		when(members.findBy("id")).thenReturn(ofNullable(member));
	}

	private void mockConversationDaoFor(Conversation conv, Member member) {
		when(conversations.create()).thenReturn(conv);
		when(conversations.findBy(member)).thenReturn(ofNullable(conv));
		when(conversations.findBy(conv.getId())).thenReturn(ofNullable(conv));
	}

	private DefaultMember stubMember(String id, String name) {
		return new DefaultMember(id, name);
	}

	@After
	public void resetMocks() {
		reset(sender, conversations, members);
	}

}
