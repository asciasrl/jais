if (!AUI.Prealarm) {

	AUI.Prealarm = function(id) {
		this.id = id;
		this.rpc = new JSONRpcClient("/aui/rpc"); 
	}
	
	AUI.Prealarm.prototype = new AUI.Device();

	AUI.Prealarm.prototype.setPortValue = function(port,newValue) {
		this.value = newValue;
		this.setStatus(newValue);
		if (newValue == "true") {
			if (this.rpc.Alarm.isArmed()) {
				AUI.Keypad.show();
			}
		}
	}	
}
