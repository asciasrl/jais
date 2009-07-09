if (!AUI.Dimmer) {
	
	/**
	 * Funzionamento:
	 * Mouse:
	 * 1 - mouseDown sul controllo
	 * 2 - onTimer: attivazione slider e mask
	 * 2.1 - mouseDown sul cursore
	 * 2.2 - mouseMove sposta il cursore
	 * 2.3 - mouseUp trasmette dato
	 * 2.4 - mouseDown su mask: chiude slider
	 * 3 - mouseUp prima di onTimer: switch della luce 
	 */
	
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
			this.cursor = document.getElementById('slider-cursor');
			var control = this.getControl();
			this.slider.style.left = (control.left - 40)+ "px";
			this.slider.style.top = (control.top - 50)+ "px";
			this.cursor = document.getElementById("slider-cursor");
			this.cursor.style.bottom = Number(this.value) + "px";
			this.mask = document.getElementById("mask");			
			var self = this;
			if (this.eventType == "mouse") {
			    this.element.removeEventListener('mouseup', this.mouseUp, false);
				this.mask.style.display = 'block';
				this.mask.style.width = window.innerWidth + "px";
				this.mask.style.height = window.innerHeight + "px";
				this.mask.style.left = window.scrollX + "px";
				this.mask.style.top = window.scrollY + "px";
				this.slider.style.display = 'block';
				this.sliderMouseDown = function(e) { return self.onSliderMouseDown(e) };
				this.slider.addEventListener('mousedown', this.sliderMouseDown , false);
				this.maskMouseDown = function(e) { return self.onMaskMouseDown(e) };
				this.mask.addEventListener('mousedown', this.maskMouseDown, false);
			} else {
			    this.element.removeEventListener('touchend', this.touchEnd, false);
				this.mask.style.display = 'block';
				this.slider.style.display = 'block';		
				this.sliderTouchStart = function(e) { return self.onSliderTouchStart(e) };
				this.slider.addEventListener('touchstart', this.sliderTouchStart , false);
				this.maskTouchStart = function(e) { return self.onMaskTouchStart(e) };
				this.mask.addEventListener('touchstart', this.maskTouchStart, false);
			}
			return;
		}
	}

	/**
	 * Gestione del primo tocco o click sul controllo
	 */
	AUI.Dimmer.prototype.onStart = function() {
		AUI.Logger.log("start "+this.id);	
		var self = this;
		var control = AUI.Controls.getControl(this.id);
		this.mode = "switching";
		this.timeout = setTimeout(function() { return self.onTimer() },control.timer);		
	}
	

	/**
	 * Fine del click/touch sul controllo
	 */
	AUI.Dimmer.prototype.onStop = function() {
		clearInterval(this.timeout);
		AUI.Logger.log("stop "+this.id);	
		var self = this;
		if (this.slider) {
			this.slider.style.display = 'none'; 
		}
		if (this.mask) {
			this.mask.style.display = 'none'; 
		}
		if (this.mode == "switching") {
			var status = this.status;			
			var newstatus = status;
			if (status == "on") {
				newstatus = "off";
			} else {
				newstatus = "on";
			}
			if (AUI.SetRequest.send((this.getControl()).address,newstatus)) {
				this.setStatus(newstatus);
			}
		} else if (this.mode == "sliding") {
			this.onSliderStop();
		}
	}
		
	AUI.Dimmer.prototype.onMouseDown = function(event) {
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
		this.eventType = "mouse";
		var self = this;
		this.mouseUp = function(e) { return self.onMouseUp(e) }
		this.element.addEventListener('mouseup', this.mouseUp, false);					
		this.onStart();
		return false;
	}

	AUI.Dimmer.prototype.onMouseUp = function(event) {
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
		this.element.removeEventListener('mouseup', this.mouseUp, false);
		this.onStop();
		return false;
	}

	AUI.Dimmer.prototype.onTouchStart = function(event) {
		event.preventDefault();
		event.stopPropagation();
		this.eventType = "touch";
		var self = this;
		this.touchEnd = function(e) { return self.onTouchEnd(e) };
		this.element.addEventListener('touchend', this.touchEnd, false);
		this.onStart();
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
		} else {
		    this.element.removeEventListener('touchend', this.touchEnd, false);			
		}
		this.onStop();
		return false;
	}

	AUI.Dimmer.prototype.getSliderValue = function(y) {
		// 11 e 15 dipendono dalle immagini
		AUI.Logger.log("scrollY="+window.scrollY);
		if (this.eventType == "mouse") {
			var y1 = 100 - (y + window.scrollY - this.slider.offsetTop - 11 - 15);
		} else {
			var y1 = 100 - (y - this.slider.offsetTop - 11 - 15);
		}
		var newValue = Math.min(100,Math.max(0,y1));
		return newValue;				
	}
	
	/**
	 * Gestione movimento del mouse o del tocco sul cursore
	 */
	AUI.Dimmer.prototype.onSliderStart = function(x,y) {
		var newValue = this.getSliderValue(y);
		this.cursor.style.bottom = newValue + 'px';
		this.initialSliderValue = this.value;
		this.sliderValue = newValue;
		AUI.Logger.log("slider start: y="+y+" value="+this.sliderValue);
	}
	
	AUI.Dimmer.prototype.onSliderMove = function(x,y) {
		var newValue = this.getSliderValue(y);
		this.cursor.style.bottom = newValue + 'px';
		this.sliderValue = newValue;
		AUI.Logger.log("slider move: y="+y+" value="+newValue);
	}

	AUI.Dimmer.prototype.onSliderStop = function() {
		AUI.Logger.log("slider stop: value="+this.sliderValue);
		if (this.sliderValue != null) {
			if (!AUI.SetRequest.sending && AUI.SetRequest.send((this.getControl()).address,this.sliderValue)) {
				this.value = this.sliderValue; 
				this.sliderValue = null;	
			} else {
				var self = this;
				if (this.retryTimer) {
					clearTimeout(this.retryTimer);
				}
				this.retryTimer = setTimeout(function() { return self.onSliderStop() },50);				
			}
		}
	}


	AUI.Dimmer.prototype.onSliderMouseDown = function(event) {
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
		var self = this;
		this.sliderMouseMove = function(e) { return self.onSliderMouseMove(e) };
		this.slider.addEventListener('mousemove', this.sliderMouseMove, false);
		this.sliderMouseUp = function(e) { return self.onSliderMouseUp(e) };
		this.slider.addEventListener('mouseup', this.sliderMouseUp, false);
		this.sliderMouseOut = function(e) { return self.onSliderMouseUp(e) };
		this.slider.addEventListener('mouseout', this.sliderMouseOut, false);
		this.onSliderStart(event.clientX, event.clientY);
		return false;
	}

	AUI.Dimmer.prototype.onSliderMouseMove = function(event) {
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
		var x = event.clientX;
		var y = event.clientY;
	    this.onSliderMove(x,y);
		return false;
	}

	AUI.Dimmer.prototype.onSliderMouseUp = function(event) {
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
		//this.mask.removeEventListener('mousemove', this.sliderMouseMove, false);
		this.slider.removeEventListener('mousemove', this.sliderMouseMove, false);
		this.slider.removeEventListener('mouseup', this.sliderMouseUp, false);
		this.slider.removeEventListener('mouseout', this.sliderMouseOut, false);
		this.onSliderStop();
	}

	AUI.Dimmer.prototype.onMaskMouseDown = function(event) {
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
		this.mask.removeEventListener('mousedown', this.maskMouseDown, false);
		this.slider.removeEventListener('mousedown', this.sliderMouseDown, false);
		this.slider.removeEventListener('mousemove', this.sliderMouseMove, false);
		this.slider.removeEventListener('mouseup', this.sliderMouseUp, false);
		this.slider.removeEventListener('mouseout', this.sliderMouseOut, false);
		this.onStop();
	}


	AUI.Dimmer.prototype.onSliderTouchStart = function(event) {
		event.preventDefault();
		event.stopPropagation();
		if (event.targetTouches.length > 1) return;
		var self = this;
		this.sliderTouchMove = function(e) { return self.onSliderTouchMove(e) };
		this.slider.addEventListener('touchmove', this.sliderTouchMove, false);
		this.sliderTouchEnd = function(e) { return self.onSliderTouchEnd(e) };
		this.slider.addEventListener('touchend', this.sliderTouchEnd, false);
		var x = event.targetTouches[0].clientX;
		var y = event.targetTouches[0].clientY;
		this.onSliderStart(x,y);
		return false;
	}

	AUI.Dimmer.prototype.onSliderTouchMove = function(event) {
		event.preventDefault();
		event.stopPropagation();
		if (event.targetTouches.length > 1) return;
		var x = event.targetTouches[0].clientX;
		var y = event.targetTouches[0].clientY;
		this.onSliderMove(x,y);
		return false;
	}

	AUI.Dimmer.prototype.onSliderTouchEnd = function(event) {
		event.preventDefault();
		event.stopPropagation();
		// TODO
		this.onSliderStop();
	}

	AUI.Dimmer.prototype.onMaskTouchStart = function(event) {
		event.preventDefault();
		event.stopPropagation();
		this.slider.removeEventListener('touchstart', this.sliderTouchStart, false);
		this.slider.removeEventListener('touchmove', this.sliderTouchMove, false);
		this.slider.removeEventListener('touchend', this.sliderTouchEnd, false);
		this.onStop();
	}

}