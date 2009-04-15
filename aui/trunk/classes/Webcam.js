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
			newstatus = "pause";
			(this.getImg()).src = skin + (this.getControl()).pause;
		} else {
			newstatus = "play";
			(this.getImg()).src = (this.getControl()).video;
		}		
	}
	
}