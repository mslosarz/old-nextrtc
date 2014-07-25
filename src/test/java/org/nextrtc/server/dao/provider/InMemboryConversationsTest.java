package org.nextrtc.server.dao.provider;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.nextrtc.server.dao.Conversations;
import org.nextrtc.server.domain.Conversation;
import org.nextrtc.server.domain.Member;
import org.nextrtc.server.domain.provider.DefaultMember;
import org.nextrtc.server.factory.ConversationFactory;
import org.nextrtc.server.factory.ConversationTypes;

import com.google.common.base.Optional;

public class InMemboryConversationsTest {
	
	private Conversations container;

	private ConversationFactory factory = ConversationTypes.mesh.getFactory();

	@Before
	public void setupConversation() {
		container = new InMemoryConversations();
	}

	@Test
	public void shouldCreateConversation() {
		// given

		// when
		Conversation created = factory.create();

		// then
		assertNotNull(created);
	}

	@Test
	public void shouldCreateAndFindConversation() {
		// given
		Conversation created = factory.create();
		container.save(created);

		// when
		Optional<Conversation> found = container.findBy(created.getId());

		// then
		assertTrue(found.isPresent());
		assertThat(found.get(), is(created));
	}

	@Test
	public void shouldReturnNullWhenDontFindAnyConversation() {
		// given

		// when
		Optional<Conversation> found = container.findBy("not existing one");

		// then
		assertFalse(found.isPresent());
	}

	@Test
	public void shouldReturnNullWhenGivenIdIsNull() {
		// given
		String nullString = null;

		// when
		Optional<Conversation> found = container.findBy(nullString);

		// then
		assertFalse(found.isPresent());
	}

	@Test
	public void shouldFindConversationByMember() {
		// given
		Conversation created = factory.create();
		container.save(created);
		Member member = new DefaultMember("qwer-ty", "Wladzio");
		created.join(member);

		// when
		Optional<Conversation> found = container.findBy(member);

		// then
		assertTrue(found.isPresent());
		assertThat(found.get(), is(created));
	}

	@Test
	public void shouldReturnNullForNullMemberParameter() {
		// given
		Conversation created = factory.create();
		container.save(created);
		Member member = new DefaultMember("qwer-ty", "Wladzio");
		created.join(member);
		Member nullMember = null;

		// when
		Optional<Conversation> found = container.findBy(nullMember);

		// then
		assertFalse(found.isPresent());
	}

	@Test
	public void shouldRemoveExistingConversation() {
		// given
		Conversation conv = factory.create();
		container.save(conv);

		// when
		container.remove(conv);
		Optional<Conversation> found = container.findBy(conv.getId());

		// then
		assertFalse(found.isPresent());
	}

	@Test
	public void removeShouldWorkForNullParameter() {
		// given
		Conversation conv = factory.create();
		container.save(conv);

		// when
		container.remove(null);
		Optional<Conversation> found = container.findBy(conv.getId());

		// then
		assertTrue(found.isPresent());
	}

	@Test
	public void shouldReturlCollectionOfConversations(){
		// given
		factory = ConversationTypes.broadcast.getFactory();
		Conversation conv1 = factory.create("0");
		Conversation conv2 = factory.create("1");
		container.save(conv1);
		container.save(conv2);

		// when
		Collection<Conversation> all = container.getAll();

		// then
		assertThat(all, containsInAnyOrder(conv1, conv2));
	}

}
