package org.nextrtc.server.domain;

import static com.google.common.base.Optional.fromNullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.nextrtc.server.domain.signal.DefaultSignal.create;
import static org.nextrtc.server.domain.signal.DefaultSignal.join;

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
import org.nextrtc.server.factory.ConversationFactory;
import org.nextrtc.server.factory.ConversationFactoryResolver;
import org.nextrtc.server.service.MessageSender;

import com.google.common.base.Optional;

public class NextRTCServerTest {

	@InjectMocks
	private NextRTCServer server;

	@Mock
	private MessageSender sender;

	@Mock
	private Conversations conversations;

	@Mock
	private Members members;

	@Mock
	private ConversationFactoryResolver resolver;

	@Mock
	private ConversationFactory factory;

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Before
	public void mockDependecy() {
		server = new NextRTCServer();
		MockitoAnnotations.initMocks(this);
		
		when(resolver.getDefaultOrBy((String) Mockito.anyString())).thenReturn(factory);
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
		when(members.findBy(Mockito.anyString())).thenReturn(fromNullable((Member) null));
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
		when(conversations.findBy(member)).thenReturn(Optional.<Conversation> absent());
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
		when(members.findBy("id")).thenReturn(fromNullable((Member) member));

		when(conversations.findBy(Mockito.anyString())).thenReturn(fromNullable((Conversation) null));
		Conversation conv = mock(Conversation.class);
		when(conv.getId()).thenReturn("cid");
		when(conv.joinOwner(member)).thenReturn(SignalResponse.EMPTY);
		mockConversationDaoFor(conv, member);
		server.register(session);

		// when
		server.handle(Message.createWith(join).withMember(member).build(), session);

		// then
		verify(members).create();
		verify(conversations).add((Conversation) Mockito.any());
		verify(sender).send((SenderRequest) Mockito.any());
	}

	private void mockMemberDaoFor(Member member) {
		when(members.create()).thenReturn(member);
		when(members.findBy("id")).thenReturn(fromNullable(member));
	}

	private void mockConversationDaoFor(Conversation conv, Member member) {
		when(factory.create()).thenReturn(conv);
		when(conversations.findBy(member)).thenReturn(fromNullable(conv));
		when(conversations.findBy(conv.getId())).thenReturn(fromNullable(conv));
	}

	private DefaultMember stubMember(String id, String name) {
		return new DefaultMember(id, name);
	}

	@After
	public void resetMocks() {
		reset(sender, conversations, members, resolver, factory);
	}

}
