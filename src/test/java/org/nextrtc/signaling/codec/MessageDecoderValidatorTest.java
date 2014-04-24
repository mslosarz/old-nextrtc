package org.nextrtc.signaling.codec;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.nextrc.signaling.codec.MessageDecoder;

@RunWith(Parameterized.class)
public class MessageDecoderValidatorTest {

	private MessageDecoder decoder = new MessageDecoder();

	@Parameters(name = "{1} -> {0}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {//
						{ "{'signal' : 'newConversation', 'content' : 'empty', 'conversationId' : 'id'}", true },//
						{ "{'signal' : 'newConversation', 'conversationId' : 'id', 'content' : {'adsad':'adsd'} }",
								true },//
						{ "{signal : 'newConversation', 'conversationId' : 'id', content : null }", true },//
						{ "{signal : 'some', 'conversationId' : 'id', content : null }", false },//
						{ "{\"signal\" : newConversation, \"content\" : fish , 'conversationId' : id}", true },//
						{ "{'signal' : 'some', 'conversationId' : 'id'}", false },//
						{ "{'content' : 'empty'}", false },//
				});
	}

	private String json;
	private boolean result;

	public MessageDecoderValidatorTest(String json, boolean result) {
		this.json = json;
		this.result = result;
	}

	@Test
	public void testValidation() {
		// given json

		// when
		boolean actual = decoder.willDecode(json);

		// then
		assertThat(actual, is(result));
	}
}
