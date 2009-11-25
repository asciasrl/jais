if (!AUI.Blind) {
	
	AUI.Blind = function(id) {
		this.id = id;
		this.expanded = false;
		this.fadetime = 3000;
	};
	
	AUI.Blind.prototype = new AUI.Device();
	
	AUI.Blind.prototype.onTouchStart = function(event) {
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
		if (this.status == "opening" || this.status == "closing") {
			this.stop();
		}
		if (this.expanded) {
			this.collapse();
		} else {
			this.expand();
		}
	};
	
	AUI.Blind.prototype.expand = function() {
		this.expanded = true;
		var self = this;
		
		this.openButton = document.getElementById(this.id+'-open');
		this.touchStartOpen = function(e) { return self.onTouchStartOpen(e); };
		//this.openButton.addEventListener('touchstart', this.touchStartOpen, false);
		this.mouseDownOpen = function(e) { return self.onMouseDownOpen(e); };
		this.openButton.addEventListener('mousedown', this.mouseDownOpen, false);
		this.openButton.style.display = 'block';

		this.closeButton = document.getElementById(this.id+'-close');
		this.touchStartClose = function(e) { return self.onTouchStartClose(e); };
		//this.closeButton.addEventListener('touchstart', this.touchStartClose, false);
		this.mouseDownClose = function(e) { return self.onMouseDownClose(e); };
		this.closeButton.addEventListener('mousedown', this.mouseDownClose, false);
		this.closeButton.style.display = 'block';
		
		this.timeout = setTimeout(function() { return self.collapse(); },this.fadetime);		
	};
	
	AUI.Blind.prototype.collapse = function() {
		clearInterval(this.timeout);
		this.expanded = false;
		
		//this.openButton.removeEventListener('touchstart', this.touchStartOpen, false);
		this.openButton.removeEventListener('mousedown', this.mouseDownOpen, false);
		this.openButton.style.display = 'none';
		
		//this.closeButton.removeEventListener('touchstart', this.touchStartClose, false);
		this.closeButton.removeEventListener('mousedown', this.mouseDownClose, false);
		this.closeButton.style.display = 'none';
		//
	};

	AUI.Blind.prototype.onMouseDownClose = function() {
		this.close();
	};

	AUI.Blind.prototype.onMouseDownOpen = function() {
		this.open();
	};
	
	AUI.Blind.prototype.open = function() {
		var control = this.getControl();		
		AUI.SetRequest.set(this,"open","opening");
		this.collapse();
	};
	
	
	
	AUI.Blind.prototype.close = function() {
		var control = this.getControl();
		AUI.SetRequest.set(this,"close","closing");
		this.collapse();		
	};
	
	AUI.Blind.prototype.stop = function() {
		var control = this.getControl();
		AUI.SetRequest.set(this,"stop","stopped");
	};

		
}