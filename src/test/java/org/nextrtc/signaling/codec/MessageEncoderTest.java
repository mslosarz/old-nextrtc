package org.nextrtc.signaling.codec;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import javax.websocket.EncodeException;

import org.junit.Test;
import org.nextrtc.signaling.codec.MessageEncoder;
import org.nextrtc.signaling.domain.Message;

public class MessageEncoderTest {

	private MessageEncoder encoder = new MessageEncoder();

	@Test
	public void shouldEncodeObject() throws EncodeException {
		// given
		Message message = Message.create()//
				.withContent("c")//
				.withSignal("o")//
				.build();

		// when
		String result = encoder.encode(message);

		// then
		assertNotNull(result);
		assertThat(result, containsString("\"content\":\"c\""));
		assertThat(result, containsString("\"signal\":\"o\""));
	}

}
