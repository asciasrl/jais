if (!AUI.Logger) {
	
	AUI.Logger = {
			level: 2
	};
	
	AUI.Logger.setLevel = function(s) {
		switch(s) {
		case 'debug': 
		case 0: 
			this.level = 0;
			break;
		case 'info':
		case 1: 
			this.level = 1;
			break;
		case 'error':
		case 2: 
			this.level = 2;
			break;		
		}
	}
	
	AUI.Logger.log = function(e) {
		if (console && this.level <= 0) {
			console.log(e);
		}
	}
	
	AUI.Logger.debug = AUI.Logger.log;  

	AUI.Logger.info = function(e) {
		if (console  && this.level <= 1) {
			console.info(e);
		}
	}

	AUI.Logger.error = function(e) {
		if (console  && this.level <= 2) {
			console.error(e);
		}
	}

}
