if (!AUI.Power) {
	
	AUI.Power = function(id) {
		this.id = id;
	}
	
	AUI.Power.prototype = new AUI.Light();
	
}