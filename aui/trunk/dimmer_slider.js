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
 * Minima variazione per cui il cursore viene spostato. [pixel]
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
 * Elemento che contiene il testo vicino all'icona del dimmer sulla mappa.
 */
var dimmerTextOnMap;

/**
 * Calcola la posizione del cursore del dimmer a partire dal valore (0-100).
 */
function dimmerValue2Cursor(value) {
	if (value >= 100) {
		return DIMMER_TOP_MIN;
	} else if (value <= 0) {
		return DIMMER_TOP_MAX;
	} 
	return Math.floor((100 - value) / 100 *
		(DIMMER_TOP_MAX - DIMMER_TOP_MIN) + DIMMER_TOP_MIN);
}

/**
 * Calcola il valore dell'intensita' luminosa a partire dalla posizione del
 * cursore.
 */
function dimmerCursor2Value(cursorTop) {
	var y = cursorTop - DIMMER_CURSOR_MIDDLE; // Centriamo il centro del cursore
	if (y <= DIMMER_TOP_MIN) {
		return 100;
	} else if (y >= DIMMER_TOP_MAX) {
		return 0;
	}
	return 100 - Math.floor((y - DIMMER_TOP_MIN) / 
		(DIMMER_TOP_MAX - DIMMER_TOP_MIN) * 100);
}

/**
 * Mostra il cursore del dimmer.
 *
 * @param divOnMap elemento div che contiene l'icona del dimmer
 * @address indirizzo del dimmer sul bus
 */
function showDimmer(divOnMap) {
	dimmerLayer.style.display = "";
	dimmerName = divOnMap.attributes.getNamedItem("name").value;
	var attributes = divOnMap.attributes;
	dimmerValueElement = attributes.getNamedItem("lit");
	dimmerIconOnMap = divOnMap.firstChild.firstChild;
	dimmerTextOnMap = divOnMap.lastChild.firstChild;
	dimmerAddress = attributes.getNamedItem("busaddress").value;
	currentDimmerCursorTop = dimmerValue2Cursor(dimmerValueElement.value); 
	dimmerCursorLayer.style.top = currentDimmerCursorTop + "px";
	statusMessage(dimmerName + ": " + dimmerValueElement.value);
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
 * Riceve la risposta alla richiesta setPort sul dimmer.
 *
 * <p>Aggiorna la posizione del cursore e l'icona sulla mappa.</p>
 */
function dimmerSetCallback(ok) {
	if (ok) { 
		// Aggiorniamo l'icona sulla mappa
		if (currentDimmerValue == 0) {	
			dimmerIconOnMap.src = IMG_LIGHT_OFF;
		} else {
			dimmerIconOnMap.src = IMG_LIGHT_ON;
		}
		dimmerTextOnMap.textContent = currentDimmerValue + "%";
		dimmerValueElement.value = currentDimmerValue;
	}
}

/**
 * Segue il trascinamento del cursore.
 *
 * @param mousePos posizione del mouse relativa allo slider.
 * @param forceUpdate facoltativo, se true impone che il cursore venga spostato.
 */
function dragDimmerCursor(mousePos, forceUpdate) {
	var y = mousePos.y;
	var newValue = dimmerCursor2Value(y);
	var newTop = dimmerValue2Cursor(newValue);
	statusMessage(dimmerName + ": " + newValue);
	// Il massimo e il minimo valore sono anti-jerkiness, perche' e' brutto
	// se il cursore non arriva in fondo.
	if (forceUpdate || 
		(Math.abs(newTop - currentDimmerCursorTop) >= DIMMER_JERKINESS) ||
		(newValue == 0) || (newValue == 100)) {
		dimmerCursorLayer.style.top = newTop + "px";
		currentDimmerCursorTop = newTop;
	}
	currentDimmerValue = newValue;
	setPort(dimmerAddress, newValue, dimmerSetCallback);
}

/**
 * Conclude il trascinamento del cursore (o risponde a un click).
 *
 * @param mousePos posizione del mouse relativa allo slider.
 */
function dragDimmerStop(mousePos) {
	dragDimmerCursor(mousePos, true);
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