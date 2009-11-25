if (!AUI.Scene) {
	
	AUI.Scene = function(id) {
		this.id = id;
	};
	
	AUI.Scene.prototype = new AUI.Device();
	
	AUI.Scene.prototype.setPortValue = function(port,newValue) {
		this.value = newValue;
	};
	
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
		AUI.SetRequest.set(this,"true","default");
	};
	
}