package org.nextrtc.server.factory.provider;

import static java.util.Collections.synchronizedMap;

import java.util.HashMap;
import java.util.Map;

import org.nextrtc.server.factory.ConversationFactory;
import org.nextrtc.server.factory.ConversationFactoryResolver;
import org.nextrtc.server.factory.ConversationTypes;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class DefaultConversationFactoryResolver implements ConversationFactoryResolver {

	private Map<String, ConversationFactory> factories = synchronizedMap(new HashMap<String, ConversationFactory>());

	private ConversationFactory defaultFactory = ConversationTypes.mesh.getFactory();
	
	public DefaultConversationFactoryResolver() {
		for (ConversationTypes type : ConversationTypes.values()) {
			register(type.name(), type.getFactory());
		}
	}

	@Override
	public void register(String name, ConversationFactory factory) {
		factories.put(name, factory);
	}

	@Override
	public ConversationFactory getDefaultOrBy(String name) {
		ConversationFactory conversationFactory = factories.get(name);
		if (conversationFactory == null) {
			return defaultFactory;
		}
		return conversationFactory;
	}

	@Override
	public void setDefault(ConversationFactory factory) {
		this.defaultFactory = factory;
	}

}
