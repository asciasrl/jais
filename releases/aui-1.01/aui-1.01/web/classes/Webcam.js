if (!AUI.Webcam) {
	
	AUI.Webcam = function(id) {
		this.id = id;
	}
	
	AUI.Webcam.prototype = new AUI.Device();
	
	AUI.Webcam.prototype.onTouchStart = function(event) {
		event.preventDefault();
		event.stopPropagation();
		var status = this.status;
		var newstatus = status;		
		if (status == "play") {
			this.setStatus("pause");
		} else {
			this.setStatus("play");
			var self = this;
			this.showVideoFunction = function() { return self.showVideo() };
			setTimeout(self.showVideoFunction,1000);
			
		}		
	}
	
	AUI.Webcam.prototype.showVideo = function() {
		(this.getImg()).src = (this.getControl()).video;		
	}
	
	
}