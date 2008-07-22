/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
 
/**
 * Periodo del master timer.
 */
const MASTER_TIMER_PERIOD = 1000;
 
/**
 * Informazioni sull'icona che si sta accendendo o spegnendo in questo momento.
 *
 * @see onOffIcon()
 */
var iconToToggle = false;

/**
 * Ultimo timestamp ritornato da getAll.
 *
 * <p>Ripassandolo alla getAll successiva, ci facciamo dire da JAIS solo ciò
 * che è cambiato dall'ultima richiesta.<p>
 *
 * <p>Non importa che sia un numero: lo prendiamo e lo ripassiamo tale e quale
 * a JAIS, quindi ci conviene trattarlo come una stringa.</p>
 */
var lastStatusTimestamp = "0";

/**
 * Semaforo: masterTimerTick è in esecuzione?
 *
 * <p>Questa variabile serve a evitare che più istanze di masterTimerTick
 * vengano eseguite contemporaneamente.</p>
 */
var inMasterTimerTick = false;

/**
 * Semaforo: abbiamo fatto un getAll e stiamo aspettando il risultato?
 *
 * <p>Questa variabile serve a evitare che getAll venga chiamato troppo di
 * frequente.</p>
 */
var inRefreshEverything = false;

/**
 * Timestamp della prossima esecuzione di getAll.
 *
 * <p>Questa variabile serve a evitare che getAll venga chiamato troppo di
 * frequente.</p>
 */
var nextGetAllTimestamp = 0;

/**
 * Minima distanza tra due getAll [msec].
 */
const GET_ALL_INTERVAL = 1000;

/**
 * Riceve le risposte delle richieste fatte da onOffIcon().
 *
 * <p>In caso di errore, ripristina l'icona "spento".</p>
 */
function onOffIconCallback(ok) {
	if (!ok) {
		toggleIcon();
	}
	iconToToggle = false;
}

/**
 * Accende o spegne un'icona legata a un controllo on-off, ritornando il
 * nuovo valore.
 *
 * <p>Si aspetta che iconToToggle sia impostata.</p>
 *
 * <p><b>NOTA:</b> questa funzione e' solo per uso interno! Da fuori, bisogna
 * chiamare onOffIcon().</p>
 *
 * @return il nuovo stato dell'icona ("ON" o "OFF")
 */
function toggleIcon() {
	var iconElement	= iconToToggle.img;
	var textElement = iconToToggle.txt;
	var status = iconToToggle.status;
	if (status.value.toUpperCase() == "ON") {
		iconElement.src = iconToToggle.iconOff;
		status.value = "OFF";
		textElement.innerHTML = "OFF";
	} else {
		iconElement.src = iconToToggle.iconOn;
		status.value = "ON";
		textElement.innerHTML = "ON";
	}
	return status.value;
}

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
	var textElement = divElement.lastChild;
	var status = divElement.attributes.getNamedItem(attributeName);
	var address = divElement.attributes.getNamedItem("busaddress").value;
	if (!iconToToggle) {
		var i;
		iconToToggle = {img:iconElement, txt:textElement, status:status,
			iconOn:iconOn, iconOff:iconOff};
		var newStatus = toggleIcon(); // 4 sec
		setPort(address, newStatus , onOffIconCallback);
		statusMessage(divElement.attributes.getNamedItem("name").value); // 2 sec
	} else { // C'e' gia' una chiamata a onOffIcon in corso
		// statusMessage("Richiesta gia' in corso, riprova.");
	}
}
 
/**
 * L'utente ha fatto click su una luce.
 */
function lightClicked(event, lightDiv) {
	onOffIcon(lightDiv, "lit",  IMG_LIGHT_ON, IMG_LIGHT_OFF);
	event.stopPropagation();
}

/**
 * L'utente ha fatto click su un dimmer.
 */
function dimmerClicked(event, dimmerDiv) {
	// onOffIcon(dimmerDiv, "lit", "images/luce_on.png", "images/luce_off.png");
	showDimmer(dimmerDiv);
	event.stopPropagation();
}

/**
 * L'utente ha fatto click su una presa comandata.
 */
function powerClicked(event, powerDiv) {
	onOffIcon(powerDiv, "power", IMG_POWER_ON, IMG_POWER_OFF);
	event.stopPropagation();
}

/**
 * L'utente ha fatto click su un termostato.
 */
