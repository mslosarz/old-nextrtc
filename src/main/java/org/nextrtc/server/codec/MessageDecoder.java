package org.nextrtc.server.codec;

import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;
import static org.nextrtc.server.domain.signal.SignalRegistry.isValid;

import java.util.Map;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

import org.apache.log4j.Logger;
import org.nextrtc.server.domain.Message;

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
		return gson.fromJson(escapeHtml4(json.replace("\"", "'")), Message.class);
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean willDecode(String json) {
		json = json.replace("\"", "'");

		try {
			Map<String, String> object = gson.fromJson(json, Map.class);
			boolean hasMember = object.get("member") != null;

			if (isValid(object.get("signal")) && hasMember) {
				return true;
			}
		} catch (Exception e) {
			log.error("Wrong syntax", e);
		}
		return false;
	}
}
