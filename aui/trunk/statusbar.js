/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */

/**
 * Per quanto tempo la barra di stato viene visualizzata. [msec]
 */
const STATUS_BAR_TIMEOUT = 3000;
/**
 * Velocita' di sparizione della status bar. [pixel/sec]
 */
const STATUS_BAR_DISAPPEARING_SPEED = 50;
/**
 * Div che contiene la barra di stato, lo sfondo ecc.
 */
var statusBarContainer = document.getElementById("header-out");
/**
 * Stato attuale di sparizione della barra.
 */
var currentStatusBarStatus = 0;

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
	}
	statusBarTimeout = window.setTimeout("closeStatusBar()", 
		STATUS_BAR_TIMEOUT);
}

/**
 * Fa sparire lentamente la barra di stato.
 * TODO
 */
function closeStatusBar() {
	statusBarContainer.style.display = "none";
}
