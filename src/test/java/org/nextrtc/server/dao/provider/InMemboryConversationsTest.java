package org.nextrtc.server.dao.provider;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.nextrtc.server.dao.Conversations;
import org.nextrtc.server.dao.provider.InMemoryConversations;
import org.nextrtc.server.domain.Conversation;
import org.nextrtc.server.domain.Member;
import org.nextrtc.server.domain.provider.DefaultMember;

public class InMemboryConversationsTest {
	
	private Conversations container;

	@Before
	public void setupConversation() {
		container = new InMemoryConversations();
	}

	@Test
	public void shouldCreateConversation() {
		// given

		// when
		Conversation created = container.create();

		// then
		assertNotNull(created);
	}

	@Test
	public void shouldCreateAndFindConversation() {
		// given
		Conversation created = container.create();

		// when
		Conversation found = container.findBy(created.getId());

		// then
		assertNotNull(found);
		assertThat(found, is(created));
	}

	@Test
	public void shouldReturnNullWhenDontFindAnyConversation() {
		// given

		// when
		Conversation found = container.findBy("not existing one");

		// then
		assertNull(found);
	}

	@Test
	public void shouldReturnNullWhenGivenIdIsNull() {
		// given
		String nullString = null;

		// when
		Conversation found = container.findBy(nullString);

		// then
		assertNull(found);
	}

	@Test
	public void shouldFindConversationByMember() {
		// given
		Conversation created = container.create();
		Member member = new DefaultMember("qwer-ty", "Wladzio");
		created.join(member);

		// when
		Conversation found = container.findBy(member);

		// then
		assertNotNull(found);
		assertThat(found, is(created));
	}

	@Test
	public void shouldReturnNullForNullMemberParameter() {
		// given
		Conversation created = container.create();
		Member member = new DefaultMember("qwer-ty", "Wladzio");
		created.join(member);
		Member nullMember = null;

		// when
		Conversation found = container.findBy(nullMember);

		// then
		assertNull(found);
	}

	@Test
	public void shouldRemoveExistingConversation() {
		// given
		Conversation conv = container.create();

		// when
		container.remove(conv);
		Conversation found = container.findBy(conv.getId());

		// then
		assertNull(found);
	}

	@Test
	public void removeShouldWorkForNullParameter() {
		// given
		Conversation conv = container.create();

		// when
		container.remove(null);
		Conversation found = container.findBy(conv.getId());

		// then
		assertNotNull(found);
	}

}
