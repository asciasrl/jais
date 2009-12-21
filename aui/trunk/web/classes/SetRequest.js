if (!AUI.SetRequest) {

	AUI.SetRequest = {
		rpc: new JSONRpcClient("/aui/rpc")
	};
	
	AUI.SetRequest.set = function(device, value, status) {
		try {
			if (status == undefined) {
				status = value;
			}
			var control = device.getControl();
			var address = control.address;
			AUI.Logger.info("writePort: "+address+"="+value);
			if (this.rpc.AUI.writePortValue(address,value)) {
				device.setStatus(status);
				return true;
			} else {
				AUI.Header.show("Errore di collegamento");				
				return false;
			}
		} catch(e) {
			AUI.Header.show(e.msg);				
			throw(e);
		};
	};

	AUI.SetRequest.setValue = function(address, value) {
		try {
			AUI.Logger.info("writePort: "+address+"="+value);
			return this.rpc.AUI.writePortValue(address,value);
		} catch(e) {
			AUI.Header.show(e.msg);				
			throw(e);
		};
	};

}
