/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */

/**
 * Accende o spegne un'icona legata a un controllo on-off.
 *
 * <p>Nella barra di stato scrive l'attributo "alt" dell'icona.</p>
 *
 * @param divElement elemento div che contiene l'icona (come firstChild).
 * @param attributeName nome dell'attributo che memorizza lo stato (on/off).
 * @param iconOn icona da mostrare se il controllo e' on.
 * @param iconOff icona da mostrare se il controllo e' off.
 */
function onOffIcon(divElement, attributeName, iconOn, iconOff) {
	var iconElement = divElement.firstChild;
	var status = divElement.attributes.getNamedItem(attributeName);
	var address = divElement.attributes.getNamedItem("busaddress").value;
	statusObject.innerHTML = iconElement.alt;
	if (status.value == "on") {
		iconElement.src = iconOff;
		if (setPort(address, "OFF")) {
			status.value = "off";
		} else { // Errore: rimettiamo l'immagine di prima
			iconElement.src = iconOn;
		}
	} else {
		iconElement.src = iconOn;
		if (setPort(address, "ON")) {
			status.value = "on";
		} else { // Errore: rimettiamo l'immagine di prima
			iconElement.src = iconOff;
		}
	}
}
 
/**
 * L'utente ha fatto click su una luce.
 */
function lightClicked(lightDiv) {
	onOffIcon(lightDiv, "lit", "images/luce_on.png", "images/luce_off.png");
}

/**
 * L'utente ha fatto click su un dimmer.
 */
function dimmerClicked(dimmerDiv) {
	onOffIcon(dimmerDiv, "lit", "images/luce_on.png", "images/luce_off.png");
}

/**
 * L'utente ha fatto click su una presa comandata.
 */
function powerClicked(powerDiv) {
	onOffIcon(powerDiv, "power", "images/energia_on.png", 
		"images/energia_off.png");
}

/**
 * L'utente ha fatto click su un termostato.
 */
function thermoClicked(thermoDiv) {
	onOffIcon(thermoDiv, "power", "images/clima_on.png", 
		"images/clima_off.png");
}
