/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */

/**
 * Coordinata y del bordo superiore del bottone "up".
 */
const BLIND_UP_TOP = 12;

/**
 * Coordinata x del bordo sinistro dei bottoni "up".
 */
const BLIND_LEFT = 22;

/**
 * Coordinata y del bordo inferiore del bottone "up".
 */
const BLIND_UP_BOTTOM = 80;

/**
 * Coordinata x del bordo destro dei bottoni.
 */
const BLIND_RIGHT = 74;

/**
 * Coordinata y del bordo inferiore del bottone "down".
 */
const BLIND_DOWN_TOP = 100;

/**
 * Coordinata y del bordo inferiore del bottone "down".
 */
const BLIND_DOWN_BOTTOM = 156;

/**
 * Altezza del controllo tapparelle.
 */
const BLIND_CONTROL_HEIGHT = 180;

/**
 * Larghezza del controllo tapparelle.
 */
const BLIND_CONTROL_WIDTH = 80;

/**
 * Coordinata y del controllo.
 */
var blindControlTop;

/**
 * Coordinata x del controllo.
 */
var blindControlLeft;

/**
 * Layer che contiene lo sfondo e il controllo.
 */
var blindLayer = document.getElementById("blind");

/**
 * Layer che contiene il controllo.
 */
var blindControlLayer = document.getElementById("blind-control");

/**
 * Icona della tapparella che stiamo modificando in questo momento
 * (&lt;div&gt;).
 */
var blindBeingControlled = false; 

/**
 * Nome della tapparella che stiamo controllando.
 */
var blindName;

/**
 * Elemento che contiene ("value") lo stato di questa tapparella.
 */
var blindStatusElement;

/**
 * Icona di questa tapparela.
 */
var blindIconOnMap;

/**
 * Posizione nel documento del controllo delle tapparelle.
 *
 * <p>Questa e' necessaria per capire dove e' stato fatto il click.</p>
 */
var blindControlPosition;

/**
 * Mostra il controllo delle tapparelle.
 *
 * <p>Se c'e' gia' una tapparella sotto controllo, mostra un messaggio.</p>
 *
 * @param divOnMap elemento div che contiene l'icona della tapparella
 */
function showBlindControl(divOnMap) {
	var maxTop, maxLeft, minTop, minLeft;
	var iconTop = parseInt(divOnMap.style.top.slice(0, -2));
	var iconLeft = parseInt(divOnMap.style.left.slice(0, -2));
	if (blindBeingControlled) {
		// Stiamo gia' controllando un'altra tapparella.
		statusMessage("Riprova.");
		return;
	}
	// Se siamo su fisso, vogliamo che lo slider appaia nello schermo
	if (MOBILE) {
		maxTop = mapSize.y - BLIND_CONTROL_HEIGHT;
		maxLeft = mapSize.x - BLIND_CONTROL_WIDTH;
		minTop = 0;
		minLeft = 0;
	} else {
		maxTop = -currentMapPosition.y + MAP_AREA_HEIGHT - 
			BLIND_CONTROL_HEIGHT;
		maxLeft = -currentMapPosition.x + MAP_AREA_WIDTH - 
			BLIND_CONTROL_WIDTH;
		minTop = -currentMapPosition.y;
		minLeft = -currentMapPosition.x;
	}
	blindControlTop = iconTop -	BLIND_CONTROL_HEIGHT / 2;
	blindControlLeft = iconLeft - BLIND_CONTROL_WIDTH / 4; // un pochino sopra
	// Non usciamo dall'area della mappa
	if (blindControlTop < minTop) {
		blindControlTop = minTop;
	} else if (blindControlTop > maxTop) {
		blindControlTop = maxTop;
	}
	if (blindControlLeft < minLeft) {
		blindControlLeft = minLeft;
	} else if (blindControlLeft > maxLeft) {
		blindControlLeft = maxLeft;
	}
	blindControlLayer.style.top = blindControlTop + "px";
	blindControlLayer.style.left = blindControlLeft + "px";
	// Lo sfondo scuretto deve coprire tutta la mappa
	blindLayer.style.left = currentMapPosition.x;
	blindLayer.style.top = currentMapPosition.y;
	blindLayer.style.width = mapSize.width + "px";
	blindLayer.style.height = mapSize.height + "px";
	blindLayer.style.display = "";
	blindName = divOnMap.attributes.getNamedItem("name").value;
	var attributes = divOnMap.attributes;
	blindStatusElement = attributes.getNamedItem("status");
	blindIconOnMap = divOnMap.firstChild.firstChild;
	blindBeingControlled = divOnMap;
	blindControlPosition = getPosition(blindControlLayer);
	statusMessage(blindName);
}

