if (!AUI.Controls) {
	
	AUI.Controls = {
		currentControlId : null,
		lastEvent : null,
		controls : null,
		addresses : null
	};

	AUI.Controls.onTouchStart = function(id,event) {
		event.preventDefault();
		event.stopPropagation();
		this.lastEvent = event;
		var device = this.getDevice(id);
		if (device) {
			device.onTouchStart(event);
		} else {
			AUI.Logger.error("Non trovato device: "+id);
		}
	}

	AUI.Controls.onMouseDown = function(id,event) {
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
		this.lastEvent = event;		
		var device = this.getDevice(id);
		if (device) {
			AUI.Logger.debug("Mousedown: "+id);
			device.onMouseDown(event);
		} else {
			AUI.Logger.error("Non trovato device: "+id);
		}
	};
	
	AUI.Controls.getControl = function(id) {
		return AUI.Controls.controls[id];		
	};
	
	AUI.Controls.getDevice = function(id) {
		var control = this.getControl(id);
		if (control == null) {
			return null;
		}
		var device = control.device;
		if (device == null) {
			var controlclass = control.controlclass;
			if (controlclass == null) {
				var str = control.type;
			    var f = str.charAt(0).toUpperCase();
			    controlclass = f + str.substr(1);
			}
			if (controlclass == "Page") {
				device = new AUI.Device(id);
			} else {
				eval("device = new AUI."+controlclass+"('"+id+"')");
			}
			if (device == null) {
				AUI.Logger.error("device non disponibile per controllo di tipo: "+control.type);
			} else {
				AUI.Logger.debug("Nuovo device "+control.type+" :"+id);
				this.controls[id].device = device;
			}
		}
		return device;
	};
	
	AUI.Controls.revertStatus = function(address) {
		AUI.Logger.info("revertStatus:"+address);
		var ids = this.addresses[address];
		if (ids == null) {
			return;
		}
		for (var i=0; i < ids.length; i++) {
			var id = ids[i];
			var device = this.getDevice(id);
			if (device == null) {
				AUI.Logger.error("Device non disponibile per "+id);
				return;
			}
			var newStatus = device.oldStatus;
			if (newStatus == null) {
				newStatus = "default";
			}
			var port = address.split(":")[1];
			try {
				device.setStatus(newStatus);
			} catch(e) {
				AUI.Logger.error("address:"+address);
				AUI.Logger.error(e);
			}
		}
	};
	
	AUI.Controls.fireDevicePortChangeEvent = function(devicePortChangeEvent) {
		var address = devicePortChangeEvent.A; 
		var ids = this.addresses[address];
		if (ids == null) {
			return;
		}
		for (var i=0; i < ids.length; i++) {
			var id = ids[i];
			var device = this.getDevice(id);
			if (device == null) {
				AUI.Logger.error("Device non disponibile per "+id);
				return;
			}
			var newValue = devicePortChangeEvent.V;
			var port = address.split(":")[1];
			try {
				AUI.Logger.info("setPortValue device="+id+" port="+port+" value="+newValue);
				device.setPortValue(port,newValue);
			} catch(e) {
				AUI.Logger.error("address:"+address);
				AUI.Logger.error(e);
			}
		}
	};

}