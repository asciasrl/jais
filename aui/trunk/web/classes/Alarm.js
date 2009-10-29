if (!AUI.Alarm) {

	AUI.Alarm = function(id) {
		this.id = id;
		this.pin = "";
		this.rpc = null;
	}
	
	AUI.Alarm.prototype = new AUI.Light();

	AUI.Alarm.prototype.onTouchStart = function(event) {
		event.stopPropagation();
		event.preventDefault();
		//event.stop();
		AUI.Keypad.show();
	}
}
