/**
 * This library require adapter.js
 */
// 'use strict';
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

	this.mediaConfig = config.mediaConfig !== undefined ? config.mediaConfig : null;
	this.type = config.type;

	this.signaling = new WebSocket(config.wsURL);
	this.peerConnections = {};
	this.localStream = null;
	this.signals = {};

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
		nextRTC.member = member;
		navigator.getUserMedia(nextRTC.mediaConfig, function(stream) {
			nextRTC.localStream = stream;
			nextRTC.call('localStream', {
				stream : stream
			});
			nextRTC.request('join', member, convId, nextRTC.type);
		}, error);
	};

	this.create = function(member, convId) {
		var nextRTC = this;
		nextRTC.member = member;
		navigator.getUserMedia(nextRTC.mediaConfig, function(stream) {
			nextRTC.localStream = stream;
			nextRTC.call('localStream', {
				stream : stream
			});
			nextRTC.request('create', member, convId, nextRTC.type);
		}, error);
	};

	this.request = function(signal, member, convId, type) {
		var req = JSON.stringify(new Message(signal, member, convId, type));
		
		this.signaling.send(req);
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
		if (nextRTC.peerConnections[member.id] == undefined) {
			var pc = new RTCPeerConnection(config.peerConfig);
			pc.onaddstream = function(evt) {
				nextRTC.call('remoteStream', {
					member : member,
					stream : evt.stream
				});
			};
			pc.onicecandidate = function(evt) {
				handle(pc, evt);
				
				function handle(pc, evt){
					if((pc.signalingState || pc.readyState) == 'stable' 
						&& nextRTC.peerConnections[member.id]['rem'] == true){
						handleCandidate(nextRTC, evt.candidate, member, pc);
						return;
					} 
					setTimeout(function(){ handle(pc, evt); }, 2000);				
				}
			};
			nextRTC.peerConnections[member.id] = {}
			nextRTC.peerConnections[member.id]['pc'] = pc;
			nextRTC.peerConnections[member.id]['rem'] = false;
		}
		return nextRTC.peerConnections[member.id];
	};

	this.offerRequest = function(nextRTC, from) {
		nextRTC.offerResponse(nextRTC, from);
	};

	this.offerResponse = function(nextRTC, signal) {
		var pc = nextRTC.preparePeerConnection(nextRTC, signal.member);
		pc['pc'].addStream(nextRTC.localStream);
		pc['pc'].createOffer(function(desc) {
			pc['pc'].setLocalDescription(desc, function() {
				nextRTC.request('offerResponse', signal.member, desc.sdp);
			}, error, success);
		}, error);
	};

	this.answerRequest = function(nextRTC, signal) {
		nextRTC.answerResponse(nextRTC, signal);
	};

	this.answerResponse = function(nextRTC, signal) {
		var pc = nextRTC.preparePeerConnection(nextRTC, signal.member);
		pc['pc'].addStream(nextRTC.localStream);
		pc['pc'].setRemoteDescription(new RTCSessionDescription({
			type : 'offer',
			sdp : signal.content
		}), function() {
			pc['rem'] = true;
			pc['pc'].createAnswer(function(desc) {
				pc['pc'].setLocalDescription(desc, function() {
					nextRTC.request('answerResponse', signal.member, desc.sdp);
				}, error, success);
			}, success, nextRTC.mediaConfig.sdpConstraints);
		}, error);
	};

	this.finalize = function(nextRTC, signal) {
		var pc = nextRTC.preparePeerConnection(nextRTC, signal.member);
		pc['pc'].setRemoteDescription(new RTCSessionDescription({
			type : 'answer',
			sdp : signal.content
		}), function(){
			pc['rem'] = true;
		}, error);
	};

	this.close = function(nextRTC, event) {
		nextRTC.signaling.close();
	};
	
	this.candidate = function(nextRTC, signal) {
		var pc = nextRTC.preparePeerConnection(nextRTC, signal.member);
		pc['pc'].addIceCandidate(new RTCIceCandidate(JSON.parse(signal.content.replace(new RegExp('\'', 'g'), '"'))), success, error);
	}

	this.init = function() {
		this.on('created', this.created);
		this.on('offerRequest', this.offerRequest);
		this.on('answerRequest', this.answerRequest);
		this.on('finalize', this.finalize);
		this.on('candidate', this.candidate);
		this.on('close', this.close);
		this.on('ping', function(){});
	};

	function handleCandidate(nextRTC, candidate, member, destPC) {
		if (candidate) {
			nextRTC.request('candidate', member, JSON.stringify(candidate));
		}
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
	console.log('error ' + JSON.stringify(error));
};

var success = function(success) {
	// dummy success function
};
