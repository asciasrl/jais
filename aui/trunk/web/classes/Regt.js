if (AUI.Regt == undefined) {
	
	AUI.Regt = {

		init : function() {
			this.request = AUI.Http.getRequest();
			AUI.Regt.get('str.70');
		},
		
		stateChange : function() {
			// todo
		},
		
		/**
		 * Get RegT info from server
		 */
		get : function(address) {
			this.request.open('GET', 'jais/get?address='+address+':*', true);
			var self = this;
			this.request.onreadystatechange = AUI.Regt.stateChange;
			this.timeout = window.setTimeout(self.timeoutExpired, 3000);
			this.request.send(null);
		},
		
		stateChange : function() {
			var request = AUI.Regt.request; 
			if (request.readyState == 4) {
				AUI.Logger.debug("status="+request.status);
				clearInterval(AUI.Regt.timeout);
				if (request.status == 200) {
					if (request.responseText.indexOf("ERROR") == 0) {
						AUI.Header.show(request.responseText);
					} else {
						AUI.Logger.info("request.status:"+request.status);
						var data;
						eval("data="+request.responseText+";");
						AUI.Regt.update(data);
					}
				} else {
					if (request.status == 500) {
						AUI.Header.show("Errore del server.");
					} else if (request.status == 400) {
						AUI.Header.show("Errore di comunicazione.");
					} else {
						AUI.Header.show("Errore di collegamento ("+request.status+")");
					}
				}
				AUI.Regt.sending = false;
			}		
		},
		
		update : function(data) {
			for (var stagione = 0; stagione <= 1; stagione++) {
				for (var giorno = 0; giorno <= 6; giorno++) {
					for (var ora = 0; ora <= 23; ora++) {
						// setPoint-Inverno-Dom-02
						var id = stagione + "-" + giorno+"-"+ora;
						var value = data[0].Status["setPoint-"+id].V;
						AUI.Regt.updateBar(id,value);
					};
				};
			};
		},
		
		updateBar : function(id,value) {
			var el = document.getElementById("eds-regt-" + id + "-bar");
			if (value == null) {
				value = 0;
			};
			el.style.height = (value + 15) + 'px';
			//el.innerHTML = value;
		},
		
		onMouseOver : function(event,id) {
				//event.preventDefault();
				//event.stopPropagation();
				AUI.Logger.log("x:"+event.clientX+" y:"+event.clientY);
				var el = document.getElementById("regt-" + id);
				// TODO calcolare offset
				//alert(el.offsetTop);
				//var value = event.clientY - el.top;
				var value = el.offsetTop + 180 - event.clientY;
				AUI.Regt.updateBar(id,value);
				var self = this;
				self.id = id;
				this.mousemove = function(e) { return self.onMouseMove(e) };
				el.addEventListener('mousemove', this.mousemove , false);
		},
		
		onMouseMove : function(event,id) {
			var el = document.getElementById("regt-" + this.id);
			var value = el.offsetTop + 180 - event.clientY;
			AUI.Regt.updateBar(id,value);
		},
		
		onMouseOut : function(event,id) {
			
		},
		
		abort : function() {
			this.request.abort();
			this.sending = false;
		},
		
		timeoutExpired : function() {
			AUI.Regt.abort();
		}

	};
}		
