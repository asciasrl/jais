if (!AUI.Webcam) {
	
	AUI.Webcam = function(id) {
		this.id = id;
	};
	
	AUI.Webcam.prototype = new AUI.Device();
	
	AUI.Webcam.prototype.onTouchStart = function(event) {
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
		if (status == "play") {
			this.setStatus("pause");
		} else {
			this.setStatus("play");
			var self = this;
			this.showVideoFunction = function() { return self.showVideo(); };
			setTimeout(self.showVideoFunction,1000);
			
		}		
	};
	
	AUI.Webcam.prototype.showVideo = function() {
		(this.getImg()).src = (this.getControl()).video;		
	};
	
	
}