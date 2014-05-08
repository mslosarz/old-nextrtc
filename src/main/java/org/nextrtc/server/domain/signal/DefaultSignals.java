package org.nextrtc.server.domain.signal;

import org.apache.log4j.Logger;
import org.nextrtc.server.domain.Conversation;
import org.nextrtc.server.domain.Member;
import org.nextrtc.server.domain.Message;
import org.nextrtc.server.domain.RequestContext;
import org.nextrtc.server.domain.SignalResponse;
import org.nextrtc.server.exception.ConversationNotFoundException;

public enum DefaultSignals implements Signal {
	/*
	 * incomming
	 */
	/**
	 * Alice -> server<br>
	 * 
	 * {<br>
	 * 'signal':'create',<br>
	 * 'member':{'id':null, 'name':'Alice'},<br>
	 * 'content':null<br>
	 * }<br>
	 * 
	 * server create new conversation <br>
	 * 
	 * server -> Alice<br>
	 * 
	 * {<br>
	 * 'signal':'created',<br>
	 * 'member':null,<br>
	 * 'content':'conversation-id'<br>
	 * }<br>
	 * 
	 */
	create {
		@Override
		public SignalResponse execute(Member member, Message message, RequestContext requestContext) {
			logRequest(message, member);

			updateMemberName(member, message, requestContext);

			Conversation conversation = requestContext.getConversationDao().create();

			Message response = Message//
					.createWith(created)//
					.withContent(conversation.getId())//
					.build();//

			return createSignalResponse(response, member);
		}
	},

	/**
	 * When conversation owner (Alice) share conversation id with other user
	 * (Bob)<br>
	 * Bob -> server<br>
	 * 
	 * {<br>
	 * 'signal':'join',<br>
	 * 'member':{'id':null, 'name':'Bob'},<br>
	 * 'content':'conversation-id'<br>
	 * }<br>
	 * 
	 * Server send to all member of given conversation offer request
	 * 
	 * server -> conversationMembers (especially to Alice)<br>
	 * 
	 * {<br>
	 * 'signal':'offerRequest',<br>
	 * 'member':{'id':'bob-id', 'name':'Bob'},<br>
	 * 'content':null<br>
	 * }<br>
	 * 
	 */
	join {
		@Override
		public SignalResponse execute(Member member, Message message, RequestContext requestContext) {
			logRequest(message, member);

			updateMemberName(member, message, requestContext);

			Conversation conversation = fetchConversation(message, requestContext);

			return conversation.join(member);
		}
	},

	/**
	 * After creating local peer, and connect local media each requested member
	 * send their offer to server<br>
	 * 
	 * Alice -> server<br>
	 * 
	 * {<br>
	 * 'signal':'offerResponse',<br>
	 * 'member':{'id':'bob-id', 'name':null},<br>
	 * 'content':'alice sdp local media description'<br>
	 * }<br>
	 * 
	 * server transmit given offer to Bob<br>
	 * 
	 * server -> Bob<br>
	 * 
	 * {<br>
	 * 'signal':'answerRequest',<br>
	 * 'member':{'id':'alice-id', 'name':'Alice'},<br>
	 * 'content':'alice sdp local media description'<br>
	 * }<br>
	 * 
	 */
	offerResponse {
		@Override
		public SignalResponse execute(Member member, Message message, RequestContext requestContext) {
			logRequest(message, member);

			Conversation conversation = fetchConversation(message, requestContext);

			return conversation.routeOffer(member, message);
		}
	},

	/**
	 * After creating local peer, and connect local media, Bob replay for offer
	 * and send the reply to server <br>
	 * 
	 * Bob -> server<br>
	 * 
	 * {<br>
	 * 'signal':'answerResponse',<br>
	 * 'member':{'id':'alice-id', 'name':null},<br>
	 * 'content':'bob sdp local media description'<br>
	 * }<br>
	 * 
	 * server -> Alice<br>
	 * 
	 * {<br>
	 * 'signal':'finalize',<br>
	 * 'member':{'id':'bob-id', 'name':null},<br>
	 * 'content':'bob sdp local media description'<br>
	 * }<br>
	 * 
	 */
	answerResponse {
		@Override
		public SignalResponse execute(Member member, Message message, RequestContext requestContext) {
			logRequest(message, member);

			Conversation conversation = fetchConversation(message, requestContext);

			return conversation.routeAnswer(member, message);
		}
	},

	/*
	 * outgoing
	 */
	created,

	offerRequest,

	answerRequest,

	finalize,

	;

	@Override
	public SignalResponse execute(Member member, Message message, RequestContext requestContext) {
		return SignalResponse.EMPTY;
	}

	public static boolean isValid(String incoming) {
		for (DefaultSignals signal : values()) {
			if (signal.name().equals(incoming)) {
				return true;
			}
		}
		return false;
	}

	protected void logRequest(Message message, Member member) {
		log.debug(String.format("Executing: %s (%s) for %s", this.name(), message, member));
	}

	private static SignalResponse createSignalResponse(Message response, Member... members) {
		SignalResponse result = new SignalResponse(response);
		for (Member member : members) {
			result.add(member);
		}
		return result;
	}

	private static void updateMemberName(Member member, Message message, RequestContext requestContext) {
		requestContext.getMemberDao().updateNick(member, message.getMemberName());
	}

	private static Conversation fetchConversation(Message message, RequestContext requestContext) {
		Conversation conversation = requestContext.getConversationDao().findBy(message.getContent());
		if (conversation == null) {
			throw new ConversationNotFoundException();
		}
		return conversation;
	}

	private static final Logger log = Logger.getLogger(DefaultSignals.class);
}
