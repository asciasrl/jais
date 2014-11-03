if (!AUI.StreamWebSocket) {
	
  var isSupportedWebSocket = ("WebSocket" in window && window.WebSocket != undefined);

  /* This line exists because my Galaxy Tab 2 would otherwise appear to have support. */
  if (isSupportedWebSocket && navigator.userAgent.indexOf("Android") > 0 && navigator.userAgent.indexOf("Chrome") == -1)
		isSupportedWebSocket = false;
  
  if (isSupportedWebSocket) {
	
	AUI.StreamWebSocket = {
		ws: null,
		connected: false,
		requestCounter: 0,
		sendTimeout: 3000,
		failCounter: 0,
		retryTimeout: 1000,
		maxRetryTimeout: 60000,
		updateTimeout: 10000,
		timeoutTimer: 0,
		streamStart: 0,
		eventCounter: 0,
		errorCounter: 0,
		
		start : function() {
			this.requestCounter++;
			clearInterval(this.timeoutTimer);
			AUI.Header.show("Connecting WebSocket ... n."+this.requestCounter);
			if (this.ws == null) {
				var loc = window.location, ws_uri;
				if (loc.protocol === "https:") {
				    ws_uri = "wss:";
				} else {
				    ws_uri = "ws:";
				}
				ws_uri += "//" + loc.host;
				ws_uri += "/ws";
				AUI.Logger.info("Start: WebSocket "+ws_uri);		
				this.ws = new WebSocket(ws_uri);
				this.ws.onclose = function(event) { return AUI.StreamWebSocket.onClose(event); };
				this.ws.onmessage = function(event) { return AUI.StreamWebSocket.onMessage(event); };
				this.ws.onerror = function(event) { return AUI.StreamWebSocket.onError(event); };
				this.ws.onopen = function() { return AUI.StreamWebSocket.onOpen(); };
				this.timeoutTimer = setTimeout(function() { return AUI.StreamWebSocket.onTimeout();},this.sendTimeout + this.updateTimeout);		
			} else {
				AUI.Logger.info("Start: abort request");		
				this.ws.close();
				this.ws = null;
				this.start();
				return;
			}
			this.eventCounter = 0;
			AUI.Logger.info("Aperto websocket n."+this.requestCounter);		
		},
		
		onTimeout : function() {
			clearInterval(this.timeoutTimer);
			this.failCounter++;
			AUI.Logger.error("Timeout StreamWebSocket failCounter="+this.failCounter);
			this.stop();
			
			var t = this.failCounter * this.retryTimeout;
			if (t > this.maxRetryTimeout) {
				t = this.maxRetryTimeout;
			}
			if (t > 0) {
				AUI.Header.show("Reconnecting WebSocket in "+(t/1000)+ " seconds");
			}
			setTimeout(function() { return AUI.StreamWebSocket.start(); },t);
		},
		
		onOpen : function() {
			this.connected = true;
			this.failCounter = 0;
			AUI.Logger.debug("onOpen " + this.ws + " send: "+AUI.Pages.getCurrentPageId());
			this.ws.send(AUI.Pages.getCurrentPageId());
			AUI.Header.show("Connected WebSocket n."+this.requestCounter);
		},
	
		onClose : function(event) {
			this.ws = null;
			AUI.Header.show("Connection WebSocket closed "+event.reason);
		},
	
		onMessage : function(event) {
			clearInterval(this.timeoutTimer);
			this.timeoutTimer = setTimeout(function() { return AUI.StreamWebSocket.onTimeout();},this.sendTimeout + this.updateTimeout);		
			var data = event.data;
			AUI.Logger.debug("Event data:" + data);
			this.eventcounter++;
			var evt = null;
			eval("evt="+data+";");
			if (evt) {
				if (evt.ERROR != undefined) {
					AUI.Header.show(evt.ERROR);
				} else {
					AUI.Controls.fireDevicePortChangeEvent(evt);
				}
			}
		},
		
		onError : function(event) {
			AUI.Logger.error("onError event=" + event);
		},

		stop : function() {
			if (this.ws == null) {
				return;
			}
			AUI.Logger.info("Stop StreamWebSocket, readyState="+this.ws.readyState);
			this.ws.close();
			this.ws = null;
		}	
	};
  }	
}