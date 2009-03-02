/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */

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
 */
function showBlindControl() {
	var maxTop, maxLeft, minTop, minLeft;
	var iconTop = parseInt(blindBeingControlled.style.top.slice(0, -2));
	var iconLeft = parseInt(blindBeingControlled.style.left.slice(0, -2));
	// Se siamo su fisso, vogliamo che lo slider appaia nello schermo
	if (MOBILE) {
		maxTop = mapSize.y - BLIND_CONTROL_HEIGHT;
		maxLeft = mapSize.x - BLIND_CONTROL_WIDTH;
		minTop = STATUS_BAR_HEIGHT;
		minLeft = 0;
	} else {
		maxTop = -currentMapPosition.y + MAP_AREA_HEIGHT - 
			BLIND_CONTROL_HEIGHT;
		maxLeft = -currentMapPosition.x + MAP_AREA_WIDTH - 
			BLIND_CONTROL_WIDTH;
		minTop = -currentMapPosition.y + STATUS_BAR_HEIGHT;
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
	blindControlPosition = getPosition(blindControlLayer);
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
			openBlind();
			hideBlindControl();
		} else if ((mousePos.y >= BLIND_DOWN_TOP) && 
			(mousePos.y <= BLIND_DOWN_BOTTOM)) {
			// Premuto "DOWN"
			closeBlind();
			hideBlindControl();
		} // Altrimenti scartiamo il click e basta.
	} 
	// Blocchiamo l'evento.
	event = event || window.event;
	event.stopPropagation();
}

/**
 * L'utente ha fatto click su una tapparella.
 *
 * <p>Se la tapparella si sta muovendo, la fermiamo. Altrimenti, apriamo il
 * controllo.</p>
 */
function blindClicked(event, blindDiv) {
	if (blindBeingControlled) {
		// Stiamo gia' controllando un'altra tapparella.
		statusMessage("Riprova.");
		return;
	}
	blindBeingControlled = blindDiv;
	var attributes = blindBeingControlled.attributes;
	blindStatusElement = attributes.getNamedItem("status");
	blindIconOnMap = blindBeingControlled.firstChild.firstChild;
	blindName = blindBeingControlled.attributes.getNamedItem("name").value;
	switch(blindStatusElement.value) {
		case "opening":
			stopBlind(true);
			break;
		case "closing":
			stopBlind(false);
			break;
		default:
			showBlindControl();
	}
	statusMessage(blindName);
	/*
	var status = blindDiv.attributes.getNamedItem("status");
	var icon = blindDiv.firstChild.firstChild;
	statusMessage(icon.alt);
	if (blindBeingControlled == false) {
		// Non c'e' nessun'altra richiesta in corso per tapparelle
		blindBeingControlled = blindDiv;
		if (status.value == "still") {
			// FIXME: dobbiamo permettere di fare sia open sia close.
			var addressOpen = blindDiv.attributes.getNamedItem("addressopen").value;
			status.value = "opening";
			icon.src = IMG_BLIND_OPENING;
			setPort(addressOpen, "ON", blindCallback);
		} else if (status.value == "opening") {
			var addressOpen = blindDiv.attributes.getNamedItem("addressopen").value;
			status.value = "still";
			icon.src = IMG_BLIND_STILL;
			setPort(addressOpen, "OFF", blindCallback);
		} else if (status.value == "closing") {
			var addressClose = blindDiv.attributes.getNamedItem("addressclose").value;
			status.value = "still";
			icon.src = IMG_BLIND_STILL;
			setPort(addressClose, "OFF", blindCallback);
		}
	} else {
		statusMessage("Riprova.");
	}
	event.stopPropagation();
	*/
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
