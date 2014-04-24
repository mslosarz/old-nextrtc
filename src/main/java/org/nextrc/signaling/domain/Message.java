package org.nextrc.signaling.domain;

import javax.websocket.Session;

import org.nextrc.signaling.Operations;

import com.google.gson.annotations.Expose;

public class Message {

	@Expose
	private String operation;
	@Expose
	private String content;

	private Session session;

	public String getOperation() {
		return operation;
	}

	public String getContent() {
		return content;
	}

	public Session getSession() {
		return session;
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

		public MessageBuilder withSession(Session session) {
			item.session = session;
			return this;
		}

		public MessageBuilder withSessionFrom(Message message) {
			item.session = message.session;
			return this;
		}

		public Message build() {
			return item;
		};

	}

}
