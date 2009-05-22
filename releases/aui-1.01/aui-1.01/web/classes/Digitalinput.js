if (!AUI.Digitalinput) {
	
	AUI.Digitalinput = function(id) {
		this.id = id;
	};
	
	AUI.Digitalinput.prototype = new AUI.Device();
	
}