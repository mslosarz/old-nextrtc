package org.nextrtc.server;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.nextrtc.server.domain.signal.DefaultSignal.create;

import javax.websocket.RemoteEndpoint.Async;
import javax.websocket.Session;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.nextrtc.server.domain.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(classes = Config.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class NextRTCEndpointTest {

	@Autowired
	private NextRTCEndpoint endpoint;

	@Test
	public void shouldAutowireDependency() {
		assertNotNull(endpoint);
	}

	@Test
	public void shouldAllowToExecuteRequest() throws Exception {
		// given
		Session incomming = mock(Session.class);
		Async async = mock(Async.class);
		when(incomming.getAsyncRemote()).thenReturn(async);


		// when
		endpoint.onOpen(incomming);
		endpoint.onMessage(Message.createWith(create).build(), incomming);
		endpoint.onClose(incomming);

		// then
		verify(async).sendObject(Mockito.anyObject());
		verify(incomming).close();
	}

}
