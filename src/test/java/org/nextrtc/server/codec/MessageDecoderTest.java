package org.nextrtc.server.codec;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.nextrtc.server.domain.signal.SignalRegistry.DefaultSignal.join;

import javax.websocket.DecodeException;

import org.junit.Test;
import org.nextrtc.server.domain.Message;
import org.nextrtc.server.domain.signal.Signal;

public class MessageDecoderTest {

	private MessageDecoder decoder = new MessageDecoder();

	@Test
	public void shouldParseObject() throws DecodeException {
		// given
		String validJson = "{'signal' : 'join', 'member':{'id':null, 'name':'Bob'}, 'content':'something'}";

		// when
		Message result = decoder.decode(validJson);

		// then
		assertNotNull(result);
		assertThat(result.getSignal(), equalTo((Signal) join));
		assertThat(result.getMemberId(), isEmptyOrNullString());
		assertThat(result.getMemberName(), is("Bob"));
		assertThat(result.getContent(), equalTo("something"));
	}

	@Test
	public void shouldParseEmptyObject() throws DecodeException {
		// given
		String validJson = "{'signal' : 'join', 'member':null, 'content':null}";

		// when
		Message result = decoder.decode(validJson);

		// then
		assertNotNull(result);
		assertThat(result.getSignal(), equalTo((Signal) join));
		assertThat(result.getMemberId(), isEmptyOrNullString());
		assertThat(result.getMemberName(), isEmptyOrNullString());
		assertThat(result.getContent(), isEmptyOrNullString());
	}
}
