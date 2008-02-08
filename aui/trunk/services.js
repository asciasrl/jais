/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
 
/**
 * L'utente ha fatto click su una luce.
 */
function lightClicked(lightDiv) {
	var lightElement = lightDiv.firstChild;
	var lit = lightDiv.attributes.getNamedItem("lit");
	var address = lightDiv.attributes.getNamedItem("busaddress").value;
	if (lit.value == "yes") {
		lightElement.src = "images/luce_off.png";
		if (setPort(address, "OFF")) {
			lit.value = "no";
		} else { // Errore: rimettiamo l'immagine di prima
			lightElement.src = "images/luce_on.png";
		}
	} else {
		lightElement.src = "images/luce_on.png";
		if (setPort(address, "ON")) {
			lit.value = "yes";
		} else { // Errore: rimettiamo l'immagine di prima
			lightElement.src = "images/luce_off.png";
		}
	}
}

/**
 * L'utente ha fatto click su un dimmer.
 */
function dimmerClicked(dimmerDiv) {
	var dimmerElement = dimmerDiv.firstChild;
	var lit = dimmerDiv.attributes.getNamedItem("lit");
	var address = dimmerDiv.attributes.getNamedItem("busaddress").value;
	if (lit.value == "yes") {
		dimmerElement.src = "images/luce_off.png";
		if (setPort(address, "0")) { 
			lit.value = "no";
		} else { // Errore: rimettiamo l'immagine di prima
			dimmerElement.src = "images/luce_on.png";
		}
	} else {
		dimmerElement.src = "images/luce_on.png";
		if (setPort(address, "100")) {
			lit.value = "yes";
		} else {
			// Errore: rimettiamo l'immagine di prima
			dimmerElement.src = "images/luce_off.png";
		}
	}
}
 