if (!AUI.Light) {
	
	AUI.Light = function(id) {
		this.id = id;
	}
	
	AUI.Light.prototype = new AUI.Device();
	
	AUI.Light.prototype.setPortValue = function(port,newValue) {
		this.value = newValue;
		if (newValue == true || newValue == "on") {
			this.setStatus("on");
		} else if (newValue == false || newValue == "off") {
			this.setStatus("off");
		}		
	}
	
	AUI.Light.prototype.onTouchStart = function(event) {
		event.preventDefault();
		event.stopPropagation();		
		var status = this.status;
		var newstatus = status;
		var command = null;
		if (status == "on") {
			newstatus = "off";
			command = "false";
		} else {
			newstatus = "on";
			command = "true";
		}
		if (command) {
			var control = this.getControl();
			if (AUI.SetRequest.send(control.address,command)) {
				this.setStatus(newstatus);
			}
		}		
	}
	
}