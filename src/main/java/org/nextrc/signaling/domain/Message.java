package org.nextrc.signaling.domain;

import javax.websocket.RemoteEndpoint.Async;
import javax.websocket.Session;

import com.google.gson.annotations.Expose;

public class Message {

	@Expose
	private String operation;

	@Expose
	private String conversationId;

	@Expose
	private String content;

	private Session session;

	public String getOperation() {
		return operation;
	}

	public Operations getOperationAsEnum() {
		return Operations.valueOf(operation);
	}

	public String getConversationId() {
		return conversationId;
	}

	public String getContent() {
		return content;
	}

	public Session getSession() {
		return session;
	}

	public Async getAsyncRemote() {
		assert (session != null);
		return session.getAsyncRemote();
	}

	public void initSession(Session session) {
		if (this.session == null) {
			this.session = session;
		}
	}

	public void send() {
		assert (session != null);
		session.getAsyncRemote().sendObject(this);
	}

	public static MessageBuilder create() {
		return new MessageBuilder();
	}

	public static class MessageBuilder {
		private Message item = new Message();

		private MessageBuilder() {
		}

		public MessageBuilder withContent(String content) {
			item.content = content;
			return this;
		}

		public MessageBuilder withOperation(String operation) {
			item.operation = operation;
			return this;
		}

		public MessageBuilder withOperation(Operations operation) {
			item.operation = operation.name();
			return this;
		}

		public MessageBuilder withConversationId(String conversationId) {
			item.conversationId = conversationId;
			return this;
		}

		public MessageBuilder withSession(Session session) {
			item.session = session;
			return this;
		}

		public MessageBuilder withSessionFrom(Message message) {
			item.session = message.session;
			return this;
		}

		public MessageBuilder withSessionFrom(Member member) {
			item.session = member.getSession();
			return this;
		}

		public Message build() {
			return item;
		};

	}

}
