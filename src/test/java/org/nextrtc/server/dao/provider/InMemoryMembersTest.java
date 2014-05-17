package org.nextrtc.server.dao.provider;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.nextrtc.server.dao.Members;
import org.nextrtc.server.dao.provider.InMemboryMembers;
import org.nextrtc.server.domain.Member;

public class InMemoryMembersTest {

	private Members container;

	@Before
	public void setupContainer() {
		container = new InMemboryMembers();
	}

	@Test
	public void shouldCreateMember() {
		// given

		// when
		Member newOne = container.create();

		// then
		assertNotNull(newOne);
		assertNotNull(newOne.getId());
	}

	@Test
	public void shouldFindCreatedMember() {
		// given
		Member member = container.create();

		// when
		Member found = container.findBy(member.getId());

		// then
		assertNotNull(found);
		assertThat(found, is(member));
	}

	@Test
	public void shouldRemoveMember() {
		// given
		Member member = container.create();

		// when
		container.remove(member);
		
		// then
		assertNull(container.findBy(member.getId()));
	}

	@Test
	public void shouldUpdateMemberNick() {
		// given
		Member member = container.create();
		assertThat(member.getName(), isEmptyOrNullString());

		// when
		container.updateNick(member, "Stefan");

		// then
		assertThat(member.getName(), is("Stefan"));

		Member found = container.findBy(member.getId());
		assertThat(member.getId(), is(found.getId()));
		assertThat(member.getName(), is(found.getName()));
	}

}
