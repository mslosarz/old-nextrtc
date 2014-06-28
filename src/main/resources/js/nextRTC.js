/**
 * This library require adapter.js
 */
navigator.getUserMedia = getUserMedia;

function Member(name) {
	this.id = null;
	this.name = name;
};

function Message(signal, member, content) {
	this.signal = signal;
	this.member = member;
	this.content = content;
};

function NextRTC(config) {

	if (NextRTC.instance == null) {
		NextRTC.instance = this;
	} else {
		return NextRTC.instance;
	}

	this.peerConnections = {};
	this.signaling = new WebSocket(config.wsURL);
	this.mediaConfig = config.mediaConfig;
	this.mediaAutoRequest = config.mediaAutoRequest !== undefined ? config.mediaAutoRequest	: true;
	this.signals = {};
	this.conversationId = null;
	this.localStream = null;

	this.on = function(signal, operation) {
		this.signals[signal] = operation;
	};

	this.call = function(event, data) {
		for ( var signal in this.signals) {
			if (event === signal) {
				return this.signals[event](this, data);
			}
		}
		console.log('Event ' + event + ' do not have defined function');
	};

	this.join = function(member, convId) {
		this.signaling
				.send(JSON.stringify(new Message('join', member, convId)));
	};
	
	this.created = function(nextRTC, signal) {
		nextRTC.conversationId = signal.content;
	};
	
	this.preparePeerConnection = function(nextRTC, member) {
		if(nextRTC.peerConnections[member.id] === undefined){
			var pc = new RTCPeerConnection(config.peerConfig);
			pc.onaddstream = function(evt) {
				nextRTC.call('streamReceived', {member : member, stream : evt.stream});
			};
			nextRTC.peerConnections[member.id] = pc;
		}
		return nextRTC.peerConnections[member.id];
	};
	
	this.offerRequest = function(nextRTC, from) {
		nextRTC.preparePeerConnection(nextRTC, from.member);
		if(nextRTC.localStream === null){
			nextRTC.call('mediaRequest', from);
		} else {
			nextRTC.offerResponse(nextRTC, from);
		}
	};
	
	this.mediaRequest = function(nextRTC, signal) {
		if (nextRTC.mediaAutoRequest) {
			navigator.getUserMedia(nextRTC.mediaConfig, function(stream) {
				NextRTC.instance.localStream = stream;
				NextRTC.instance.call('mediaResponse', signal);
			}, error);
		} else {
			console.log('You should override method on(mediaRequest) to provide NextRTC.instance.localStream');
		}
	};
	
	this.mediaResponse = function(nextRTC, signal) {
		if(signal.signal === 'offerRequest'){
			nextRTC.offerResponse(nextRTC, signal);
		} else if (signal.signal === 'answerRequest') {
			nextRTC.answerResponse(nextRTC, signal);
		}
	};
	
	this.offerResponse = function(nextRTC, signal) {
		var pc = nextRTC.preparePeerConnection(nextRTC, signal.member);
		pc.addStream(nextRTC.localStream);
		pc.createOffer(function(desc) {
			pc.setLocalDescription(desc);
			nextRTC.signaling.send(JSON.stringify(new Message('offerResponse', signal.member, desc.sdp)));
		}, error);
	};
	
	this.answerRequest = function(nextRTC, signal) {
		nextRTC.preparePeerConnection(nextRTC, signal.member.id);
		if(nextRTC.localStream === null){
			nextRTC.call('mediaRequest', signal);
		} else {
			nextRTC.answerResponse(nextRTC, signal);
		}
	};
	
	this.answerResponse = function(nextRTC, signal) {
		var pc = nextRTC.preparePeerConnection(nextRTC, signal.member);
		pc.addStream(nextRTC.localStream);
		pc.setRemoteDescription(
				new RTCSessionDescription({
					type : 'offer',
					sdp : signal.content
				}), 
				function() {
					pc.createAnswer(function(desc) {
						pc.setLocalDescription(desc);
						nextRTC.signaling.send(JSON.stringify(new Message('answerResponse', signal.member, desc.sdp)));
			}, error);
		});
	};
	
	this.finalize = function(nextRTC, signal){
		var pc = nextRTC.preparePeerConnection(nextRTC, signal.member);
		pc.setRemoteDescription(new RTCSessionDescription({
			type : 'answer',
			sdp : signal.content
		}));
	};
	
	this.close = function(nextRTC, event){
		
	};
	
	this.signaling.onmessage = function(event) {
		var signal = JSON.parse(event.data);
		NextRTC.instance.call(signal.signal, signal);
	};

	this.signaling.onclose = function(event) {
		NextRTC.instance.call('close', event);
	};

	this.signaling.onerror = function(event) {
		NextRTC.instance.call('error', event);
	};

	this.init = function() {
		this.on('created', this.created);
		this.on('offerRequest', this.offerRequest);
		this.on('mediaRequest', this.mediaRequest);
		this.on('mediaResponse', this.mediaResponse);
		this.on('answerRequest', this.answerRequest);
		this.on('finalize', this.finalize);
		this.on('close', this.close);
	};

	this.init();
};

NextRTC.instance = null;

NextRTC.onReady = function() {
	console.log('It is highly recommended to override method NextRTC.onReady');
};

// it works for new Chrome, Opera and FF
if (document.addEventListener) {
	document.addEventListener('DOMContentLoaded', function() {
		NextRTC.onReady();
	});
}

var error = function(error) {
	console.log('error ' + error);
};

