package org.nextrtc.signaling.domain;

public enum Signals {
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

	public static boolean isValid(String incoming) {
		for (Signals signal : values()) {
			if (signal.name().equals(incoming)) {
				return true;
			}
		}
		return false;
	}

}
