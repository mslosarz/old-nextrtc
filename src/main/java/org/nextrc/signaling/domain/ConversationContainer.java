package org.nextrc.signaling.domain;

import static org.nextrc.signaling.Operations.conversationCreated;
import static org.nextrc.signaling.domain.Message.create;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

		conversations.put(conversationId, new Conversation());

		Message result = create()//
				.withOperation(conversationCreated)//
				.withContent(conversationId)//
				.withSessionFrom(newOne)//
				.build();

		result.send();
		return result;
	}

	private String fetchConversationId() {
		String conversationId = UUID.randomUUID().toString();
		while (conversations.containsKey(conversationId)) {
			conversationId = UUID.randomUUID().toString();
		}
		return conversationId;
	}

	public Conversation findConversationById(String conversationId) {
		return conversations.get(conversationId);
	}

}
