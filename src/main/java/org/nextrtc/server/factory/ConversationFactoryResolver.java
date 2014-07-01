package org.nextrtc.server.factory;

public interface ConversationFactoryResolver {

	void register(String name, ConversationFactory factory);

	ConversationFactory getDefaultOrBy(String name);

	void setDefault(ConversationFactory factory);
}
