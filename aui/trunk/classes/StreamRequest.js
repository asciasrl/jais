if (!AUI.StreamRequest) {
	
	AUI.StreamRequest = {
		streamReq: new XMLHttpRequest(),
		streamStart: 0,
		eventCounter: 0,
		errorCounter: 0
	};
	
	AUI.StreamRequest.start = function() {
		if (this.streamReq.readyState != 4) {
			this.streamReq.onreadystatechange = null;
			this.streamReq.abort();
		}
		this.streamReq.open('GET', 'stream/'+AUI.Page.getCurrentPageId(), true);
		this.streamReq.send(null);
		this.streamReq.onreadystatechange = AUI.StreamRequest.onReadyStateChange;
		this.streamStart = 0;
		this.eventCounter = 0;
		AUI.Logger.info("Aperto stream");
	};
	
	AUI.StreamRequest.onReadyStateChange = function() {
		var self = AUI.StreamRequest;
		var streamReq = self.streamReq;
		AUI.Logger.log("readyState:"+streamReq.readyState);
		if ((streamReq.readyState == 3 || streamReq.readyState == 4) && streamReq.status == 200) {
			var res = streamReq.responseText.substring(self.streamStart);
			var i = 0;
			while ((i = res.indexOf("\n")) > 0) {
				var res1 = res.substring(0, i);
				res = res.substring(i+1);
				self.streamStart += i+1;
				self.eventCounter++;
				try {
					AUI.Logger.log(self.eventCounter+"/"+self.errorCounter+":"+res1);
					var evt;
					eval("evt="+res1+";");
					if (evt) {
						AUI.Controls.fireDevicePortChangeEvent(evt);
					}
				} catch (e) {
					self.errorCounter++;
					// TODO: handle exception
					AUI.Logger.error(e);
				}			
			}
		}
		if (streamReq.readyState == 4) {
			AUI.Logger.info("in processStreamResponse eseguo sendStreamRequest()");
			self.start();		
		}
	}
	
};