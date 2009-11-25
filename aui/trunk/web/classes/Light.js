if (!AUI.Light) {
	
	AUI.Light = function(id) {
		this.id = id;
	};
	
	AUI.Light.prototype = new AUI.Device();
	
	AUI.Light.prototype.setPortValue = function(port,newValue) {
		this.value = newValue;
		if (newValue == true || newValue == "true" || newValue == "on") {
			this.setStatus("on");
		} else if (newValue == false || newValue == "false" ||newValue == "off") {
			this.setStatus("off");
		}		
	};
	
	AUI.Light.prototype.onTouchStart = function(event) {
		if (event.preventDefault) {
			event.preventDefault();
		} else {
			event.returnValue = false;
		}
		if (event.stopPropagation) {
			event.stopPropagation();
		} else if (window.event) {
			window.event.cancelBubble = true;
		} 
		var status = this.status;
		var newstatus = status;
		if (status == "on") {
			newstatus = "off";
		} else {
			newstatus = "on";
		}
		AUI.SetRequest.set(this,newstatus);
	};
	
}