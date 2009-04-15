if (!AUI.Dimmer) {
	
	AUI.Dimmer = function(id) {
		this.id = id;
		this.element = this.getElement();
	}
	
	AUI.Dimmer.prototype = new AUI.Light();
	
	AUI.Dimmer.prototype.setPortValue = function(port,newValue) {
		this.value = newValue;
		if (newValue > 0) {
			this.setStatus("on");
		} else {
			this.setStatus("off");
		}
		(this.getLabel()).innerHTML = newValue + "%";
	}
	
	AUI.Dimmer.prototype.onTimer = function() {
		if (this.mode == "sliding") {
			return;
		} else if (this.mode == "switching") {
			this.mode = "sliding";
			AUI.Logger.info("switching -> sliding");

			// calcola la posizione del cursore
			this.slider = document.getElementById('slider');
			this.slider.style.left = (this.startX - 20 - 60) + "px";
			this.slider.style.top = (this.startY - 11 - 15 - (100 - this.value)) + "px";
			this.cursor = document.getElementById("slider-cursor");
			this.cursor.style.bottom = Number(this.value) + "px";
			this.mask = document.getElementById("mask");			
			if (this.eventType == "mouse") {
				//this.element.removeEventListener('mousemove', this.mouseMove, false);
			    this.element.removeEventListener('mouseup', this.mouseUp, false);
			    //this.element.removeEventListener('mouseout', this.mouseOut, false);
				this.mask.style.display = 'block';
				this.mask.style.width = window.innerWidth + "px";
				this.mask.style.height = window.innerHeight + "px";
				this.mask.style.left = window.scrollX + "px";
				this.mask.style.top = window.scrollY + "px";
				this.slider.style.display = 'block';		
				this.slider.addEventListener('mousemove', this.mouseMove , false);
				//this.slider.addEventListener('mouseup', this.mouseUp, false);
				this.mask.addEventListener('mousedown', this.mouseUp, false);
			} else {
			    this.element.removeEventListener('touchend', this.touchEnd, false);
				this.mask.style.display = 'block';
				this.slider.style.display = 'block';		
				this.slider.addEventListener('touchmove', this.touchMove , false);
				this.mask.addEventListener('touchstart', this.touchEnd, false);
			}
			//this.onMove(0,0);
			return;
		}
		var control = this.getControl();
		var value = this.value;
		if (value == null) {
			value = 0;
		}
		var step = this.step; 
		if (step == null) {
			step = control.step;
			if (step == null) {
				step = 10;
			}
		}
		value = 1.0*step + 1.0*value;
		if (value == "NaN") {
			AUI.Logger.error(this.id + " value="+value+" step="+step);
		}
		if (value > control.max) {
			value = control.max;
			this.step = -1.0*step;
		}
		if (value < control.min) {
			value = 0;
			this.step = -1.0*step;
		}
		AUI.SetRequest.send(control.address,value);
		var self = this;
		this.timeout = setTimeout(function() { return self.onTimer() },control.timer);		
	}

	AUI.Dimmer.prototype.onStop = function() {
		clearInterval(this.timeout);
		var slider = document.getElementById('slider');
		slider.style.display = 'none';
		AUI.Logger.log("stop "+this.id);	
		var self = this;
		if (this.mode == "switching") {
			this.switching = null;
			var status = this.status;			
			var newstatus = status;
			if (status == "on") {
				newstatus = "off";
			} else {
				newstatus = "on";
			}
			if (newstatus != status) {
				this.setStatus(newstatus);
			}			
			AUI.SetRequest.send((this.getControl()).address,this.status);
		} else if (this.mode == "cycling") {
			this.step = -1.0*this.step;
			this.cycling = false;
		}
	}
	
	AUI.Dimmer.prototype.onMove = function(x,y) {
		this.mode = "sliding";
		var newValue = Math.min(100,Math.max(0,this.initialValue - y));		
		if (Math.abs(newValue - this.value) > 1) {
			if (!AUI.SetRequest.sending && AUI.SetRequest.send((this.getControl()).address,newValue)) {
				this.cursor.style.bottom = newValue + 'px';
				this.value = newValue;				
			} else {
				var self = this;
				if (this.retryTimer) {
					clearTimeout(this.retryTimer);
				}
				this.retryTimer = setTimeout(function() { return self.onMove(x,y) },50);
			}
		}
	}

	AUI.Dimmer.prototype.onStart = function() {
		AUI.Logger.log("start "+this.id);	
		this.initialValue = this.value;
		var self = this;
		this.mode = "switching";
		this.timeout = setTimeout(function() { return self.onTimer() },1000);		
	}
	
	AUI.Dimmer.prototype.onTouchStart = function(event) {
		event.preventDefault();
		event.stopPropagation();
		this.eventType = "touch";
		this.startX = event.targetTouches[0].clientX;
		this.startY = event.targetTouches[0].clientY;
		var self = this;
		this.touchMove = function(e) { return self.onTouchMove(e) };
		//this.element.addEventListener('touchmove', this.touchMove, false);
		this.touchEnd = function(e) { return self.onTouchEnd(e) };
		this.element.addEventListener('touchend', this.touchEnd, false);
		this.onStart();
		return false;
	}

	AUI.Dimmer.prototype.onMouseDown = function(event) {
		event.preventDefault();
		event.stopPropagation();
		this.eventType = "mouse";
		this.startX = event.clientX;
		this.startY = event.clientY;
		var self = this;
		this.mouseMove = function(e) { return self.onMouseMove(e) };
		//this.element.addEventListener('mousemove', this.mouseMove , false);
		this.mouseUp = function(e) { return self.onMouseUp(e) }
		this.element.addEventListener('mouseup', this.mouseUp, false);					
		//this.mouseOut = function(e) { return self.onMouseUp(e) }
		//this.element.addEventListener('mouseout', this.mouseOut, false);					
		this.onStart();
		return false;
	}

	AUI.Dimmer.prototype.onTouchMove = function(event) {
		event.preventDefault();
		event.stopPropagation();
		if (event.targetTouches.length > 1) return;
		var x = event.targetTouches[0].clientX - this.startX;
		var y = event.targetTouches[0].clientY - this.startY;
		//if (this.mode != "sliding") {
			//this.mode = "sliding";
		    //this.element.removeEventListener('touchmove', this.touchMove , false);
		    //this.element.removeEventListener('touchend', this.touchEnd, false);
			//this.slider.style.display = 'block';		
			//this.slider.addEventListener('touchmove', this.touchMove, false);
			//this.slider.addEventListener('touchend', this.touchEnd, false);
		//}		
		this.onMove(x,y);
		return false;
	}
	
	AUI.Dimmer.prototype.onMouseMove = function(event) {
		event.preventDefault();
		event.stopPropagation();
		var x = event.clientX - this.startX;
		var y = event.clientY - this.startY;
	    
		//this.element.removeEventListener('mousemove', this.mouseMove, false);
	    //this.element.removeEventListener('mouseup', this.mouseUp, false);
	    //this.element.removeEventListener('mouseout', this.mouseOut, false);
		//this.slider.style.display = 'block';		
		//this.slider.addEventListener('mousemove', this.mouseMove , false);
		//this.slider.addEventListener('mouseup', this.mouseUp, false);					
		//this.slider.addEventListener('mouseout', this.mouseOut, false);					

	    this.onMove(x,y);
	    // TODO
		return false;
	}

	AUI.Dimmer.prototype.onTouchEnd = function(event) {
		event.preventDefault();
		event.stopPropagation();
		var self = this;
		if (this.mode == "sliding") {
		    this.mask.removeEventListener('touchstart', this.touchEnd, false);
		    this.slider.removeEventListener('touchmove', this.touchMove , false);
			this.mask.style.display = 'none';
		    //this.slider.removeEventListener('touchend', this.touchEnd, false);			
		} else {
		    //this.element.removeEventListener('touchmove', this.touchMove , false);
		    this.element.removeEventListener('touchend', this.touchEnd, false);			
		}
		this.onStop();
		return false;
	}

	AUI.Dimmer.prototype.onMouseUp = function(event) {
		event.preventDefault();
		event.stopPropagation();
		if (this.mode == "sliding") {
		    //this.slider.removeEventListener('mousemove', this.mouseMove, false);
		    this.mask.removeEventListener('mousedown', this.mouseUp, false);
			this.mask.style.display = 'none';
		    //this.slider.removeEventListener('mouseout', this.mouseOut, false);
		} else {
		    //this.element.removeEventListener('mousemove', this.mouseMove, false);
		    this.element.removeEventListener('mouseup', this.mouseUp, false);
		    //this.element.removeEventListener('mouseout', this.mouseOut, false);
		}
		if (this.mask) {
		    //this.mask.removeEventListener('mouseup', this.maskEvent, false);
		}
		this.onStop();
		return false;
	}


}