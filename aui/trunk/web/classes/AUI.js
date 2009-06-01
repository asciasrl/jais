if (!AUI) {
    /**
     * The AUI global namespace object.
     * @class AUI
     * @static
     */
     var AUI = {
    	init : function(page) {
    	 	AUI.Logger.info("init AUI");
    	 	AUI.Layers.init();
    	 	window.setTimeout(function() { AUI.Pages.init() }, 1000);
     	},
     	
     	getScrollX : function() {
     		if (window.scrollX) {
     			return window.scrollX;
     		} else if (document.compatMode == "CSS1Compat") {
     			return document.body.parentNode.scrollLeft;
     		} else if (document.body.scrollLeft) {
     			return document.body.scrollLeft;
     		}
     		return 0;
     	},
     	
     	getScrollY : function() {
     		if (window.scrollY) {
     			return window.scrollY;
     		} else if (document.compatMode == "CSS1Compat") {
     			return document.body.parentNode.scrollTop;
     		} else if (document.body.scrollLeft) {
     			return document.body.scrollTop;
     		}
     		return 0;
     	},
     	
     	getInnerWidth : function() {
     		if (window.innerWidth) {
     			return window.innerWidth;
     		} else if (document.documentElement.clientWidth && document.documentElement.clientWidth > 0) {
     			return document.documentElement.clientWidth;
     		} else if (document.body.clientWidth) {
     			return document.body.clientWidth;
     		}
     		return 0;
     	},
     	
     	getInnerHeight : function() {
     		if (window.innerHeight) {
     			return window.innerHeight;
     		} else if (document.documentElement.clientHeight && document.documentElement.clientHeight > 0) {
     			return document.documentElement.clientHeight;
     		} else if (document.body.clientHeight) {
     			return document.body.clientHeight;
     		}
     		return 0;
     	}


     };
}
