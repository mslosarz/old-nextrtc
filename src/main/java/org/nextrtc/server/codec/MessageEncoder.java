package org.nextrtc.server.codec;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import org.apache.log4j.Logger;
import org.nextrtc.server.domain.Message;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MessageEncoder implements Encoder.Text<Message> {
	private static final Logger log = Logger.getLogger(MessageEncoder.class);

	private Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

	@Override
	public void destroy() {
	}

	@Override
	public void init(EndpointConfig config) {
	}

	@Override
	public String encode(Message message) throws EncodeException {
		String json = gson.toJson(message);
		log.debug(String.format("Response: %s", json));
		return json;
	}
}
