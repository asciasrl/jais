if (!AUI.Upload) {
	
	AUI.Upload = {
		
		frame : function(c) {
	 
			var n = 'f' + Math.floor(Math.random() * 99999);
			var d = document.createElement('DIV');
			d.innerHTML = '<iframe style="display:none" src="about:blank" id="'+n+'" name="'+n+'" onload="AUI.Upload.loaded(\''+n+'\')"></iframe>';
			document.body.appendChild(d);
	 
			var i = document.getElementById(n);
			if (c && typeof(c.onComplete) == 'function') {
				i.onComplete = c.onComplete;
			}
	 
			return n;
		},
	 
		form : function(f, name) {
			f.setAttribute('target', name);
		},
	 
		submit : function(f, c) {
			this.form(f, this.frame(c));
			if (c && typeof(c.onStart) == 'function') {
				return c.onStart();
			} else {
				return true;
			}
			if (console) console.log("Submit ...");
		},
	 
		loaded : function(id) {
			var i = document.getElementById(id);
			if (i.contentDocument) {
				var d = i.contentDocument;
			} else if (i.contentWindow) {
				var d = i.contentWindow.document;
			} else {
				var d = window.frames[id].document;
			}
			if (d.location.href == "about:blank") {
				return;
			}
	 
			if (typeof(i.onComplete) == 'function') {
				var res;
				if(d && d.body){
					if (d.body.childNodes.length == 1 && d.body.firstChild.localName.toLowerCase() == "pre") {
						res = d.body.firstChild.innerHTML;
					} else { 
						res = d.body.innerHTML; 
					}
				} 
				if(d && d.XMLDocument) {
					res = d.XMLDocument; 				
				}				
				i.onComplete(res);
			}
		}	 
	}
}