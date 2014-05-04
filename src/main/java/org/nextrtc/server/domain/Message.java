package org.nextrtc.server.domain;

import org.nextrtc.server.domain.signal.DefaultSignals;
import org.nextrtc.server.domain.signal.Signal;

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
	private String signal = "";

	@Expose
	private MessageMember member;

	@Expose
	private String content = "";

	public Signal getSignal() {
		return DefaultSignals.valueOf(signal);
	}

	public String getContent() {
		return content;
	}

	public String getMemberName() {
		return member.getName();
	}

	public String getMemberId() {
		return member.getId();
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
			this.name = member.getNick();
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

	public static MessageBuilder createFrom(Message message) {
		return new MessageBuilder(message.getSignal())//
				.withContent(message.getContent());
	}

	public static class MessageBuilder {

		private Message message = new Message();

		private MessageBuilder(Signal signal) {
			assert (signal != null);
			message.signal = signal.name();
		}

		public MessageBuilder withContent(String content) {
			message.content = content;
			return this;
		}

		public MessageBuilder member(Member member) {
			message.member = new MessageMember(member);
			return this;
		}

		public Message build() {
			return message;
		}

	}

}
