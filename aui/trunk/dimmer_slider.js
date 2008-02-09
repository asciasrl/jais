/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
 
/**
 * Altezza dell'immagine del cursore del dimmer.
 *
 * <p>Questa grandezza serve a centrare il cursore del mouse e il cursore del
 * dimmer. Deve essere pari alla meta' dell'altezza dell'immagine che
 * rappresenta il cursore.</p>
 */
const DIMMER_CURSOR_MIDDLE = 30;
/**
 * Massima altezza del cursore del dimmer.
 *
 * <p>Nota: la massima altezza e' minore della minima: il livello del dimmer
 * aumenta verso l'alto!</p>
 */
const DIMMER_TOP_MIN = 23;

/**
 * Minima altezza cursore del dimmer.
 *
 * <p>Nota: questa deve essere l'altezza minima del bordo superiore del
 * cursore!</p>
 *
 * @see DIMMER_TOP_MIN
 */
const DIMMER_TOP_MAX = 217;

/**
 * Minima variazione per cui il cursore viene spostato.
 *
 * <p>Questo parametro riduce la fluidita' ma aumenta la velocita' di risposta
 * del controllo.</p>
 *
 * <p>Il valore deve essere maggiore o uguale a 1.</p>
 */
const DIMMER_JERKINESS = 5;

/**
 * Valore attuale del dimmer.
 */
var currentDimmerValue = 0;

/**
 * Altezza attuale del cursore del dimmer.
 *
 * <p>Questo valore puo' differire dal corrispondente currentdimmerValue
 * per la jerkiness.</p>
 */
var currentDimmerCursorTop = DIMMER_TOP_MIN;
  
/**
 * Layer che contiene il controllo dimmer.
 */
var dimmerLayer = document.getElementById("dimmer");

/**
 * Layer che contiene il solo cursore del dimmer.
 */
var dimmerCursorLayer = document.getElementById("dimmer-tasto");

/**
 * Nome del dimmer.
 */
var dimmerName;

/**
 * Indirizzo del dimmer sul bus.
 */
var dimmerAddress;

/**
 * Elemento che contiene il valore a cui si trova il dimmer.
 */
var dimmerValueElement;

/**
 * Icona del dimmer sulla mappa.
 */
var dimmerIconOnMap;

/**
 * Mostra il cursore del dimmer.
 *
 * @param divOnMap elemento div che contiene l'icona del dimmer
 * @address indirizzo del dimmer sul bus
 */
function showDimmer(divOnMap) {
	dimmerLayer.style.display = "";
	dimmerName = divOnMap.firstChild.alt;
	var attributes = divOnMap.attributes;
	dimmerValueElement = attributes.getNamedItem("lit");
	dimmerIconOnMap = divOnMap.firstChild;
	dimmerAddress = attributes.getNamedItem("busaddress").value;
	currentDimmerCursorTop = (100 - dimmerValueElement.value) / 100 * 
		(DIMMER_TOP_MAX - DIMMER_TOP_MIN);
	dimmerCursorLayer.style.top = currentDimmerCursorTop + "px";
	statusObject.innerHTML = dimmerName + ": " + dimmerValueElement.value;
}

/**
 * Nasconde il cursore del dimmer.
 *
 * <p>Questa funzione viene chiamata quando il dimmer e' visualizzato e l'utente
 * fa click fuori dal cursore.</p>
 */
function hideDimmer() {
	dimmerLayer.style.display = "none";
}	

/**
 * Segue il trascinamento del cursore.
 *
 * @param mousePos posizione del mouse relativa allo slider.
 */
function dragDimmerCursor(mousePos) {
	var y = mousePos.y - DIMMER_CURSOR_MIDDLE;
	if (y < DIMMER_TOP_MIN) {
		y = DIMMER_TOP_MIN;
	} else if (y > DIMMER_TOP_MAX) {
		y = DIMMER_TOP_MAX;
	}
	var newValue = 100 - Math.floor((y - DIMMER_TOP_MIN) / 
		(DIMMER_TOP_MAX - DIMMER_TOP_MIN) * 100);	
	statusObject.innerHTML = dimmerName + ": " + newValue;
	// Il massimo e il minimo valore sono anti-jerkiness, perche' e' brutto
	// se il cursore non arriva in fondo.
	if ((Math.abs(y - currentDimmerCursorTop) > DIMMER_JERKINESS) ||
		(newValue == 0) || (newValue == 100)) {
		dimmerCursorLayer.style.top = y + "px";
		currentDimmerCursorTop = y;
	}
	if (setPort(dimmerAddress, newValue)) { 
		// Aggiorniamo l'icona sulla mappa, se necessario
		if (newValue * dimmerValueElement.value == 0) {
			if (newValue == 0) {
				dimmerIconOnMap.src = IMG_LIGHT_OFF;
			} else {
				dimmerIconOnMap.src = IMG_LIGHT_ON;
			}
		}
		dimmerValueElement.value = newValue;
	} else { // Errore: fissiamo lo slider a 0
		dimmerCursorLayer.style.top = DIMMER_TOP_MAX + "px";
	}
}

/**
 * Chiamata quando lo slider riceve un click.
 *
 * <p>Blocca la propagazione del click, altrimenti questo sarebbe ricevuto dal
 * div padre che chiuderebbe lo slider.</p>
 */
function dimmerSliderClicked(event) {
	event = event || window.event;
	event.stopPropagation();
}