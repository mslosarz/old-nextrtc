package org.nextrtc.server.domain.signal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.nextrtc.server.domain.Conversation;
import org.nextrtc.server.domain.Member;
import org.nextrtc.server.domain.Message;
import org.nextrtc.server.domain.RequestContext;
import org.nextrtc.server.domain.SignalResponse;
import org.nextrtc.server.exception.ConversationNotFoundException;

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

				Conversation conversation = requestContext.getConversationDao().create();

				return conversation.joinOwner(owner);
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

				Conversation conversation = fetchConversation(member, requestContext);

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

				Conversation conversation = fetchConversation(member, requestContext);

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

				Conversation conversation = fetchConversation(member, requestContext);

				return conversation.disconnect(member);
			}
		};

		@Override
		public SignalResponse execute(Member member, Message message, RequestContext requestContext) {
			return SignalResponse.EMPTY;
		}

		protected void logRequest(Message message, Member member) {
			log.debug(String.format("Executing: %s (%s) for %s", this.name(), message, member));
		}

		protected Conversation fetchConversation(Member member, RequestContext requestContext) {
			Conversation conversation = requestContext.getConversationDao().findBy(member);
			if (conversation == null) {
				throw new ConversationNotFoundException();
			}
			return conversation;
		}

		protected Conversation fetchConversation(Message message, RequestContext requestContext) {
			Conversation conversation = requestContext.getConversationDao().findBy(message.getContent());
			if (conversation == null) {
				throw new ConversationNotFoundException();
			}
			return conversation;
		}

		protected void updateMemberName(Member member, Message message, RequestContext requestContext) {
			requestContext.getMemberDao().updateNick(member, message.getMemberName());
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
