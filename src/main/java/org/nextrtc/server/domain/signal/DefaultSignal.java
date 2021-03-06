package org.nextrtc.server.domain.signal;

import static org.springframework.util.StringUtils.isEmpty;

import org.nextrtc.server.dao.Conversations;
import org.nextrtc.server.domain.*;
import org.nextrtc.server.exception.ConversationExists;
import org.nextrtc.server.exception.ConversationNotFoundException;
import org.nextrtc.server.factory.ConversationFactory;

import com.google.common.base.Optional;

public enum DefaultSignal implements Signal {
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
		public SignalResponse execute(Member owner, Message message, RequestContext requestContext) {
			logRequest(message, owner);

			updateMemberName(owner, message, requestContext);

			Conversation conversation = createConversation(message, requestContext);

			registerConversation(conversation, requestContext);

			return conversation.joinOwner(owner);
		}

		private Conversation createConversation(Message message, RequestContext requestContext) {
			String conversationId = message.getContent();
			Conversations conversations = requestContext.getConversations();

			ConversationFactory factory = requestContext.getConversationFactoryResolver().getDefaultOrBy(message.getType());
			if (isEmpty(conversationId)) {
				return factory.create();
			} else if (conversations.findBy(conversationId).isPresent() == false) {
				return factory.create(conversationId);
			}
			throw new ConversationExists("Conversation " + conversationId + " exists!");
		}

		private void registerConversation(Conversation conversation, RequestContext requestContext) {
			requestContext.getConversations().save(conversation);
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

			Optional<Conversation> conversation = fetchConversationBy(message, requestContext);

			return conversation.get().join(member);
		}

		protected Optional<Conversation> fetchConversationBy(Message message, RequestContext requestContext) {
			return requestContext.getConversations().findBy(message.getContent());
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

			Conversation conversation = getConversation(member, requestContext);

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

			Conversation conversation = getConversation(member, requestContext);

			return conversation.routeAnswer(member, message);
		}
	},

	ping {
		@Override
		public SignalResponse execute(Member member, Message message, RequestContext requestContext) {
			return new SignalResponse(Message//
					.createWith(ping)//
					.withMember(member)//
					.build(), member);
		}
	},

	candidate {
		@Override
		public SignalResponse execute(Member member, Message message, RequestContext requestContext) {
			logRequest(message, member);

			Member requestedMember = requestContext.getMembers().findBy(message.getMemberId()).get();
			return new SignalResponse(//
					Message.createWith(candidate)//
							.withMember(member)//
							.withContent(message.getContent())//
							.build(), requestedMember);
		}
	},

	/*
	 * outgoing
	 */
	created,

	joined,

	offerRequest,

	answerRequest,

	finalize,

	/**
	 * Signal send to all member when someone leave the conversation<br>
	 * server -> All member<br>
	 * 
	 * {<br>
	 * 'signal':'left',<br>
	 * 'member':{'id':'bob-id', 'name':null},<br>
	 * 'content':null<br>
	 * }<br>
	 */
	left {
		@Override
		public SignalResponse execute(Member member, Message message, RequestContext requestContext) {
			logRequest(message, member);

			Conversation conversation = getConversation(member, requestContext);

			SignalResponse disconnect = conversation.disconnect(member);

			removeEmptyConversation(requestContext, conversation);

			return disconnect;
		}

		private void removeEmptyConversation(RequestContext requestContext, Conversation conversation) {
			if (conversation.isEmpty()) {
				requestContext.getConversations().remove(conversation);
			}
		}
	};

	@Override
	public SignalResponse execute(Member member, Message message, RequestContext requestContext) {
		return SignalResponse.EMPTY;
	}

	protected void logRequest(Message message, Member member) {
		// SignalRegistry.log.debug(String.format("Executing: %s (%s) for %s",
		// this.name(), message, member));
	}

	protected Conversation getConversation(Member member, RequestContext requestContext) {
		for (Conversation conv : requestContext.getConversations().findBy(member).asSet()) {
			return conv;
		}
		throw new ConversationNotFoundException();
	}

	protected void updateMemberName(Member member, Message message, RequestContext requestContext) {
		member.setName(message.getMemberName());
		requestContext.getMembers().update(member);
	}
}