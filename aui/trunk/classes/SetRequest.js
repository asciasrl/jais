if (!AUI.SetRequest) {

	AUI.SetRequest = {
			request: new XMLHttpRequest(),
			sending: false
	};
	
	AUI.SetRequest.send = function(address, value) {
		if (this.sending) {
			statusMessage("Richiesta in corso.");
			return false;
		}
		try {
			this.sending = true;
			this.request.open('GET', 'jais/set?'+address+'='+value, true);
			this.request.send(null);
			var self = this;
			this.request.onreadystatechange = self.stateChange;
			this.timeout = window.setTimeout(self.timeoutExpired, 3000);
			AUI.Logger.info("request: "+address+"="+value);
		} catch(e) {
			this.sending = false;
			throw(e);
		};
		return true;
	};
	
	AUI.SetRequest.stateChange = function() {
		var request = AUI.SetRequest.request; 
		if (request.readyState == 4) {
			AUI.Logger.debug("status="+request.status);
			clearInterval(AUI.SetRequest.timeout);
			if (request.status == 200) {
				statusMessage(request.responseText);
			} else if (request.status == 500) {
				statusMessage("Errore del server.");
			} else if (request.status == 400) {
				statusMessage("Errore di comunicazione.");
			} else {
				statusMessage("Errore di collegamento ("+this.request.status+")");
			}
			AUI.SetRequest.sending = false;
		}		
	};
	
	AUI.SetRequest.abort = function() {
		this.request.abort();
		this.sending = false;
	};
	
	AUI.SetRequest.timeoutExpired = function() {
		//alert(this);
		statusMessage("Timeout del collegamento.");
		AUI.SetRequest.abort();
	};
}
