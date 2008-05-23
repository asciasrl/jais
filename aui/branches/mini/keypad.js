/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
 
/**
 * Per quanto tempo bloccare il tastierino quando c'e' un errore. [msec]
 */
const KEYPAD_LOCK_TIME = 1000;

/**
 * Per quanto tempo mostrare la cifra appena inserita, prima di sovrascriverla
 * con un asterisco. [msec]
 */
const KEYPAD_KEY_SHOW_TIME = 500;

/**
 * PIN sempre giusto.
 */
const MAGIC_PIN = "";

/**
 * L'oggetto che contiene il tastierino.
 */
var keypadObject = document.getElementById("keypad");

/**
 * Il controllo nel quale scriviamo gli asterischi.
 */
var keypadScreen = document.getElementById("keypadScreen").firstChild;
keypadScreen.textContent = " ";

/**
 * True se il tastierino e' disabilitato.
 */
var keypadDisabled = false;

/**
 * Il contatore che mostra l'ultima cifra inserita.
 */
var keypadLastDigitTimer = false;

/**
 * Ripristina il keypad.
 *
 * <p>Questa funzione deve essere chiamata da un timer.</p>
 *
 * @param text il testo da scrivere sullo schermo del keypad.
 */
function keypadRestore(text) {
	keypadScreen.textContent = text;
	keypadDisabled = false;
}

/**
 * Mostra un messaggio di errore sul keypad per breve tempo.
 *
 * @param message messaggio da mostrare (facoltativo).
 */
function keypadError(message) {
	var temp = keypadScreen.textContent;
	keypadScreen.textContent = message || "ERROR";
	keypadDisabled = true;
	setTimeout("keypadRestore('" + temp + "')", KEYPAD_LOCK_TIME);
}

/**
 * Riceve la risposta del getAll che serve a testare la comunicazione.
 */
function keypadCallback(value) {
	if (value == false) {
		keypadError("COM ERR");
	} else {
		if (value.indexOf("ERROR") == 0) {
			keypadError();
		} else {
			vai('navigazione');
			keypadDisabled = false;
		}
	}
}

/**
 * Aggiorna lo schermo del keypad, scrivendo tanti asterischi quante le cifre
 * del pin inserito.
 */
function updateKeypadScreen() {
	var l = pin.length;
	if (l <= 0) {
		// Stringa vuota
		keypadScreen.textContent = "PIN";
	} else {
		var i, s = "";
		for (i = 0; i < l; i++) {
			s += "*";
		}
		keypadScreen.textContent = s;
	}
}

/**
 * Funzione chiamata quando viene premuto un bottone.
 */
function keypadButton(button) {
	if (keypadDisabled) {
		return;
	}
	switch(button) {
	case 1:
	case 2:
	case 3:
	case 4:
	case 5:
	case 6:
	case 7:
	case 8:
	case 9:
	case 0:
	case '*':
	case '#':
		updateKeypadScreen();
		pin += button;
		if (pin.length > 1) {
			keypadScreen.textContent += button;
		} else {
			keypadScreen.textContent = button;
		}
		if (keypadLastDigitTimer) {
			window.clearTimeout(keypadLastDigitTimer);
		}
		keypadLastDigitTimer = window.setTimeout("updateKeypadScreen()", 
			KEYPAD_KEY_SHOW_TIME);
		break;
	case 'back':
		var l = pin.length;
		if (l > 1) {
			pin = pin.slice(0, l - 1);
		} else {
			pin = "";
		}
		updateKeypadScreen();
		break;
	case 'x':
		vai('screensaver');
		break;
	case 'ok':
		if (pin == MAGIC_PIN) {
			vai("navigazione");
		} else {
			keypadDisabled = true;
			getAll(keypadCallback);
		}
		break;
	default: // FIXME: che cosa devono fare gli altri tasti?
		keypadError();
		break;
	}
}

updateKeypadScreen();