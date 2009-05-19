if (!AUI.Light) {
	
	AUI.Input = function(id) {
		this.id = id;
	};
	
	AUI.Input.prototype = new AUI.Device();
	
}