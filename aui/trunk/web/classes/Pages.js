/**
 * Gestisce il passaggio da una pagina all'altra, avviando la richiesta stream
 * @requires StreamRequest
 * @static
 */

if (!AUI.Pages) {

	AUI.Pages = {
		currentPage: null,
		currentLayer: null,
		currentPageId: null
	};

	AUI.Pages.init = function() {
		if (this.currentPageId != null) {
			this.changeTo(this.currentPageId);
		}
		var self = this;
		this.pageMouseMove = function(e) { return self.onPageMouseMove(e); };
		this.pageMouseUp = function(e) { return self.onPageMouseUp(e); };
		this.pageMouseOut = function(e) { return self.onPageMouseUp(e); };
	};
	
	AUI.Pages.changeTo = function(toPageId) {
		var newPage = document.getElementById(toPageId);
		if (newPage == null) {			
			throw("Pagina '"+toPageId+"' non trovata");
		}
		if (this.currentPage != null) {
			this.currentPage.style.display = 'none';
		}
		newPage.style.display = 'block';
		window.scroll(0,0);
		this.currentPage = newPage;
		this.currentPageId = toPageId;
		AUI.Logger.info("Cambiata pagina:"+toPageId);
		if (AUI.Pages.pageHaveLayers(toPageId)) {
			if (AUI.StreamRequest) {
				AUI.Logger.info("Start StreamRequest");
				AUI.StreamRequest.start();
			}
			AUI.Layers.show();
		} else {
			if (AUI.StreamRequest) {
				AUI.Logger.info("Stop StreamRequest");
				AUI.StreamRequest.stop();
			}
			AUI.Layers.hide();
		}
	};
	
	AUI.Pages.onMouseDown = function(pageId, event) {
		if (event.preventDefault) {
			event.preventDefault();
		} else {
			event.returnValue = false;
		}
		if (event.stopPropagation) {
			event.stopPropagation();
		} else if (window.event) {
			window.event.cancelBubble = true;
		} 
		AUI.Layers.hide();
		this.currentPage.addEventListener('mousemove', this.pageMouseMove, false);
		this.currentPage.addEventListener('mouseup', this.pageMouseUp, false);
		this.currentPage.addEventListener('mouseout', this.pageMouseOut, false);
		this.scrollStartX = event.clientX + window.scrollX;
		this.scrollStartY = event.clientY + window.scrollY;
		this.scrollMaxX = this.currentPage.scrollWidth - window.innerWidth; 
		this.scrollMaxY = this.currentPage.scrollHeight - window.innerHeight;
		return false;
	};
	
	AUI.Pages.onPageMouseUp = function(event) {
		if (event.preventDefault) {
			event.preventDefault();
		} else {
			event.returnValue = false;
		}
		if (event.stopPropagation) {
			event.stopPropagation();
		} else if (window.event) {
			window.event.cancelBubble = true;
		} 
		AUI.Logger.info("MouseUp "+this.currentPageId);
		if (this.pageHaveLayers(this.currentPageId)) {
			AUI.Layers.show();
		}
		this.currentPage.removeEventListener('mousemove', this.pageMouseMove, false);
		this.currentPage.removeEventListener('mouseup', this.pageMouseUp, false);
		this.currentPage.removeEventListener('mouseout', this.pageMouseOut, false);
		return false;
	};

	AUI.Pages.onPageMouseMove = function(event) {
		if (event.preventDefault) {
			event.preventDefault();
		} else {
			event.returnValue = false;
		}
		if (event.stopPropagation) {
			event.stopPropagation();
		} else if (window.event) {
			window.event.cancelBubble = true;
		} 

		var dx = event.clientX - this.scrollStartX;
		var dy = event.clientY - this.scrollStartY;
		var x = -dx;
		var y = -dy;
		
		if (x < 0) {
			x = 0;
		} else if (x > this.scrollMaxX) {
			x = this.scrollMaxX;
		}
		if (y < 0) {
			y = 0;
		} else if (y > this.scrollMaxY) {
			y = this.scrollMaxY;
		}
		
		window.scroll(x,y);
		AUI.Logger.info("dx="+dx+" dy="+dy+" x="+x+" y="+y);
		
		// TODO
		return false;
	};
	
	AUI.Pages.pageHaveLayers = function(pageId) {
		var o = AUI.Pages.pageLayers[pageId];
		if (typeof(o) == 'object') {
			for (i in o) {
				if (i != 'null' && i != '') {
					return true;
				}
			}
		}
		return false;
	};

	AUI.Pages.change = function(fromPageId,toPageId) {
		this.currentPage = document.getElementById(fromPageId);
		this.changeTo(toPageId);
	};

	AUI.Pages.setCurrentPageId = function(pageId) {
		this.currentPageId = pageId;
	};

	AUI.Pages.getCurrentPageId = function() {
		if (this.currentPage != null) {
			return this.currentPage.id;
		} else {
			return this.currentPageId;
		}				
	};

	AUI.Pages.getCurrentName = function() {
		return this.currentPageId.substring(5);		
	};

	AUI.Pages.getLayerControls = function(layer) {
		var pageId = this.getCurrentPageId();
		var layers = this.pageLayers[pageId];
		return layers[layer];
	};
	
	AUI.Pages.showLayer = function(layer) {
		var pageId = this.getCurrentPageId();
		var layers = this.pageLayers[pageId];
		for (i in layers) {
			if (i == 'null' || i == '') {
				continue;
			}
			if (i == layer) {
				this.setLayerVisibility(i,true);
			} else {
				this.setLayerVisibility(i,false);
			}
		}
		this.currentLayer = layer;
	};

	AUI.Pages.hideLayer = function(layer) {
		this.setLayerVisibility(layer,false);		
	};

	AUI.Pages.setLayerVisibility = function(layer,status) {
		var controls = this.getLayerControls(layer);
		if (controls == undefined) {
			AUI.Logger.debug("Nessun controllo sul layer "+layer);
		} else {
			for (var i=0; i < controls.length; i++) {
				var id = controls[i];
				var device = AUI.Controls.getDevice(id);
				if (device) {
					var element = device.getElement();
					if (element) {
						if (status) {
							element.style.display = 'block';
						} else {
							element.style.display = 'none';
						}
					} else {
						AUI.Logger.error("Non trovato elemento per "+id);
					}
				} else {
					AUI.Logger.error("Non trovato device "+id);
				}
			}
		}
	};

};
