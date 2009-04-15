/**
 * Gestisce il passaggio da una pagina all'altra, avviando la richiesta stream
 * @requires StreamRequest
 * @static
 */

if (!AUI.Page) {

	AUI.Page = {
		currentPage: null,
		currentPageId: null
	};

	AUI.Page.changeTo = function(toPageId) {
		var newPage = document.getElementById(toPageId);
		if (newPage == null) {			
			throw("Pagina '"+toPageId+"' non trovata");
		}
		if (this.currentPage != null) {
			this.currentPage.style.display = 'none'
		}
		newPage.style.display = '';
		this.currentPage = newPage;
		if (AUI.StreamRequest) {
			AUI.StreamRequest.start();
		}
		AUI.Logger.info("Cambiata pagina:"+toPageId);
	};

	AUI.Page.change = function(fromPageId,toPageId) {
		this.currentPage = document.getElementById(fromPageId);
		this.changeTo(toPageId);
	};

	AUI.Page.setCurrentPageId = function(pageId) {
		this.currentPageId = pageId;
	};

	AUI.Page.getCurrentPageId = function() {
		if (this.currentPage != null) {
			return this.currentPage.id;
		} else {
			return this.currentPageId;
		}				
	};

};
