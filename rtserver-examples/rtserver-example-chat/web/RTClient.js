RTClient = {};

RTClient.Socket = function(config) {

	this.path = config.path;

	if (window.WebSocket !== undefined) {
		this.socket = new WebSocket('ws://' + config.host + config.path);
		setHandlers.call(this);
	} else if (window.MozWebSocket !== undefined) {
		this.socket = new MozWebSocket('ws://' + config.path);
		setHandlers.call(this);
	} else {
		this.socket = new RTClient.JQueryLongPollSocket(config);
	}

	function setHandlers() {
		if (config.onOpen) this.socket.onopen = config.onOpen;
		if (config.onMessage) this.socket.onmessage = config.onMessage;
		if (config.onClose) this.socket.onclose = config.onClose;
		if (config.onError) this.socket.onerror = config.onError;
	}
}

RTClient.Socket.prototype = {
	
	send: function(data) {
		this.socket.send(data);
	},
	
	setOnMessage: function(handler) {
		this.socket.onmessage = handler;
	}
};

RTClient.Socket._logError = function(msg) {
	if (window.console) console.error(msg);
};


RTClient.HttpSocket = function(config) {
	this.host = config.host;
	this.path = config.path;
	if (config.onOpen) this.onopen = config.onOpen;
	if (config.onMessage) this.onmessage = config.onMessage;
	if (config.onClose) this.onclose = config.onClose;
	if (config.onError) this.onerror = config.onError;

	this._connect();
};

RTClient.HttpSocket.prototype._getXHRObject = function() {

	if (window.XMLHttpRequest) {
		return new window.XMLHttpRequest;
	}
	else {
		try {
			return new ActiveXObject("Msxml2.XMLHTTP.6.0");
		}
		catch(ex1) {
			try {
				return new ActiveXObject("Msxml2.XMLHTTP.3.0");
			} catch (ex2) {
				try {
					return new ActiveXObject("Microsoft.XMLHTTP");
				} catch (ex3) {
					throw new Error('Browser does not support XMLHttpRequest.');
				}
			}
		}
	}
};

RTClient.HttpSocket.prototype._connect = function() {
	var self = this;
	self._sendRequest({
		path: self.path,
		type: 'GET',
		headers: {
			'X-Initial-Connection': 'true'
		},
		complete: function(res) {
			self.connectionId = res.data;
			self._sendLongPollRequest();
			if (self.onopen) self.onopen();
		}
	});
};

RTClient.HttpSocket.prototype._sendLongPollRequest = function() {

	var self = this;
	this._sendRequest({
		path: self.path,
		type: 'GET',
		complete: function(res) {
			self._sendLongPollRequest();
			if (self.onmessage) self.onmessage({data: res.data});
		}
	});
};

RTClient.HttpSocket.prototype._sendRequest = function(config) {

};
/**
 * Sends a request using an XMLHttpRequest object.
 * @param config Object of configuration items. Possible values:
 * path - The path the request should be sent to. (Required)
 * type - The type of request to make. E.g. GET, POST... (Required)
 * headers - An headers to be sent with the request. (Optional)
 * complete - Function that is executed once the request completes. (Optional)
 */
RTClient.HttpSocket.prototype._sendRequest = function(config) {

	var self = this,
		xhr = self._getXHRObject();

	xhr.open(config.type, config.path, true);

	if (config.headers) {
		for (header in config.headers) {
			xhr.setRequestHeader(header, config.headers[header]);
		}
	}
	xhr.onreadystatechange = function() {

		if (xhr.readyState === 4) {
			if (config.complete) {

				var strResHeaders = xhr.getAllResponseHeaders();
				var objResHeaders = new Object();

				if (strResHeaders) {
					// Generate a response headers object for easy access
					var arrResHeaders = strResHeaders.split('\r\n'); // Response headers are separated by CRLF
					for (var i = 0; i < arrResHeaders.length; i++) {
						// Split the header into key value. Position 0 holds key, position 1 holds value
						var splitHeader = arrResHeaders[i].split(':');
						objResHeaders[splitHeader[0]] = splitHeader[1];
					}
				}

				config.complete({
					status: xhr.status,
					data: xhr.responseText,
					headers: objResHeaders,
					xhr: xhr
				});
			}
		}
	};

	xhr.send((config.data) ? config.data : null);
};

RTClient.HttpSocket.prototype.send = function(data) {
	var self = this;
	self._sendRequest({
		path: self.path,
		type: 'POST',
		data: data,
		headers: {
			'X-Connection-Id': self.connectionId
		},
		complete: function(res) {
			if (res.status === 200) {
				// We should only call onmessage if
				// a. An onmessage handler has been specified
				// b. We haven't been given a header indicating this is an empty response. The empty response
				// header is used to mimic the ability of a socket being able to send a request without
				// receiving a response.
				if (self.onmessage && res.headers['X-Empty-Response'] !== 'true') self.onmessage({data: res.data});
			}
		}
	});
}

