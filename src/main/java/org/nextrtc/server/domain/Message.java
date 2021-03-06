package org.nextrtc.server.domain;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import org.nextrtc.server.domain.signal.Signal;
import org.nextrtc.server.domain.signal.SignalRegistry;
import org.nextrtc.server.factory.ConversationTypes;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

public class Message {

	/**
	 * Use MessageDto.createWith(...) instead of new MessageDto()
	 */
	@Deprecated
	public Message() {
	}

	@Expose
	private String signal = EMPTY;

	@Expose
	private MessageMember member;

	@Expose
	private String content = EMPTY;

	@Expose
	private String type = EMPTY;

	public Signal getSignal() {
		return SignalRegistry.get(signal);
	}

	public String getContent() {
		return content;
	}

	public String getMemberName() {
		return member != null ? member.getName() : EMPTY;
	}

	public String getMemberId() {
		return member != null ? member.getId() : EMPTY;
	}

	public String getType() {
		return type;
	}

	@Override
	public String toString() {
		return new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJson(this);
	}

	public static class MessageMember {

		@Expose
		private String id;
		@Expose
		private String name;

		@Deprecated
		public MessageMember() {
		}

		public MessageMember(Member member) {
			this.id = member.getId();
			this.name = member.getName();
		}

		public String getId() {
			return id;
		}

		public String getName() {
			return name;
		}

	}

	public static MessageBuilder createWith(Signal signal) {
		return new MessageBuilder(signal);
	}

	public static MessageBuilder createWith(String signal) {
		return new MessageBuilder(signal);
	}

	public static class MessageBuilder {

		private Message message = new Message();

		private MessageBuilder(String signal) {
			if (signal == null) {
				throw new IllegalArgumentException();
			}
			message.signal = signal;
		}

		private MessageBuilder(Signal signal) {
			if (signal == null) {
				throw new IllegalArgumentException();
			}
			message.signal = signal.name();
		}

		public MessageBuilder withContent(String content) {
			message.content = content;
			return this;
		}

		public MessageBuilder withMember(Member member) {
			message.member = new MessageMember(member);
			return this;
		}

		public MessageBuilder withType(ConversationTypes type) {
			message.type = type.name();
			return this;
		}

		public Message build() {
			return message;
		}

	}

}
