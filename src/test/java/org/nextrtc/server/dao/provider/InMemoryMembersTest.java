package org.nextrtc.server.dao.provider;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.nextrtc.server.dao.Members;
import org.nextrtc.server.domain.Member;
import org.nextrtc.server.factory.MemberFactory;
import org.nextrtc.server.factory.provider.DefaultMemberFactory;

import com.google.common.base.Optional;

public class InMemoryMembersTest {

	private Members container;

	private MemberFactory factory = new DefaultMemberFactory();

	@Before
	public void setupContainer() {
		container = new InMemboryMembers();
	}

	@Test
	public void shouldCreateMember() {
		// given

		// when
		Member newOne = factory.create();

		// then
		assertNotNull(newOne);
		assertNotNull(newOne.getId());
	}

	@Test
	public void shouldFindCreatedMember() {
		// given
		Member member = factory.create();
		container.save(member);

		// when
		Optional<Member> found = container.findBy(member.getId());

		// then
		assertTrue(found.isPresent());
		assertThat(found.get(), is(member));
	}

	@Test
	public void shouldRemoveMember() {
		// given
		Member member = factory.create();
		container.save(member);

		// when
		container.remove(member);
		
		// then
		assertFalse(container.findBy(member.getId()).isPresent());
	}

	@Test
	public void shouldUpdateMemberNick() {
		// given
		Member member = factory.create();
		container.save(member);
		assertThat(member.getName(), isEmptyOrNullString());

		// when
		container.updateNick(member, "Stefan");

		// then
		assertThat(member.getName(), is("Stefan"));

		Optional<Member> found = container.findBy(member.getId());
		assertTrue(found.isPresent());
		assertThat(member.getId(), is(found.get().getId()));
		assertThat(member.getName(), is(found.get().getName()));
	}

}
