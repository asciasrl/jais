if (!AUI.Pushbutton) {
	
	AUI.Pushbutton = function(id) {
		this.id = id;
	}
	
	AUI.Pushbutton.prototype = new AUI.Device();
	
	AUI.Pushbutton.prototype.setPortValue = function(port,newValue) {
		this.value = newValue;
		if (newValue == true || newValue == "true" || newValue == "on") {
			this.setStatus("on");
		} else if (newValue == false || newValue == "false" ||newValue == "off") {
			this.setStatus("off");
		}		
	}
	
	AUI.Pushbutton.prototype.onTouchStart = function(event) {
		event.preventDefault();
		event.stopPropagation();
		var self = this;
		this.touchEnd = function(e) { return self.onTouchEnd(e) };
		this.element.addEventListener('touchend', this.touchEnd, false);
		this.onStart();
		return false;
	}

	AUI.Pushbutton.prototype.onMouseDown = function(event) {
		event.preventDefault();
		event.stopPropagation();
		var self = this;
		this.mouseUp = function(e) { return self.onMouseUp(e) }
		this.element.addEventListener('mouseup', this.mouseUp, false);					
		this.onStart();
		return false;
	}

	AUI.Pushbutton.prototype.onStart = function() {		
		var control = this.getControl();
		AUI.SetRequest.send(this,"on");
	}

	AUI.Pushbutton.prototype.onTouchEnd = function(event) {
		event.preventDefault();
		event.stopPropagation();
	    this.element.removeEventListener('touchend', this.touchEnd, false);			
		this.onStop();
		return false;
	}

	AUI.Pushbutton.prototype.onMouseUp = function(event) {
		event.preventDefault();
		event.stopPropagation();
		this.element.removeEventListener('mouseup', this.mouseUp, false);
		this.onStop();
		return false;
	}

	AUI.Pushbutton.prototype.onStop = function() {		
		var control = this.getControl();
		if (!AUI.SetRequest.send(this,"off")) {
			var self = this;
			if (this.retryTimer) {
				clearTimeout(this.retryTimer);
			}
			this.retryTimer = setTimeout(function() { return self.onStop() },100);
			AUI.Logger.debug("Will retry Pushbutton.onStop()");
		}
	}

}