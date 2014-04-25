package org.nextrtc.signaling.codec;

import static org.nextrtc.signaling.domain.Signals.isValid;

import java.util.Map;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

import org.apache.log4j.Logger;
import org.nextrtc.signaling.domain.Message;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MessageDecoder implements Decoder.Text<Message> {
	private static final Logger log = Logger.getLogger(MessageDecoder.class);
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
		log.info(json);
		Map<String, String> object = gson.fromJson(json, Map.class);
		boolean containsContent = object.containsKey("content");
		boolean containsConversationId = object.containsKey("conversationId");
		return isValid(object.get("signal")) && containsContent && containsConversationId;
	}
}
