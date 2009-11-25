if (AUI.Regt == undefined) {
	
	AUI.Regt = {

		init : function(address) {
			this.step = 0.5; // regulation step
			this.factor = 4; // pixels per step
			this.minT = 15; // minimum value
			this.maxT = 30; // minimum value
			this.request = AUI.Http.getRequest();
			this.jsonrpc = new JSONRpcClient("/aui/rpc");
			this.jsonrpc.AUI.login("utente","demo");
			this.sending = false;
			this.data = null;
			this.id = null;
			this.cursor = null;
			this.value = null;
			this.address = address; 
			this.get();
			// autologin
			AUI.Logger.setLevel(2);
			var self = this;
		},
		
		/**
		 * Get RegT info from server
		 */
		get : function() {
			this.updateSetPoints();
			this.updateTemp();
			this.updateSetPoint();
			this.updateSeason();
			this.updateMode();			
		},

		updateSetPoints : function() {
			this.request.open('GET', 'jais/get?address='+this.address+':*', true);
			var self = this;
			this.request.onreadystatechange = AUI.Regt.stateChange;
			this.timeout = window.setTimeout(self.timeoutExpired, 3000);
			this.request.send(null);
			this.sending = true;
		},
		
		updateTemp : function() {
			var s = "" + this.jsonrpc.AUI.getPortValue(this.address+':temp');
			var i = s.indexOf(".");
			if (i>-1) {
				s = s.substr(0,i) + "," + s.substr(i+1,1);
			} else {
				s += ",0";
			}
			$("temp").innerHTML = s + "°C";
		},

		updateMode : function() {
			var mode = this.jsonrpc.AUI.getPortValue(this.address+':mode');
			if (mode == "manual") {
				var src = $("mode-manual").src;
				src = src.replace("off.","on.");
				$("mode-manual").src = src;
			} else {
				var src = $("mode-manual").src;
				src = src.replace("on.","off.");
				$("mode-manual").src = src;				
			}
			if (mode == "chrono") {
				var src = $("mode-chrono").src;
				src = src.replace("off.","on.");
				$("mode-chrono").src = src;
			} else {
				var src = $("mode-chrono").src;
				src = src.replace("on.","off.");
				$("mode-chrono").src = src;				
			}
			if (mode == "off") {
				var src = $("mode-off").src;
				src = src.replace("off.","on.");
				$("mode-off").src = src;
			} else {
				var src = $("mode-off").src;
				src = src.replace("on.","off.");
				$("mode-off").src = src;				
			}
		},

		updateSeason : function() {
			var season = this.jsonrpc.AUI.getPortValue(this.address+':season');
			var src = $("season-winter").src;
			if (season == "WINTER") {
				src = src.replace("off.","on.");
			} else {
				src = src.replace("on.","off.");
			}
			$("season-winter").src = src;
			src = $("season-summer").src;
			if (season == "SUMMER") {
				src = src.replace("off.","on.");
			} else {
				src = src.replace("on.","off.");
			}
			$("season-summer").src = src;
		},

		updateSetPoint : function() {
			s = "" + this.jsonrpc.AUI.getPortValue(this.address+':setPoint');
			var i = s.indexOf(".");
			if (i>-1) {
				s = s.substr(0,i) + "," + s.substr(i+1,1);
			} else {
				s += ",0";
			}
			$("setPoint").innerHTML = s + "°C";
		},
		
		updatePreset : function() {
			var setPoint = this.jsonrpc.AUI.getPortValue(this.address+':setPoint');
			var stagione = this.jsonrpc.AUI.getPortValue(this.address+':season');
			if (mode == "manual") {
				var src = $("mode-manual").src;
				src = src.replace("off.","on.");
				$("mode-manual").src = src;
			} else {
				var src = $("mode-manual").src;
				src = src.replace("on.","off.");
				$("mode-manual").src = src;				
			}
			if (mode == "chrono") {
				var src = $("mode-chrono").src;
				src = src.replace("off.","on.");
				$("mode-chrono").src = src;
			} else {
				var src = $("mode-chrono").src;
				src = src.replace("on.","off.");
				$("mode-chrono").src = src;				
			}
			if (mode == "off") {
				var src = $("mode-off").src;
				src = src.replace("off.","on.");
				$("mode-off").src = src;
			} else {
				var src = $("mode-off").src;
				src = src.replace("on.","off.");
				$("mode-off").src = src;				
			}
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
						var tmp;
						eval("tmp="+request.responseText+";");
						AUI.Regt.update(tmp);
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
				AUI.Logger.setLevel(0);
			}		
		},
		
		update : function(newData) {
			this.data = newData;
			for (var stagione = 0; stagione <= 1; stagione++) {
				for (var giorno = 0; giorno <= 6; giorno++) {
					for (var ora = 0; ora <= 23; ora++) {
						// setPoint-1-2-3
						var id = stagione + "-" + giorno+"-"+ora;
						var value = this.data[0].Status["setPoint-"+id].V;
						AUI.Logger.debug("id="+id+" value="+value);
						AUI.Regt.updateBar(id,value);
					};
				};
			};
		},
		
		updateBar : function(id,value) {
			var el = document.getElementById("eds-regt-" + id);
			el.style.bottom = ((value - this.minT ) * this.factor) + 'px';
			el.style.display = 'block';
			/*
			FIXME aggiungere zero finale
			if (Math.abs(Math.round(value) - value) < 0.1) {
				value = value + '.0';
			}
			*/
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
		
		onMouseDownDay : function(event,id) {
			AUI.Logger.log("onMouseDownDay id="+id+" x:"+event.clientX+" y:"+event.clientY);
			event.preventDefault();
			event.stopPropagation();
		},

		onMouseDownHour : function(event,id) {
			AUI.Logger.log("onMouseDownHour id="+id+" x:"+event.clientX+" y:"+event.clientY);
			event.preventDefault();
			event.stopPropagation();
		},
		
		cursorValue : function(cursor,y) {
			var hourDiv = cursor.parentNode;
			var hourPosition = AUI.getPosition(hourDiv);
			// coordinata y del bordo inferiore del blocco dell'ora
			var bottom = hourPosition.y + hourDiv.offsetHeight; 
			var value = (bottom - y - window.scrollY - cursor.clientHeight / 2) / this.factor + this.minT;
			value = Math.round(value / this.step) * this.step;
			if (value == null) {
				value = 0;
			};
			if (value < this.minT) {
				value = this.minT;
			}
			if (value > this.maxT) {
				value = this.maxT;
			}
			return value;
		},

		onMouseDownCursor : function(event,id) {
			event.preventDefault();
			event.stopPropagation();
			this.id = id;
			var cursor = document.getElementById("eds-regt-" + id);			
			this.cursor = cursor;
			var value = this.cursorValue(cursor,event.clientY); 
			this.value = value;
			AUI.Logger.log("onMouseDownCursor id="+id+" x:"+event.clientX+" y:"+event.clientY+" value="+value);
			AUI.Regt.updateBar(id,value);
			// registra listener
			var self = this;
			this.mouseUpCursor = function(e) { return self.onMouseUpCursor(e); };
			cursor.addEventListener('mouseup', this.mouseUpCursor, false);					
			this.mouseOutCursor = function(e) { return self.onMouseUpCursor(e); };
			cursor.addEventListener('mouseout', this.mouseOutCursor, false);
			this.mouseMoveCursor = function(e) { return self.onMouseMoveCursor(e); };
			cursor.addEventListener('mousemove', this.mouseMoveCursor, false);					
		},
		
		onMouseMoveCursor : function(event) {
			event.preventDefault();
			event.stopPropagation();
			var id = this.id;
			var cursor = this.cursor;
			var value = this.cursorValue(cursor,event.clientY);
			this.value = value;
			AUI.Logger.log("onMouseMoveCursor id="+id+" x:"+event.clientX+" y:"+event.clientY+" value="+value);
			AUI.Regt.updateBar(id,value);
		},
		
		onMouseUpCursor : function(event) {
			var cursor = this.cursor;
			cursor.removeEventListener('mouseup', this.mouseUpCursor, false);					
			cursor.removeEventListener('mouseout', this.mouseOutCursor, false);
			cursor.removeEventListener('mousemove', this.mouseMoveCursor, false);
			this.onCursorStop();
			this.id = null;
			this.cursor = null;
		},
		
		onCursorStop : function() {
			if (this.value != null) {
				//if (!AUI.SetRequest.sending && AUI.SetRequest.setValue(this.address+":setPoint-"+this.id,this.value)) {
				if (this.jsonrpc.AUI.writePortValue(this.address+":setPoint-"+this.id,this.value)) {
					this.value = null;
				} else {
					var self = this;
					if (this.retryTimer) {
						clearTimeout(this.retryTimer);
					}
					this.retryTimer = setTimeout(function() { return self.onCursorStop(); },50);				
				}
			}
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
		
		activatePreset : function(preset) {
			
		},
		
		setSeason : function(season) {
			this.jsonrpc.AUI.writePortValue(this.address+':season',season);
			var self = this;
			setTimeout(function() { self.updateSeason(); },500);;
			setTimeout(function() { self.updateSetPoint(); },1500);;
		},

		setMode : function(mode) {
			this.jsonrpc.AUI.writePortValue(this.address+':mode',mode);
			var self = this;
			setTimeout(function() { self.updateMode(); },500);;
			setTimeout(function() { self.updateSetPoint(); },500);;
		},

		setPointUp : function() {
			var t = this.jsonrpc.AUI.getPortValue(this.address+':setPoint');
			this.jsonrpc.AUI.writePortValue(this.address+':setPoint',t+0.5);
			var self = this;
			setTimeout(function() { self.updateSetPoint(); },500);;
			setTimeout(function() { self.updateMode(); },500);;
		},
		
		setPointDown : function() {
			var t = this.jsonrpc.AUI.getPortValue(this.address+':setPoint');
			this.jsonrpc.AUI.writePortValue(this.address+':setPoint',t-0.5);
			var self = this;
			setTimeout(function() { self.updateSetPoint(); },500);;
			setTimeout(function() { self.updateMode(); },500);;
		},
		
		clearSeason : function(which) {
			if (!confirm("Clean season set points ?")) return;
			this.jsonrpc.AUI.writePortValue(this.address+':ResetSeason',which);			
			var self = this;
			setTimeout(function() { self.updateSetPoints(); },2000);;
		},
		
		clearDay : function(which) {
			if (!confirm("Clean day set points ?")) return;
			this.jsonrpc.AUI.writePortValue(this.address+':ResetDay',which);			
			var self = this;
			setTimeout(function() { self.updateSetPoints(); },1000);;
			
		}
		
	};
}		
