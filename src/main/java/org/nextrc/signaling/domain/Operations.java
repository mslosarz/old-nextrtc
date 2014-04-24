package org.nextrc.signaling.domain;

public enum Operations {
	// incoming
	newConversation {
		@Override
		public void execute(Message createRequest, ConversationContainer container) {
			container.createNewConversation(createRequest);
		}
	},
	joinConversation {
		@Override
		public void execute(Message joinRequest, ConversationContainer container) {
			Conversation conversation = container.findConversationById(joinRequest.getConversationId());
			conversation.join(joinRequest);
		}

	},

	// outgoing
	conversationCreated, mediaOffer, mediaAnswer;

	public void execute(Message message, ConversationContainer container) {
	}

	public static boolean isValid(String operation) {
		for (Operations op : values()) {
			if (op.name().equals(operation)) {
				return true;
			}
		}
		return false;
	}

}
