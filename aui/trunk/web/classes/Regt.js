if (AUI.Regt == undefined) {
	
	AUI.Regt = {

		init : function() {
			this.factor = 2;
			this.request = AUI.Http.getRequest();
			AUI.Regt.get('1.255');
			AUI.Logger.setLevel(2);
			var self = this;
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
						//AUI.Logger.debug("Response:"+request.responseText);
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
						// setPoint-1-2-3
						var id = stagione + "-" + giorno+"-"+ora;
						var value = data[0].Status["setPoint-"+id].V;
						AUI.Logger.debug("id="+id+" value="+value);
						AUI.Regt.updateBar(id,value);
					};
				};
			};
		},
		
		updateBar : function(id,value) {
			var el = document.getElementById("eds-regt-" + id);
			if (value == null) {
				value = 0;
			};
			el.style.bottom = (value * this.factor) + 'px';
			el.innerHTML = value;
		},
		
		onMouseOver : function(event,id) {
			AUI.Logger.log("onMouseOver id="+id+" x:"+event.clientX+" y:"+event.clientY);
			//event.preventDefault();
			//event.stopPropagation();
			var el = document.getElementById("eds-regt-" + id);
			// TODO calcolare offset
			//alert(el.offsetTop);
			//var value = event.clientY - el.top;
			this.el = el;
			var parent = el.parentNode;
			this.parent = parent;
			var position = AUI.Regt.getPosition(parent);
			this.y = position.y + parent.offsetHeight; 
			//AUI.Regt.updateBar(id,value);
			var self = this;
			self.id = id;
			this.mousemove = function(e) { return self.onMouseMove(e,id); };
			el.addEventListener('mousemove', this.mousemove , false);
			AUI.Logger.log("onMouseOver y:"+this.y);
		},
		
		onMouseMove : function(event,id) {
			var el = document.getElementById("eds-regt-" + this.id);
			var value = (this.y - event.clientY - window.scrollY) / this.factor;
			AUI.Logger.log("onMouseMove id="+id+" x:"+event.clientX+" y:"+event.clientY+" value="+value);
			//var value = el.offsetTop + 180 - event.clientY;
			AUI.Regt.updateBar(id,value);
			return false;
		},
		
		onMouseOut : function(event,id) {
			
		},
		
		abort : function() {
			this.request.abort();
			this.sending = false;
		},
		
		timeoutExpired : function() {
			AUI.Regt.abort();
		},
		
		getPosition : function(e){
			var left = 0;
			var top  = 0;

			while (e.offsetParent){
				left += e.offsetLeft;
				top  += e.offsetTop;
				e     = e.offsetParent;
			}

			left += e.offsetLeft;
			top  += e.offsetTop;
			
			return {x:left, y:top};
		}


	};
}		
