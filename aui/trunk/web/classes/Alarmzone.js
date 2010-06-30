if (!AUI.Alarmzone) {

	AUI.Alarmzone = function(id) {
		this.id = id;
		this.bypass = false;
		this.rpc = null;
	};
	
	AUI.Alarmzone.prototype = new AUI.Light();

	AUI.Alarmzone.prototype.onTouchStart = function(event) {
		event.stopPropagation();
		event.preventDefault();
		var control = this.getControl();
		var address = control.address;
		var newstatus;
		if (this.bypassed == "") {
			newstatus = "on";
		} else {
			newstatus = "off";
		}
		AUI.SetRequest.setValue(address,newstatus);
	};
	
	AUI.Alarmzone.prototype.setPortValue = function(port,newValue) {
		this.value = newValue;
		if (newValue == true || newValue == "true" || newValue == "on") {
			if (port == "Status" || port == "Stato") {
				this.status = "on";
			} else if (port == "Bypassed") {
				this.bypassed = "bypassed_";
			} else {
				AUI.Logger.error("Port not recognized:"+port);
			}
		} else if (newValue == false || newValue == "false" ||newValue == "off") {
			if (port == "Status" || port == "Stato") {
				this.status = "off";
			} else if (port == "Bypassed") {
				this.bypassed = "";
			} else {
				AUI.Logger.error("Port not recognized:"+port);
			}
		}		
		this.setStatus(this.bypassed + this.status);				
	};

}
