package org.nextrtc.server.factory;


public enum ConversationTypes {
	broadcast(new BroadcastConversationFactory()), //
	mesh(new MeshConversationFactory());

	private ConversationFactory factory;

	ConversationTypes(ConversationFactory factory) {
		this.factory = factory;
	}

	public ConversationFactory getFactory() {
		return factory;
	}
	
}
