if (!AUI.Layers) {
	
	AUI.Layers = {		
		layers: null,
		minSpeed: 80,
		maxSpeed: 320,
		position: 0,
		speed: 0, // pixels / secondo
		updateInterval: 40, // intervallo di aggiornamento, mS
		target: null, // icona verso la quale si sta muovendo
		
		init : function() {
			var i = 0;
			do {
				var el = document.getElementById('layer-'+i);
				if (el == null) {
					break;
				}
				this.layers[i].size = 80;
				this.layers[i].element = el;				
				this.layers[i].img = document.getElementById('layer-'+i+'-img'); 
				i++;
			} while (true);
			this.scroller = document.getElementById('scroller'); 
			this.update();
			 
			//this.onTimer();
		},
		
		onMouseDown : function(layer,event) {
			event.preventDefault();
			event.stopPropagation();
			// TODO
			this.target = layer;
			AUI.Logger.info("target="+layer+" position="+this.position);
			this.startTimer();
		},
		
		onTouchStart : function(layer,event) {
			event.preventDefault();
			event.stopPropagation();
			// TODO
			this.target = layer;
			AUI.Logger.info("target="+layer+" position="+this.position);
			this.startTimer();
		},

		update : function() {
			this.position = this.bound(this.position);
			// TODO 
			var p = this.position;
			// determino per ogni icona la grandezza
			for (var i=0; i < this.layers.length; i++) {
				var size, opacity, height;
				var d = Math.abs(p - i);
				if (d >= 3) {
					size = 0;
					opacity = 0;
				} else if (d > 0.5) {
					var sized = 80 - 20 * (d - 0.5);
					size = Math.round(sized);
					opacity = Math.round(100 - 40 * (d - 0.5));
				} else {
					size = 80;
					opacity = 100;
				}
				var layer = this.layers[i];
				if (layer.size != size) {
					layer.size = size;
					var element = layer.element;
					var img = this.layers[i].img;
					if (size == 0) {
						element.style.display='none';
					} else {
						element.style.display='';
						element.style.width=size+'px';	  
						if (size < 80) {
							height = (size*65/80); // nasconde la scritta
						} else {
							height = size;
						}
						element.style.height=height+'px';
						element.style.marginTop=((80-size)/2)+'px';
						img.style.width=size+'px';
						img.style.height=size+'px';
						
						if (opacity < 100) {
							element.style.opacity=(opacity / 100);
						} else {
							element.style.opacity='1';
						}
						//div_obj.style.filter='alpha(opacity='+opacity+')';
					}
				}
				//appbar_icoset('app-'+i,size,opacity);
				// s=s+" i="+i+" d="+d+" size="+size;
			}
			
			// sistemo l'offset
			var totsize = 0;
			for (var i=0; i < p; i++) {
				totsize += (80 - this.layers[i].size);
			}			
			var offset = Math.round(p*80) - 40 - totsize;
			if (offset >= 80) {
				offset -= 80;
			}
			this.scroller.style.left = '-'+offset+'px';
			//AUI.Logger.info("resto="+resto+" totsize="+totsize+" p80="+Math.round(p*80)+" offset="+offset);

		},
		
		startTimer : function() {
			this.timestamp = new Date().getTime();
			clearInterval(this.timer);
			this.onTimer();
		},
		
		onTimer : function() {
			var dx = 0;
			if (this.target != null) {
				dx = this.target - this.position;
				this.target = this.bound(this.target);
				if (dx == 0) {
					this.speed = 0;
				} else if (Math.abs(this.speed) < this.minSpeed) {
					this.speed = dx / Math.abs(dx) * this.minSpeed; 
				} else if (Math.abs(this.speed) < this.maxSpeed) {
					this.speed = this.speed * ( 1 + 10 * this.updateInterval / 1000); 
				}				
				AUI.Logger.log("target="+this.target+" position="+this.position+" dx="+dx+" speed="+this.speed);
			}
			if (Math.abs(this.speed) > this.maxSpeed) {
				this.speed = this.speed / Math.abs(this.speed) * this.maxSpeed;
			}
			var t = (new Date()).getTime();
			var dt = t - this.timestamp;
			this.timestamp = t;
			var dp = this.speed * (dt) / 1000.0 / 80.0;
			AUI.Logger.log("dp="+dp);
			if (Math.abs(dp) > 0 && Math.abs(dx) > 0 && Math.abs(dx / dp) < 1) {
				dp = dx;
				this.target = null;
				this.speed = 0;
			}
			//AUI.Logger.debug("dt="+dt+" dp="+dp+" position="+this.position);
			this.position += dp;
			this.update();
			if (Math.abs(this.speed) > 0) {
				var self = this;
				this.timer = setTimeout(function() { return self.onTimer() },this.updateInterval);
				//AUI.Logger.debug("elapsed="+((new Date().getTime()) - t));
			} else {
				AUI.Pages.showLayer(this.layers[Math.round(this.position)].layer);				
			}
			return false;
		},

		bound : function(p) {
			var l = this.layers.length; 
			while (p < 2) {
				p += l - 5;
			}
			while (p > (l - 3)) {
				p -= l - 5;
			}
			return p;
		}
				
	};
	
	
}
