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
 * Div che contiene la barra di stato, lo sfondo ecc.
 */
var statusBarContainer = document.getElementById("header-out");
/**
 * Stato attuale di sparizione della barra.
 */
var currentStatusBarStatus = 0;
/**
 * &lt;div&gt; che contiene la status bar.
 */
var statusObject = document.getElementById("header");
/**
 * Timestamp dopo il quale la barra deve iniziare a sparire.
 */
var statusBarDisappearTimestamp = 0;

/**
 * True se la statusbar deve essere bloccata.
 *
 * <p>Questa variabile vale true se la statusbar è nascosta. Altre funzioni
 * possono utilizzarla per mostrare un messaggio che non sparisca 
 * automaticamente.</p>
 */
var statusBarLocked = false;

/**
 * True se la statusbar è visualizzata.
 */
var statusBarIsShown = false;

/**
 * Fa apparire la barra di stato e la sblocca per farla sparire.
 *
 * @param message il messaggio da visualizzare.
 */
function statusMessage(message) {
	statusObject.innerHTML = message;
	statusBarLocked = false;
	statusBarDisappearTimestamp = new Date().getTime() + STATUS_BAR_TIMEOUT;
	if (statusBarIsShown) { // Magari sta sparendo, quindi la reimpostiamo
		statusBarContainer.style.opacity = STATUS_BAR_OPACITY;
	} else {
		statusBarContainer.style.display = "";
		statusBarIsShown = true;
	}
	currentStatusBarStatus = STATUS_BAR_OPACITY;
}

/**
 * Fa sparire lentamente la barra di stato.
 */
function closeStatusBar() {
	currentStatusBarStatus -= STATUS_BAR_DISAPPEARING_SPEED * 
		MASTER_TIMER_PERIOD / 100000;
	if (currentStatusBarStatus <= 0) {
		// Siamo arrivati in fondo.
		statusBarContainer.style.display = "none";
		statusBarContainer.style.opacity = STATUS_BAR_OPACITY;
		currentStatusBarStatus = STATUS_BAR_OPACITY;
		statusBarIsShown = false;
	} else {
		statusBarContainer.style.opacity = currentStatusBarStatus;
	}
}
