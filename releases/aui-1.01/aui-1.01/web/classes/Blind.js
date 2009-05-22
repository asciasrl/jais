if (!AUI.Blind) {
	
	AUI.Blind = function(id) {
		this.id = id;
	}
	
	AUI.Blind.prototype = new AUI.Device();
	
	AUI.Blind.prototype.onTouchStart = function(event) {
		event.preventDefault();
		event.stopPropagation();
		var status = this.status;
		var command = null;
		var newstatus = status;
		switch (status) {
		case "stopped":
			command = "open";
			newstatus = "opening";
			break;
		case "opening":
			command = "stop";
			newstatus = "opened";
			break;
		case "opened":
			command = "close";
			newstatus = "closing";
			break;
		case "closed":
			command = "open";
			newstatus = "opening";
			break;
		case "closing":
			command = "stop";
			newstatus = "closed";
			break;
		default:
			command = "open";
			newstatus = "opening";
		}
		if (newstatus != status) {
			this.setStatus(newstatus);
		}
		if (command) {
			var control = this.getControl();
			AUI.SetRequest.send(control.address,command);
		}		
	}

	
}