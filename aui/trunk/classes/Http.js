if (!AUI.Http) {
	
	AUI.Http = {};

  	AUI.Http.getRequest = function() {
  		try {
  			return new XMLHttpRequest();
  		} catch (e) {
  			try {
  				return new ActiveXObject("Msxml2.XMLHTTP");
  			} catch (e) {
    			try {
    				return new ActiveXObject("Microsoft.XMLHTTP");
    			} catch (e) {
					try {
						return window.createRequest();
					} catch (e) {
						window.alert("Browser non compatibile.");
					}
    			}
  			}
  		}
  		return null;
  	}
}
