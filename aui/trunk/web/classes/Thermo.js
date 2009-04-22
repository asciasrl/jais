if (!AUI.Thermo) {
	
	AUI.Thermo = function(id) {
		this.id = id;
	}
	
	AUI.Thermo.prototype = new AUI.Device();
	
	AUI.Thermo.prototype.setPortValue = function(port,newValue) {
		if (port == "temp") {
			var s = String(newValue);
			var i = s.indexOf(".");
			if (i>-1) {
				s = s.substr(0,i) + "," + s.substr(i+1,1);
			} else {
				s += ",0";
			}
			(this.getLabel()).innerHTML = s + "°C";
		}
	}
}