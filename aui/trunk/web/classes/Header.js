if (!AUI.Header) {
	
	AUI.Header = {
		header: null,
		out: null,
		opacity: 0.8, // opacita della intestazione [0 .. 1]
		fadeDelay: 2000, // ritardo prima che cominci a sfumare
		fadeTime: 1000, // tempo di sfumatura
		fadeInterval: 40, // intervallo aggiornamento
		timestamp: 0,
		fadeTimer: 0,

		show : function(message) {
			if (this.header == null) {
				this.header = document.getElementById("header");
			}
			AUI.Logger.info("message="+message);
			this.header.innerHTML = message;
			if (this.out == null) {
				this.out = document.getElementById("header-out");
			}
			this.out.style.opacity = this.opacity;
			this.out.style.display = "block";
			this.out.style.left = AUI.getScrollX() + "px";
			this.out.style.top = AUI.getScrollY() + "px";
			this.out.style.width = AUI.getInnerWidth() + "px";
			if (this.fadeTimer > 0) {
				clearInterval(this.fadeTimer);
			}
			this.timestamp = new Date().getTime();
			var self = this;
			this.fadeTimer = setTimeout(function() { return self.fade() },this.fadeInterval);
		},

		fade : function() {
			var x = Math.max(0,(new Date().getTime() - this.timestamp - this.fadeDelay) / this.fadeTime);
			var v = this.opacity * (1 - x);
			if (v <= 0) {
				this.out.style.display = "none";
			} else {
				this.out.style.opacity = v;
				this.out.style.left = AUI.getScrollX() + "px";
				this.out.style.top = AUI.getScrollY() + "px";
				var self = this;
				this.fadeTimer = setTimeout(function() { return self.fade() },this.fadeInterval);
			}
		}
	};
}
