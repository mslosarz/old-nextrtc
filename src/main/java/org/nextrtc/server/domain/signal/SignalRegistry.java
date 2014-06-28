package org.nextrtc.server.domain.signal;

import static org.springframework.util.StringUtils.isEmpty;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.nextrtc.server.dao.Conversations;
import org.nextrtc.server.domain.Conversation;
import org.nextrtc.server.domain.Member;
import org.nextrtc.server.domain.Message;
import org.nextrtc.server.domain.RequestContext;
import org.nextrtc.server.exception.ConversationExists;
import org.nextrtc.server.exception.ConversationNotFoundException;

import com.google.common.base.Optional;

public class SignalRegistry {
	private static final Logger log = Logger.getLogger(SignalRegistry.class);

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

				return conversation.joinOwner(owner);
			}

			private Conversation createConversation(Message message, RequestContext requestContext) {
				String conversationId = message.getContent();
				Conversations conversations = requestContext.getConversations();

				if (isEmpty(conversationId)) {
					return conversations.create();
				} else if (conversations.findBy(conversationId).isPresent() == false) {
					return conversations.create(conversationId);
				}
				throw new ConversationExists("Conversation " + conversationId + " exists!");
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

				if (conversation.isPresent() == false) {
					return create.execute(member, message, requestContext);
				}
				return conversation.get().join(member);
			}

			protected Optional<Conversation> fetchConversationBy(Message message, RequestContext requestContext) {
				return requestContext.getConversations().findBy(message.getContent());
			}
		},

		/**
		 * After creating local peer, and connect local media each requested
		 * member send their offer to server<br>
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
		 * After creating local peer, and connect local media, Bob replay for
		 * offer and send the reply to server <br>
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

		/*
		 * outgoing
		 */
		created,

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
			log.debug(String.format("Executing: %s (%s) for %s", this.name(), message, member));
		}

		protected Conversation getConversation(Member member, RequestContext requestContext) {
			for (Conversation conv : requestContext.getConversations().findBy(member).asSet()) {
				return conv;
			}
			throw new ConversationNotFoundException();
		}

		protected void updateMemberName(Member member, Message message, RequestContext requestContext) {
			requestContext.getMembers().updateNick(member, message.getMemberName());
		}
	}

	private static Map<String, Signal> signals = new ConcurrentHashMap<>();

	static {
		for (Signal signal : DefaultSignal.values()) {
			register(signal);
		}
	}

	public static Signal get(String signal) {
		if (signal != null) {
			return signals.get(signal);
		}
		return null;
	}

	public static void register(Signal signal) {
		signals.put(signal.name(), signal);
	}

	public static boolean isValid(String incoming) {
		if (incoming != null) {
			return signals.containsKey(incoming);
		}
		return false;
	}

	public static void unregister(String signal) {
		signals.remove(signal);
	}

	public static void unregisterAll() {
		signals.clear();
	}
}