/**
 * Apre la tapparella.
 */
function openBlind() {
	var addressOpen = 
		blindBeingControlled.attributes.getNamedItem("addressopen").value;
	blindStatusElement.value = "opening";
	blindIconOnMap.src = IMG_BLIND_OPENING;
	setPort(addressOpen, "ON", blindCallback);
}

/**
 * Chiude la tapparella.
 */
function closeBlind() {
	var addressClose = 
		blindBeingControlled.attributes.getNamedItem("addressclose").value;
	blindStatusElement.value = "closing";
	blindIconOnMap.src = IMG_BLIND_CLOSING;
	setPort(addressClose, "ON", blindCallback);
}

/**
 * Ferma la tapparella che si sta aprendo o chiudendo.
 *
 * @param opening true se la tapparella si sta aprendo, false se si sta  
 * chiudendo.
 */
function stopBlind(opening) {
	var address;
	if (opening) { 
		address = 
			blindBeingControlled.attributes.getNamedItem("addressopen").value;
	} else {
		address = 
			blindBeingControlled.attributes.getNamedItem("addressclose").value;
	}
	blindStatusElement.value = "still";
	blindIconOnMap.src = IMG_BLIND_STILL;
	setPort(address, "OFF", blindCallback);
}

/**
 * Riceve le risposte delle richieste fatte da blindClicked() e "libera" la
 * tapparella controllata.
 *
 * <p>In caso di errore, ripristina l'icona "fermo".</p>
 */
function blindCallback(ok) {
	if (!ok) {
		blindBeingControlled.attributes.getNamedItem("status").value = "still";
		blindBeingControlled.firstChild.firstChild.src = IMG_BLIND_STILL;
	}
	blindBeingControlled = false;
}


/**
 * Risponde a un click sul controllo.
 *
 * <p>L'evento viene bloccato per evitare che sia ricevuto dallo sfondo.</p>
 */
function blindControlClicked(event) {
	var mousePos = mouseCoords(event, blindControlPosition);
	if ((mousePos.x >= BLIND_LEFT) && (mousePos.x <= BLIND_RIGHT)) {
		if ((mousePos.y >= BLIND_UP_TOP) && (mousePos.y <= BLIND_UP_BOTTOM)) {
			// Premuto "UP"
			switch(blindStatusElement.value) {
			case "still":
				openBlind();
				break;
			case "closing":
				stopBlind(false);
				break;
			default:
				blindBeingControlled = false;
			}
			hideBlindControl();
		} else if ((mousePos.y >= BLIND_DOWN_TOP) && 
			(mousePos.y <= BLIND_DOWN_BOTTOM)) {
			// Premuto "DOWN"
			switch(blindStatusElement.value) {
			case "still":
				closeBlind();
				break;
			case "opening":
				stopBlind(true);
				break;
			default:
				blindBeingControlled = false;
			}
			hideBlindControl();
		} // Altrimenti scartiamo il click e basta.
	} 
	// Blocchiamo l'evento.
	event = event || window.event;
	event.stopPropagation();
}

/**
 * Nasconde il controllo della tapparella.
 *
 * <p>Questa funzione viene chiamata quando il controllo e' visualizzato e 
 * l'utente fa click fuori.</p>
 */
function hideBlindControl() {
	blindLayer.style.display = "none";
}

/**
 * Riceve un click sullo sfondo e chiude il controllo.
 */
function blindBackgroundClicked() {
	blindBeingControlled = false;
	hideBlindControl();
}
