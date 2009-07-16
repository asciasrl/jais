if (!AUI.Group) {
	
	AUI.Group = function(id) {
		this.id = id;
	}
	
	AUI.Group.prototype = new AUI.Scene();

	AUI.Scene.prototype.onTouchStart = function(event) {
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
		var control = this.getControl();
		var address = control.address;
		var value = control.value;
		AUI.SetRequest.send(address,value);
	}

}