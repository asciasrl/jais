if (!AUI.StreamRequest) {
	
	AUI.StreamRequest = {
		streamReq: null,
		running: false,
		requestCounter: 0,
		sendTimeout: 3000,
		failCounter: 0,
		retryTimeout: 1000,
		maxRetryTimeout: 60000,
		updateTimeout: 10000,
		timeoutTimer: 0,
		streamStart: 0,
		eventCounter: 0,
		errorCounter: 0
	};
	
	AUI.StreamRequest.start = function() {
		var self = AUI.StreamRequest;
		AUI.Header.show("Connessione in corso ... "+self.requestCounter);
		clearInterval(self.timeoutTimer);
		if (self.streamReq == null) {
			self.streamReq = AUI.Http.getRequest();
			//this.streamReq.onreadystatechange = function(e) { return self.onReadyStateChange() };
			AUI.Logger.info("Start: instanziato streamReq ");		
		} else if (self.streamReq.readyState >= 1) {
			AUI.Logger.info("Start: abort request readyState="+this.streamReq.readyState);		
			self.streamReq.onreadystatechange = function() {};
			self.streamReq.abort();
			//return; // Ci pensa onReadyStateChange a richiamare start
			self.streamReq = AUI.Http.getRequest();
			self.streamReq.onreadystatechange = self.onReadyStateChange;
		}
		self.requestCounter++;
		//AUI.Logger.info("Start: doing open()");		
		self.streamReq.open('GET', 'stream/'+AUI.Pages.getCurrentPageId()+"?c="+self.requestCounter, true);
		//AUI.Logger.info("Start: done open()");		
		//this.streamReq.timeout = this.sendTimeout;
		//this.streamReq.ontimeout = function() { return self.onTimer() };
		self.running = true;
		self.streamReq.onreadystatechange = self.onReadyStateChange;
		self.streamReq.send(false);
		//AUI.Logger.info("Start: done send()");		
		self.streamStart = 0;
		self.eventCounter = 0;
		self.timeoutFunction = function() { return self.onTimer(); };
		self.timeoutTimer = setTimeout(self.timeoutFunction,self.sendTimeout);
		AUI.Logger.info("Aperto stream c="+self.requestCounter);		
	};
	
	AUI.StreamRequest.onTimer = function() {
		var self = AUI.StreamRequest;
		AUI.Logger.error("Timeout StreamRequest, readyState="+self.streamReq.readyState);
		if (self.streamReq >= 1) {
			self.streamReq.abort();
		} else {
			if (self.running) {
				self.start();
			}
		}
	};

	AUI.StreamRequest.stop = function() {
		var self = AUI.StreamRequest;
		self.running = false;
		clearInterval(self.timeoutTimer);
		if (self.streamReq == null) {
			return;
		}
		AUI.Logger.info("Stop StreamRequest, readyState="+self.streamReq.readyState);
		if (self.streamReq >= 1) {
			self.streamReq.abort();
		}
	};

	AUI.StreamRequest.onReadyStateChange = function() {
		var self = AUI.StreamRequest;
		var streamReq = self.streamReq;
		AUI.Logger.info("stateChange, readyState="+streamReq.readyState);
		clearInterval(self.timeoutTimer);
		if (((streamReq.readyState == 3) || (streamReq.readyState == 4)) && (streamReq.status == 200)) {
			self.failCounter = 0;
			var res = streamReq.responseText.substring(self.streamStart);
			var i = 0;
			while ((i = res.indexOf("\n")) > 0) {
				var res1 = res.substring(0, i);
				res = res.substring(i+1);
				self.streamStart += i+1;
				self.eventCounter++;
				try {
					AUI.Logger.debug("evt="+res1);
					//AUI.Logger.log(self.eventCounter+"/"+self.errorCounter+":"+res1);
					var evt;
					eval("evt="+res1+";");
					if (evt) {
						if (evt.ERROR != undefined) {
							AUI.Header.show(evt.ERROR);
						} else {
							AUI.Controls.fireDevicePortChangeEvent(evt);
						}
					}
				} catch (e) {
					self.errorCounter++;
					// TODO: handle exception
					AUI.Logger.error(e);
				}
			}
			//AUI.Logger.info("stateChange, setting timeout "+(self.sendTimeout + self.updateTimeout));
			self.timeoutTimer = setTimeout(self.timeoutFunction,self.sendTimeout + self.updateTimeout);		
			//AUI.Logger.info("stateChange, setted timeout");
		}
		if (streamReq.readyState == 4) {
			//AUI.Logger.info("stateChange, readyState=4: start()");
			self.streamReq = null;
			if (self.running) {
				var t = self.failCounter * self.retryTimeout;
				if (t > self.maxRetryTimeout) {
					t = self.maxRetryTimeout;
				}
				if (t > 0) {
					AUI.Header.show("Tentativo riconessione fra "+(t/1000)+ " secondi");
				}
				setTimeout(self.start,t);
				self.failCounter++;
			}
		}
	};
	
};