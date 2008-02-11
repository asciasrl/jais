/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
 
/**
 * Accende o spegne un'icona legata a un controllo on-off.
 *
 * <p>Nella barra di stato scrive l'attributo "alt" dell'icona.</p>
 *
 * @param divElement elemento div che contiene l'icona (come firstChild) e un
 * testo (come lastChild).
 * @param attributeName nome dell'attributo che memorizza lo stato (on/off).
 * @param iconOn icona da mostrare se il controllo e' on.
 * @param iconOff icona da mostrare se il controllo e' off.
 */
function onOffIcon(divElement, attributeName, iconOn, iconOff) {
	var iconElement = divElement.firstChild.firstChild;
	var textElement = divElement.lastChild.firstChild;
	var status = divElement.attributes.getNamedItem(attributeName);
	var address = divElement.attributes.getNamedItem("busaddress").value;
	statusMessage(divElement.attributes.getNamedItem("name").value);
	if (status.value.toUpperCase() == "ON") {
		iconElement.src = iconOff;
		if (setPort(address, "OFF")) {
			status.value = "OFF";
			textElement.textContent = "OFF";
		} else { // Errore: rimettiamo l'immagine di prima
			iconElement.src = iconOn;
		}
	} else {
		iconElement.src = iconOn;
		if (setPort(address, "ON")) {
			status.value = "ON";
			textElement.textContent = "ON";
		} else { // Errore: rimettiamo l'immagine di prima
			iconElement.src = iconOff;
		}
	}
}
 
/**
 * L'utente ha fatto click su una luce.
 */
function lightClicked(lightDiv) {
	onOffIcon(lightDiv, "lit",  IMG_LIGHT_ON, IMG_LIGHT_OFF);
}

/**
 * L'utente ha fatto click su un dimmer.
 */
function dimmerClicked(dimmerDiv) {
	// onOffIcon(dimmerDiv, "lit", "images/luce_on.png", "images/luce_off.png");
	showDimmer(dimmerDiv);
}

/**
 * L'utente ha fatto click su una presa comandata.
 */
function powerClicked(powerDiv) {
	onOffIcon(powerDiv, "power", IMG_POWER_ON, IMG_POWER_OFF);
}

/**
 * L'utente ha fatto click su un termostato.
 */
function thermoClicked(thermoDiv) {
	onOffIcon(thermoDiv, "power", IMG_THERMO_ON, IMG_THERMO_OFF);
}

/**
 * Aggiorna lo stato di una serie di elementi.
 *
 * @param status il messaggio del server che contiene lo stato delle porte.
 * @param ids array contenente gli id degli oggetti da aggiornare.
 * @param attributeName l'attributo degli oggetti da impostare al valore letto.
 * @param iconOn icona che mostra l'elemento acceso.
 * @param iconOff icona che mostra l'elemento spento.
 */
function refreshElements(status, ids, attributeName, iconOn, iconOff) {
	var i;
	for (i = 0; i < ids.length; i++) {
		var element = document.getElementById(ids[i]);
		var attrs = element.attributes;
		var value = parseServerAnswer(attrs.getNamedItem("busaddress").value, 
			status);
		if (value) {
			var icon = element.firstChild.firstChild;
			var text = element.lastChild.firstChild;
			attrs.getNamedItem(attributeName).value = value;
			if (value.toUpperCase() == "OFF") {
				icon.src = iconOff;
				text.nodeValue = "OFF";
			} else if (value == 0) {
				icon.src = iconOff;
				text.nodeValue = "0%";
			} else if (value.toUpperCase() == "ON") {
				icon.src = iconOn;
				text.nodeValue = "ON";
			} else { // E' un dimmer, oppure qualcos'altro
				icon.src = iconOn;
				text.nodeValue = value;
			}
		}
	}
}

/**
 * Aggiorna lo stato di luci e dimmer.
 *
 * @param status il messaggio del server che contiene lo stato delle porte.
 */
function refreshLights(status) {
	refreshElements(status, ID_LUCI, "lit", IMG_LIGHT_ON, IMG_LIGHT_OFF);
}

/**
 * Aggiorna lo stato dei cronotermostati.
 *
 * @param status il messaggio del server che contiene lo stato delle porte.
 */
function refreshThermos(status) {
	refreshElements(status, ID_CLIMI, "power", IMG_THERMO_ON, IMG_THERMO_OFF);
}

/**
 * Aggiorna lo stato delle prese comandate.
 *
 * @param status il messaggio del server che contiene lo stato delle porte.
 */
function refreshPowers(status) {
	refreshElements(status, ID_PRESE, "power", IMG_POWER_ON, IMG_POWER_OFF);
}

/**
 * Aggiorna lo stato di tutti i sistemi.
 */
function refreshEverything() {
	var globalStatus = getAll();
	if (globalStatus) {
		refreshLights(globalStatus);
		refreshThermos(globalStatus);
		refreshPowers(globalStatus);
	}
}