RTClient.JQueryLongPollSocket = function(config) {

	var self = this;

	this.host = config.host;
	this.path = config.path;
	if (config.onOpen) this.onopen = config.onOpen;
	if (config.onMessage) this.onmessage = config.onMessage;
	if (config.onClose) this.onclose = config.onClose;
	if (config.onError) this.onerror = config.onError;

	this._connect();
	
	window.onbeforeunload = function() {
		self._writeXhr.abort();
	}
};

RTClient.JQueryLongPollSocket.prototype._connect = function() {

	var self = this;
	
	self._writeXhr = $.ajax({
		url: self.path,
		type: 'GET',
		beforeSend: function(xhr) {
			xhr.setRequestHeader('X-Initial-Connection', 'true');
		},
		success: function(data, textStatus, jqXHR) {
			self.connectionId = data;
			self._sendLongPollRequest();
			if (self.onopen) self.onopen();
		}
	});
};

RTClient.JQueryLongPollSocket.prototype._sendLongPollRequest = function() {

	var self = this;
	self._writeXhr = $.ajax({
		url: self.path,
		type: 'GET',
		beforeSend: function(xhr) {
			xhr.setRequestHeader('X-Connection-Id', self.connectionId);
		},
		success: function(data, textStatus, jqXHR) {
			self._sendLongPollRequest();

			var timeout = jqXHR.getResponseHeader("X-Timeout");

			if (!timeout || timeout !== "true") {
				// We should be receiving a JSON array containing our messages.
				for (var i = 0; i < data.length; i++) {
					if (self.onmessage) self.onmessage({data: data[i]});
				}
			}
		},
		complete: function(jqXHR, textStatus) {

			// For all none successful requests, just re-send the long poll request
			if (textStatus !== "success") {
				self._sendLongPollRequest();
			}
		}
	});
};

RTClient.JQueryLongPollSocket.prototype.send = function(data) {

	var self = this;
	$.ajax({
		url: self.path,
		type: 'POST',
		data: data,
		beforeSend: function(xhr) {
			xhr.setRequestHeader('X-Connection-Id', self.connectionId);
		},
		success: function(data, textStatus, jqXHR) {
			if (this.onmessage) this.onmessage(data);
			if (self.onmessage && jqXHR.getResponseHeader('X-Empty-Response') !== 'true')
				self.onmessage({data: data});
		}
	});
};

/** 
 * Creates a JSON RPC object which can be used to send JSON RPC
 * requests and register JSON RPC listeners.
 * @param socket The socket used to communicate with the server.
 */
RTClient.JsonCommunicator = function(socket) {
	
	var self = this;
	self._socket = socket;
	self._currentRequestId = 0;
	self._responseHandlers = {};
	self._listeners = {};
	
	self._socket.setOnMessage(function(event) {
	
		var message = JSON.parse(event.data);
		var id = message.id;
		
		// If a c (channel) property is specified, the server sent a message from a channel.
		// See if we have a listener listening for the specified channel.
		if (message.c) {
			if (self._listeners[message.c]) {
				self._listeners[message.c](message.d);
			}
		}
		
		// If a r (result) property is specified, it was a successful RPC call.
		if (message.r) {
		
			// If a success handler was specified for the given request id,
			// call the success handler.
			if (self._responseHandlers[id] && self._responseHandlers[id].success) {
				self._responseHandlers[id].success(message.result);
			}
			
			// We are done with this request, remove it from the object holding response handlers.
			delete self._responseHandlers[id];
		
		// If an error object is specified, it was an unsuccessful RPC call.
		} else if (message.e) {
		
			// If an error handler was specified for the given resquest id,
			// call the error handler.
			if (self._responseHandlers[id] && self._responseHandlers[id].error) {
				self._responseHandlers[id].error(message.error.message, message.error.code);
			}
			
			// We are done with this request, remove it from the object holding response handlers.
			delete self._responseHandlers[id];
		}
	});
};

RTClient.JsonCommunicator.prototype = {
	
	/**
	 * Send a JSON RPC request.
	 * @param config The configuration options for the request. Options:
	 * 		method The method name of the RPC request.
	 * 		params The parameters for the RPC request.
	 * 		success Callback called on a successful response. The parameter
	 * 		        provided to the callback is a single object containing the
	 * 		        the response parameters.
	 * 		error Callback called on an error response. The parameters provided
	 * 		to the callback is the message and error code (in that order).
	 */
	sendRpc: function(config) {
		var self = this,
			id = self._currentRequestId.toString(),
			request = {
				m: config.method,
				p: config.params,
				i: id
			};
		self._responseHandlers[id] = {success: config.success, error: config.error};
		self._socket.send(JSON.stringify(request));
		self._currentRequestId++;
	},
	
	/**
	 * Listens for JSON messages sent from the specified channel.
	 * @param channel The channel the message came from.
	 * @param handler A handler to be executed when a message is
	 * received from the specified channel.
	 */
	listen: function(channel, handler) {
		var self = this;
		self._listeners[channel] = handler;
	}
};