package org.nextrtc.server.codec;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class MessageDecoderValidatorTest {

	private MessageDecoder decoder = new MessageDecoder();

	@Parameters(name = "{1} -> {0}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {//
						{ "{'signal' : 'join', 'content' : 'empty', 'member' : {id:null, name:Alice}}", true },//
						{ "{'signal' : 'create', 'content' : null, 'member' : {'id':null, 'name':Bob} }", true },//
						{ "{ signal : 'answerResponse', 'member' : {}, content : null }", true },//
						{ "{ signal : 'wrong', 'member' : {}, content : null }", false },//
						{ "{\"signal\" : join, \"content\" : fish , 'member' : null}", false },//
						{ "{'signal' : 'created', member : {} }", true },//
						{ "{'content' : 'empty'}", false },//
						{ "{'signal':'join','member':{'id':null,'name':'asdf'}}", true }
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
