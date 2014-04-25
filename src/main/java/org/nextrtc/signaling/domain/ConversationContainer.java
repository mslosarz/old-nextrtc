package org.nextrtc.signaling.domain;

import static org.nextrtc.signaling.domain.Message.create;
import static org.nextrtc.signaling.domain.Signals.conversationCreated;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.websocket.Session;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * This class holds all the conversation, has information about all occupied
 * conversation id's
 */
@Component
@Scope("singleton")
public class ConversationContainer {
	private Map<String, Conversation> conversations = Collections.synchronizedMap(new HashMap<>());

	public Message createNewConversation(Message newOne) {
		String conversationId = fetchConversationId();

		conversations.put(conversationId, new Conversation(newOne));

		Message result = create()//
				.withSignal(conversationCreated)//
				.withConversationId(conversationId)//
				.withSessionFrom(newOne)//
				.build();
		result.send();
		return result;
	}

	public Conversation findConversationById(String conversationId) {
		return conversations.get(conversationId);
	}

	public void disconnectAllMembersWith(Session session) {
		for (Conversation conversation : conversations.values()) {
			conversation.disconnect(session);
		}
	}

	private String fetchConversationId() {
		String conversationId = UUID.randomUUID().toString();
		while (conversations.containsKey(conversationId)) {
			conversationId = UUID.randomUUID().toString();
		}
		return conversationId;
	}

}
