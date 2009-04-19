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

	AUI.Pages.changeTo = function(toPageId) {
		var newPage = document.getElementById(toPageId);
		if (newPage == null) {			
			throw("Pagina '"+toPageId+"' non trovata");
		}
		if (this.currentPage != null) {
			this.currentPage.style.display = 'none'
		}
		newPage.style.display = '';
		window.scroll(0,0);
		this.currentPage = newPage;
		AUI.Logger.info("Cambiata pagina:"+toPageId);
		if (AUI.StreamRequest) {
			AUI.StreamRequest.start();
		}
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
	
	AUI.Pages.getLayerControls = function(layer) {
		var pageId = this.getCurrentPageId();
		var layers = this.pageLayers[pageId];
		return layers[layer];
	}
	
	AUI.Pages.showLayer = function(layer) {
		if (this.currentLayer != null) {
			if (this.currentLayer != layer) {
				this.hideLayer(this.currentLayer);
			} 
		}
		this.setLayerVisibility(layer,true);
		this.currentLayer = layer;
	}

	AUI.Pages.hideLayer = function(layer) {
		this.setLayerVisibility(layer,false);		
	}

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
	}

};
