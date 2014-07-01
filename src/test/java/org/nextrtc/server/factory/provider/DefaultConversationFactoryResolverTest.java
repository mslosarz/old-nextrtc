package org.nextrtc.server.factory.provider;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.nextrtc.server.factory.ConversationFactory;
import org.nextrtc.server.factory.ConversationFactoryResolver;

public class DefaultConversationFactoryResolverTest {

	private ConversationFactoryResolver resolver;

	@Before
	public void setup() {
		resolver = new DefaultConversationFactoryResolver();
	}

	private final ConversationFactory mockedFactory = mock(ConversationFactory.class);

	@Test
	public void shouldReturnDefaultFactoryForNull() {
		// given

		// when
		ConversationFactory defaultFactory = resolver.getDefaultOrBy(null);

		// then
		assertNotNull(defaultFactory);
	}

	@Test
	public void shouldReturnKnownDefaultFactoryForNull() {
		// given
		resolver.setDefault(mockedFactory);

		// when
		ConversationFactory defaultFactory = resolver.getDefaultOrBy(null);

		// then
		assertNotNull(defaultFactory);
		assertThat(defaultFactory, is(mockedFactory));
	}

	@Test
	public void shouldReturnDefaultFactoryForNotExisting() {
		// given

		// when
		ConversationFactory defaultFactory = resolver.getDefaultOrBy("");

		// then
		assertNotNull(defaultFactory);
	}

	@Test
	public void shouldReturnKnownDefaultFactoryForNotExisting() {
		// given
		resolver.setDefault(mockedFactory);

		// when
		ConversationFactory defaultFactory = resolver.getDefaultOrBy("");

		// then
		assertNotNull(defaultFactory);
		assertThat(defaultFactory, is(mockedFactory));
	}

	@Test
	public void shouldAllowToRegisterNewFactory() {
		// given
		ConversationFactory factory = mock(ConversationFactory.class);
		resolver.register("my own factory", factory);
		resolver.setDefault(mockedFactory);

		// when
		ConversationFactory returnedFactory = resolver.getDefaultOrBy("my own factory");

		// then
		assertNotNull(returnedFactory);
		assertThat(returnedFactory, is(factory));
	}

}
