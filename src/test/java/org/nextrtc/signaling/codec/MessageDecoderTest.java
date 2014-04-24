package org.nextrtc.signaling.codec;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import javax.websocket.DecodeException;

import org.junit.Test;
import org.nextrc.signaling.codec.MessageDecoder;
import org.nextrc.signaling.domain.Message;

public class MessageDecoderTest {

	private MessageDecoder decoder = new MessageDecoder();

	@Test
	public void shouldParseValidObject() throws DecodeException {
		// given
		String validJson = "{'operation' : 'some', 'content' : 'empty'}";

		// when
		Message result = decoder.decode(validJson);

		// then
		assertNotNull(result);
		assertThat(result.getOperation(), equalTo("some"));
		assertThat(result.getContent(), equalTo("empty"));
	}
}
