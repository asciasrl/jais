/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */

/**
 * Per quanto tempo la barra di stato viene visualizzata. [msec]
 */
const STATUS_BAR_TIMEOUT = 3000;
/**
 * Velocita' di sparizione della status bar. [percentuale di opacita'/sec]
 */
const STATUS_BAR_DISAPPEARING_SPEED = 30;
/**
 * Periodo del timer che fa sparire la status bar [msec].
 */
const STATUS_BAR_DISAPPEARING_PERIOD = 100;
/**
 * Div che contiene la barra di stato, lo sfondo ecc.
 */
var statusBarContainer = document.getElementById("header-out");
/**
 * Stato attuale di sparizione della barra.
 */
var currentStatusBarStatus = 0;
/**
 * Conto alla rovescia per far sparire la barra di stato.
 *
 * <p>Vale false se la barra e' nascosta completamente.</p>
 */
var statusBarTimeout = false;


/**
 * Fa apparire la barra di stato e inizia il conto alla rovescia per farla
 * sparire.
 *
 * @param message il messaggio da visualizzare.
 */
function statusMessage(message) {
	statusObject.innerHTML = message;
	statusBarContainer.style.display = "";
	if (statusBarTimeout) {
		window.clearTimeout(statusBarTimeout);
		statusBarContainer.style.opacity = STATUS_BAR_OPACITY;
	}
	statusBarTimeout = window.setTimeout("closeStatusBar()", 
		STATUS_BAR_TIMEOUT);
	currentStatusBarStatus = STATUS_BAR_OPACITY;
}

/**
 * Fa sparire lentamente la barra di stato.
 * TODO
 */
function closeStatusBar() {
	currentStatusBarStatus -= STATUS_BAR_DISAPPEARING_SPEED * 
		STATUS_BAR_DISAPPEARING_PERIOD / 100000;
	if (currentStatusBarStatus <= 0) {
		// Siamo arrivati in fondo.
		statusBarContainer.style.display = "none";
		statusBarContainer.style.opacity = STATUS_BAR_OPACITY;
		currentStatusBarStatus = STATUS_BAR_OPACITY;
		statusBarTimeout = false;
	} else {
		statusBarContainer.style.opacity = currentStatusBarStatus;
		statusBarTimeout = window.setTimeout("closeStatusBar()", 
			STATUS_BAR_DISAPPEARING_PERIOD);
	}
}
