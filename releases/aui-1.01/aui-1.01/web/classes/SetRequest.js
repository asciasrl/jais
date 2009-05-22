if (!AUI.SetRequest) {

	AUI.SetRequest = {
			request: AUI.Http.getRequest(),
			sending: false
	};
	
	AUI.SetRequest.send = function(address, value) {
		if (this.sending) {
			AUI.Header.show("Richiesta in corso.");
			return false;
		}
		try {
			this.sending = true;
			this.address = address;
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
				AUI.Header.show(request.responseText);
				if (request.responseText.indexOf("ERROR") == 0) {
					AUI.Controls.revertStatus(AUI.SetRequest.address);					
				}
			} else {
				AUI.Controls.revertStatus(AUI.SetRequest.address);
				if (request.status == 500) {
					AUI.Header.show("Errore del server.");
				} else if (request.status == 400) {
					AUI.Header.show("Errore di comunicazione.");
				} else {
					AUI.Header.show("Errore di collegamento ("+request.status+")");
				}
			}
			AUI.SetRequest.sending = false;
		}		
	};
	
	AUI.SetRequest.abort = function() {
		this.request.abort();
		this.sending = false;
	};
	
	AUI.SetRequest.timeoutExpired = function() {
		AUI.SetRequest.abort();
	};
}
