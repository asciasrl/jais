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
     	}
     };
}
