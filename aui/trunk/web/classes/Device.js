if (!AUI.Device) {
		
	AUI.Device = function(id) {
		this.id = id;
		this.oldStatus = null;
		this.status = null;
		this.element = null;
	};
	
	AUI.Device.prototype.getControl = function() {
		return AUI.Controls.getControl(this.id);
	};
	
	AUI.Device.prototype.getElement = function() {
		if (this.element == null) {
			this.element = document.getElementById(this.id); 
		}
		return this.element;
	};
	
	AUI.Device.prototype.getLabel = function() {
		var label = this.label;
		if (label == null) {
			label = document.getElementById(this.id+"-label");
			if (label == null) {
				AUI.Logger.error("Elemento label non trovato per "+this.id);
			} else {
				this.label = label;
			}
		}
		return label;
	};

	AUI.Device.prototype.getImg = function() {
		var img = this.img;
		if (img == null) {
			img = document.getElementById(this.id+"-img");
			if (img == null) {
				AUI.Logger.error("Elemento label non trovato per "+this.id);
			} else {
				this.img = img;
			}
		}
		return img;
	};
	
	AUI.Device.prototype.setStatus = function(newStatus) {
		if (newStatus != null && (this.status == null || newStatus != this.status)) {
			this.oldStatus = this.status;
			this.status = newStatus;
			var control = this.getControl();
			var newImg = control[newStatus];
			if (newImg) {
				AUI.Logger.info("Stato "+this.id+" :"+newStatus);
				(this.getImg()).src = skin + newImg;
			} else {
				AUI.Logger.error("Stato non valido per "+this.id+" :"+newStatus);
			}
		}
	};
	
	AUI.Device.prototype.setPortValue = function(port,newValue) {
		this.value = newValue;
		this.setStatus(newValue);
	};
	
	AUI.Device.prototype.setLabelValue = function(text) {
		var label = this.getLabel();
	};
	
	AUI.Device.prototype.onTouchStart = function(event) {
		return;
	};

	AUI.Device.prototype.onMouseDown = function(event) {
		this.onTouchStart(event);
	};
	
	
}