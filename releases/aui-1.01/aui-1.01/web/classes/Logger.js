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
	
	AUI.Logger.debug = function(e) {
		if (this.level <= 0) {
			try {
				console.log(this.getTS() + e);
			} catch(e) {}
		}
	}
	
	AUI.Logger.log = AUI.Logger.debug;

	AUI.Logger.info = function(e) {
		if (this.level <= 1) {
			try {
				console.info(this.getTS() + e);
			} catch(e) {}
		}
	}

	AUI.Logger.error = function(e) {
		if (this.level <= 2) {
			try {
				console.error(this.getTS() + e);
			} catch(e) {}
		}
	}
	
	AUI.Logger.getTS = function() {
		var d = new Date();
		return d.valueOf() / 1000.0 + " ";

	}
	
}
