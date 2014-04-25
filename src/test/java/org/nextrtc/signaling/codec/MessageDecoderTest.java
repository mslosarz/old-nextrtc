package org.nextrtc.signaling.codec;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import javax.websocket.DecodeException;

import org.junit.Test;
import org.nextrtc.signaling.codec.MessageDecoder;
import org.nextrtc.signaling.domain.Message;

public class MessageDecoderTest {

	private MessageDecoder decoder = new MessageDecoder();

	@Test
	public void shouldParseValidObject() throws DecodeException {
		// given
		String validJson = "{'signal' : 'some', 'content' : 'empty', 'conversationId' : 'aaa'}";

		// when
		Message result = decoder.decode(validJson);

		// then
		assertNotNull(result);
		assertThat(result.getSignal(), equalTo("some"));
		assertThat(result.getContent(), equalTo("empty"));
		assertThat(result.getConversationId(), equalTo("aaa"));
	}
}
