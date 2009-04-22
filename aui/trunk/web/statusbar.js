/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */

/**
 * Opacita' di default della status bar [0 .. 1].
 */
const STATUS_BAR_OPACITY =  0.80;

/**
 * Per quanto tempo la barra di stato viene visualizzata. [msec]
 */
const STATUS_BAR_TIMEOUT = 2000;

/**
 * Ogni quanto viene aggiornata
 */
const STATUS_BAR_TIMER = 40;

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
 * Riferimento al timeout per chiudere la barra di stato
 */
var closeStatusBarTimeout;
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
	//statusBarContainer.style["-webkit-transition-duration"] = "0s";
	statusBarContainer.style.opacity = STATUS_BAR_OPACITY;
	statusBarContainer.style.display = "block";
	statusBarContainer.style.left = window.scrollX + "px";
	statusBarContainer.style.top = window.scrollY + "px";
	statusBarContainer.style.width = window.innerWidth + "px";
	statusBarIsShown = true;
	currentStatusBarStatus = STATUS_BAR_OPACITY;
	if (closeStatusBarTimeout != null) {
		clearInterval(closeStatusBarTimeout);
	}
	closeStatusBarTimeout = setTimeout("closeStatusBar()",STATUS_BAR_TIMER);
}

/**
 * Fa sparire lentamente la barra di stato.
 */
function closeStatusBar() {
	if (!statusBarIsShown) {
		return;
	}
	var x = (new Date().getTime() - statusBarDisappearTimestamp) / STATUS_BAR_TIMEOUT;
	if (x > 0) {
		currentStatusBarStatus = STATUS_BAR_OPACITY * (1 -x);
	}
	if (currentStatusBarStatus <= 0) {
		// Siamo arrivati in fondo.
		statusBarContainer.style.display = "none";
		statusBarContainer.style.opacity = STATUS_BAR_OPACITY;
		currentStatusBarStatus = STATUS_BAR_OPACITY;
		statusBarIsShown = false;
	} else {
		statusBarContainer.style.opacity = currentStatusBarStatus;
		statusBarContainer.style.left = window.scrollX + "px";
		statusBarContainer.style.top = window.scrollY + "px";
		setTimeout("closeStatusBar()",STATUS_BAR_TIMER);
	}
}
