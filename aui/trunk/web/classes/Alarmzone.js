if (!AUI.Alarmzone) {

	AUI.Alarmzone = function(id) {
		this.id = id;
		this.bypassed = "";
		this.zonestatus = "off";
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
		// prova 2 volte
		if (AUI.SetRequest.setValue(address,newstatus) || AUI.SetRequest.setValue(address,newstatus)) {
			return true;
		} else {
			AUI.Header.show("Errore di comunicazione, riprovare.");				
			return false;
		}
	};
	
	AUI.Alarmzone.prototype.setPortValue = function(port,newValue) {
		this.value = newValue;
		if (newValue == true || newValue == "true" || newValue == "on") {
			if (port == "Status" || port == "Stato") {
				this.zonestatus = "on";
			} else if (port == "Bypass") {
				AUI.Logger.info("Bypassed on");
				this.bypassed = "bypassed_";
			} else {
				AUI.Logger.error("Port not recognized:"+port);
			}
		} else if (newValue == false || newValue == "false" ||newValue == "off") {
			if (port == "Status" || port == "Stato") {
				this.zonestatus = "off";
			} else if (port == "Bypass") {
				AUI.Logger.info("Bypassed off");
				this.bypassed = "";
			} else {
				AUI.Logger.error("Port not recognized:"+port);
			}
		}		
		AUI.Logger.info("Set status: '" + this.bypassed + "' '"+this.zonestatus+"'");
		this.setStatus(this.bypassed + this.zonestatus);				
	};

}