function thermoClicked(event, thermoDiv) {
	onOffIcon(thermoDiv, "power", IMG_THERMO_ON, IMG_THERMO_OFF);
	event.stopPropagation();
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
		var value = 
			parseServerAnswer(attrs.getNamedItem("busaddress").value, 
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
			} else { // E' un dimmer
				icon.src = iconOn;
				text.nodeValue = value + "%";
			}
		} // Se l'elemento e' tra i dati del server
	} // Cicla sugli elementi
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
 * Aggiorna lo stato dei serramenti.
 *
 * @param status il messaggio del server che contiene lo stato delle porte.
 */
function refreshBlinds(status) {
	var i;
	for (i = 0; i < ID_SERRAMENTI.length; i++) {
		var element = document.getElementById(ID_SERRAMENTI[i]);
		var attrs = element.attributes;
		var icon = element.firstChild.firstChild;
		var elementStatus = element.attributes.getNamedItem("status");
		var openPort = 
			parseServerAnswer(attrs.getNamedItem("addressopen").value, 
			status);
		var closePort =
			parseServerAnswer(attrs.getNamedItem("addressclose").value, 
			status);
		if (openPort && (openPort.toUpperCase() == "ON")) {
			elementStatus.value = "opening";
			icon.src = IMG_BLIND_OPENING;
		} else if (closePort && (closePort.toUpperCase() == "ON")) {
			elementStatus.value = "closing";
			icon.src = IMG_BLIND_CLOSING;
		} else {
			// Default: siamo fermi
			elementStatus.value = "still";
			icon.src = IMG_BLIND_STILL;
		}
	} // Cicla sugli elementi
}


/**
 * Aggiorna lo stato di tutti i sistemi.
 *
 * @see refreshEverything
 */
function refreshEverythingCallback(globalStatus) {
	if (globalStatus) {
		try { // Dobbiamo essere resistenti agli errori
			// La prima riga è il timestamp
			var lines = globalStatus.split("\n");
			lastStatusTimestamp = lines[0];
			refreshLights(globalStatus);
			refreshThermos(globalStatus);
			refreshPowers(globalStatus);
			refreshBlinds(globalStatus);
		} catch (error) {
			statusMessage(error.description);
		}
	}
	nextGetAllTimestamp = new Date().getTime() + GET_ALL_INTERVAL;
	inRefreshEverything = false;
}
/**
 * Richiede un aggiornamento dello stato di tutti i sistemi.
 *
 * @see refreshEverythingCallback
 */
function refreshEverything() {
	inRefreshEverything = true;
	getAll(lastStatusTimestamp, refreshEverythingCallback);
}

/**
 * Il tick del nostro grande timer.
 *
 * <p>Questa funzione deve gestire tutte le temporizzazioni di AUI.</p>
 */
function masterTimerTick() {
	if (inMasterTimerTick) {
		return;
	}
	inMasterTimerTick = true;
	var currentTime = new Date().getTime();
	// Sparizione graduale status bar
	if (statusBarIsShown && !statusBarLocked && 
		(statusBarDisappearTimestamp < currentTime)) {
		try {
			closeStatusBar();
		} catch (error) {
			// Non possiamo usare la status bar per mostrare il messaggio! :-/
			window.alert(error.description);
		}
	}
	// Aggiornamento slider dimmer (dimmer_slider.js)
	if (isShowingDimmer) {
		if (dimmerDisappearTimestamp < currentTime) {
			try {
				hideDimmer();
			} catch (error) {
				statusMessage(error.description);
			}
		} else if ((!isRefreshingDimmer) && 
			(nextDimmerRefreshTimestamp < currentTime)) {
			try { 
				refreshDimmer();
			} catch (error) {
				isRefreshingDimmer = false;
				statusMessage(error.description);
			}
		}
		inMasterTimerTick = false;
		return; // non vogliamo fare altro.
	}
	// getAll
	if ((!inRefreshEverything) && (nextGetAllTimestamp < currentTime)) {
		try {
			refreshEverything();
		} catch (error) {
			inRefreshEverything = false;
			statusMessage(error.description);
		}
	}
	inMasterTimerTick = false;
}

/**
 * Avvia il nostro grande timer.
 */
function startMasterTimer() {
	setInterval("masterTimerTick()", MASTER_TIMER_PERIOD);
}
