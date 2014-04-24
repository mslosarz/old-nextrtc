package org.nextrc.signaling.codec;

import static org.nextrc.signaling.domain.Signals.isValid;

import java.util.Map;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

import org.nextrc.signaling.domain.Message;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MessageDecoder implements Decoder.Text<Message> {

	private Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

	@Override
	public void destroy() {

	}

	@Override
	public void init(EndpointConfig config) {

	}

	@Override
	public Message decode(String json) throws DecodeException {
		return gson.fromJson(json, Message.class);
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean willDecode(String json) {
		Map<String, String> object = gson.fromJson(json, Map.class);
		boolean containsContent = object.containsKey("content");
		boolean containsConversationId = object.containsKey("conversationId");
		return isValid(object.get("signal")) && containsContent && containsConversationId;
	}
}
