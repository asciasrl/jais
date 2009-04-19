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
		event.preventDefault();
		event.stopPropagation();
		this.lastEvent = event;		
		var device = this.getDevice(id);
		if (device) {
			device.onMouseDown(event);
		} else {
			AUI.Logger.error("Non trovato device: "+id);
		}
	}
	
	AUI.Controls.getControl = function(id) {
		return AUI.Controls.controls[id];		
	}
	
	AUI.Controls.getDevice = function(id) {
		var control = this.getControl(id);
		if (control == null) {
			return null;
		}
		var device = control.device;
		if (device == null) {
			if (control.type == "dimmer") {
				device = new AUI.Dimmer(id);
			} else if (control.type == "light") {
				device = new AUI.Light(id);
			} else if (control.type == "power") {
				device = new AUI.Power(id);
			} else if (control.type == "blind") {
				device = new AUI.Blind(id);
			} else if (control.type == "thermo") {
				device = new AUI.Thermo(id);
			} else if (control.type == "webcam") {
				device = new AUI.Webcam(id);
			}
			if (device == null) {
				AUI.Logger.error("device non disponibile per controllo di tipo: "+control.type);
			} else {
				AUI.Logger.debug("Nuovo device "+control.type+" :"+id);
				this.controls[id].device = device;
			}
		}
		return device;
	}
	
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
				device.setPortValue(port,newValue);
			} catch(e) {
				AUI.Logger.error("address:"+address);
				AUI.Logger.error(e);
			}
		}
	}

}