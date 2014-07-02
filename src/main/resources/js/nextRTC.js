/**
 * This library require adapter.js
 */
navigator.getUserMedia = getUserMedia;

function Member(name) {
	this.id = null;
	this.name = name;
};

function Message(signal, member, content, type) {
	this.signal = signal;
	this.member = member;
	this.content = content;
	this.type = type;
};

function NextRTC(config) {

	if (NextRTC.instance == null) {
		NextRTC.instance = this;
	} else {
		return NextRTC.instance;
	}

	this.signaling = new WebSocket(config.wsURL);
	this.peerConnections = {};
	this.mediaConfig = config.mediaConfig;
	this.mediaAutoRequest = config.mediaAutoRequest !== undefined ? config.mediaAutoRequest
			: true;
	this.signals = {};
	this.localStream = null;
	this.type = config.type;

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
		var nextRTC = this;
		if (this.mediaAutoRequest) {
			navigator.getUserMedia(nextRTC.mediaConfig, function(stream) {
				nextRTC.localStream = stream;
				nextRTC.call('localStream', {
					stream : stream
				});
				nextRTC.request('join', member, convId, nextRTC.type);
			}, error);
		} else if(nextRTC.localStream == null) {
			console.log('You should provide NextRTC.instance.localStream before call this method');
		} else {
			nextRTC.request('join', member, convId, nextRTC.type);
		}
	};

	this.create = function(member, convId) {
		var nextRTC = this;
		if (this.mediaAutoRequest) {
			navigator.getUserMedia(nextRTC.mediaConfig, function(stream) {
				nextRTC.localStream = stream;
				nextRTC.call('localStream', {
					stream : stream
				});
				nextRTC.request('create', member, convId, nextRTC.type);
			}, error);
		}  else if(nextRTC.localStream == null) {
			console.log('You should provide NextRTC.instance.localStream before call this method');
		} else {
			nextRTC.request('create', member, convId, nextRTC.type);
		}
	};

	this.request = function(signal, member, convId, type) {
		this.signaling.send(JSON.stringify(new Message(signal, member, convId,
				type)));
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

	this.preparePeerConnection = function(nextRTC, member) {
		if (nextRTC.peerConnections[member.id] === undefined) {
			var pc = new RTCPeerConnection(config.peerConfig);
			pc.onaddstream = function(evt) {
				nextRTC.call('remoteStream', {
					member : member,
					stream : evt.stream
				});
			};
			nextRTC.peerConnections[member.id] = pc;
		}
		return nextRTC.peerConnections[member.id];
	};

	this.offerRequest = function(nextRTC, from) {
		nextRTC.preparePeerConnection(nextRTC, from.member);
		nextRTC.offerResponse(nextRTC, from);
	};

	this.offerResponse = function(nextRTC, signal) {
		var pc = nextRTC.preparePeerConnection(nextRTC, signal.member);
		pc.addStream(nextRTC.localStream);
		pc.createOffer(function(desc) {
			pc.setLocalDescription(desc);
			nextRTC.request('offerResponse', signal.member, desc.sdp);
		}, error);
	};

	this.answerRequest = function(nextRTC, signal) {
		nextRTC.preparePeerConnection(nextRTC, signal.member.id);
		nextRTC.answerResponse(nextRTC, signal);
	};

	this.answerResponse = function(nextRTC, signal) {
		var pc = nextRTC.preparePeerConnection(nextRTC, signal.member);
		pc.addStream(nextRTC.localStream);
		pc.setRemoteDescription(new RTCSessionDescription({
			type : 'offer',
			sdp : signal.content
		}), function() {
			pc.createAnswer(function(desc) {
				pc.setLocalDescription(desc);
				nextRTC.request('answerResponse', signal.member, desc.sdp);
			}, error);
		});
	};

	this.finalize = function(nextRTC, signal) {
		var pc = nextRTC.preparePeerConnection(nextRTC, signal.member);
		pc.setRemoteDescription(new RTCSessionDescription({
			type : 'answer',
			sdp : signal.content
		}));
	};

	this.close = function(nextRTC, event) {
		nextRTC.signaling.close();
	};

	this.init = function() {
		this.on('created', this.created);
		this.on('offerRequest', this.offerRequest);
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
