/**
 * appbar_simple.js
 * (C) 2008 Ascia S.r.l.
 */

/**
 * Numero dell'icona attualmente centrata (selezionata).
 */
var selectedAppbarIconNumber;

/**
 * Mostra la barra delle applicazioni.
 *
 * <p>Questa funzione legge selectedAppBarIconNumber.</p>
 */
function appbar_show() {
	// determino per ogni icona l'opacita'
	for (i=1; i <= appbar_num; i++) {
		var size, opacity;
		if (i == selectedAppbarIconNumber + 1) {
			opacity = 100;
			size = 80;
		} else {
			opacity = 50;
			size = 65;
		}
		var div_obj = appbar_icoset('app-'+i,size,opacity, true);
		// Le icone piccole devono essere piu' distanziate da quelle vicine.
		var newMargin = ((80-size)/2)+'px';
		div_obj.style.marginLeft = newMargin;
		div_obj.style.marginRight = newMargin;
	}
}

//appbar_show(300);
//appbar_show(320);


/**
 * Attiva il servizio rappresentato dall'icona piu' vicina alla posizione in
 * cui e' arrivato il click del mouse.
 */
function selectClickedIcon(x) {
	selectedAppbarIconNumber = Math.round((x - 40) / 80);
	var selectedService = SERVICES[selectedAppbarIconNumber];
	iconSelected(selectedService);
	appbar_show();
	//statusMessage("x: " + x + "  -> " + selectedService);
}

/**
 * Gestisce un trascinamento sull'appbar.
 *
 * @param mousePos posizione attuale del mouse relativa all'appbar.
 */
function dragAppBar(mousePos) {
	selectClickedIcon(mousePos.x); 
}

/**
 * Risponde a un click.
 *
 * <p>Questa funzione viene chiamata quando il bottone del mouse viene 
 * rilasciato.</p>
 *
 * @param mousePos posizione del mouse relativa all'appbar.
 * @param timeStamp istante in cui il bottone viene rilasciato.
 */
function dragAppBarStop(mousePos, timeStamp) {
	selectClickedIcon(mousePos.x);
}

selectClickedIcon(APPBAR_START_POSITION);
makeDraggable(document.getElementById('appbar'));