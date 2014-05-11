package org.nextrtc.server.codec;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.nextrtc.server.domain.Message.createWith;
import static org.nextrtc.server.domain.signal.DefaultSignals.created;

import javax.websocket.EncodeException;

import org.junit.Test;
import org.nextrtc.server.domain.DefaultMember;
import org.nextrtc.server.domain.Message;

public class MessageEncoderTest {

	private MessageEncoder encoder = new MessageEncoder();

	@Test
	public void shouldEncodeObject() throws EncodeException {
		// given
		Message message = createWith(created)//
				.withContent("c")//
				.withMember(new DefaultMember("qwe", "rty"))//
				.build();

		// when
		String result = encoder.encode(message);

		// then
		assertNotNull(result);
		assertThat(result, containsString(replaceQuotes("'content':'c'")));
		assertThat(result, containsString(replaceQuotes("'signal':'created'")));
		assertThat(result, containsString(replaceQuotes("'member':{'id':'qwe','name':'rty'}")));
	}

	@Test
	public void shouldEncodeObjectWithNullMember() throws EncodeException {
		// given
		Message message = createWith(created)//
				.withContent("c")//
				.build();

		// when
		String result = encoder.encode(message);

		// then
		assertNotNull(result);
		assertThat(result, containsString(replaceQuotes("'content':'c'")));
		assertThat(result, containsString(replaceQuotes("'signal':'created'")));
		assertThat(result, not(containsString(replaceQuotes("'member'"))));
	}

	private String replaceQuotes(String string) {
		return string.replace("'", "\"");
	}
}
