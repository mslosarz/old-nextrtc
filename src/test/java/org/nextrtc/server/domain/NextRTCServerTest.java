package org.nextrtc.server.domain;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.nextrtc.server.domain.signal.DefaultSignals.create;

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
import org.nextrtc.server.dao.ConversationDao;
import org.nextrtc.server.dao.MemberDao;
import org.nextrtc.server.exception.MemberNotFoundException;
import org.nextrtc.server.service.MessageSender;

public class NextRTCServerTest {

	@InjectMocks
	private NextRTCServer server;

	@Mock
	private MessageSender sender;

	@Mock
	private ConversationDao conversationDao;

	@Mock
	private MemberDao memberDao;

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
		when(memberDao.create()).thenReturn(member);

		// when
		server.register(session);

		// then
		verify(memberDao).create();
	}

	@Test
	public void shouldThrowErrorOnRequestFromNotExistingMember() {
		// given
		Session session = mock(Session.class);
		Message message = Message.createWith(create).member(mock(Member.class)).build();

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
		server.handle(Message.createWith(create).member(member).build(), session);

		// then
		verify(sender).send((SenderRequest) Mockito.any());
	}

	private void mockMemberDaoFor(Member member) {
		when(memberDao.create()).thenReturn(member);
		when(memberDao.findBy("id")).thenReturn(member);
	}

	private void mockConversationDaoFor(Conversation conv, Member member) {
		when(conversationDao.create()).thenReturn(conv);
		when(conversationDao.findBy(member)).thenReturn(conv);
		when(conversationDao.findBy(conv.getId())).thenReturn(conv);
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

	private DefaultMember stubMember(String id, String name) {
		return new DefaultMember(id, name);
	}

	@After
	public void resetMocks() {
		reset(sender, conversationDao, memberDao);
	}

}
