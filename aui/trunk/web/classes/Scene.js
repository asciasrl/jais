if (!AUI.Scene) {
	
	AUI.Scene = function(id) {
		this.id = id;
	}
	
	AUI.Scene.prototype = new AUI.Device();
	
	AUI.Scene.prototype.setPortValue = function(port,newValue) {
		this.value = newValue;
	}
	
	AUI.Scene.prototype.onTouchStart = function(event) {
		event.preventDefault();
		event.stopPropagation();
		var control = this.getControl();
		AUI.SetRequest.send(control.address,"true");
	}
	
}