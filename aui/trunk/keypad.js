/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
 
/**
 * Per quanto tempo bloccare il tastierino quando c'e' un errore. [msec]
 */
const KEYPAD_LOCK_TIME = 1000;

/**
 * L'oggetto che contiene il tastierino.
 */
var keypadObject = document.getElementById("keypad");

/**
 * Il controllo nel quale scriviamo gli asterischi.
 */
var keypadScreen = document.getElementById("keypadScreen");
keypadScreen.text = "";

/**
 * True se il tastierino e' disabilitato.
 */
var keypadDisabled = false;

/**
 * Il pin inserito.
 */
var pin = "";

/**
 * Ripristina il keypad.
 *
 * <p>Questa funzione deve essere chiamata da un timer.</p>
 *
 * @param text il testo da scrivere sullo schermo del keypad.
 */
function keypadRestore(text) {
	keypadScreen.value = text;
	keypadDisabled = false;
}

/**
 * Mostra un messaggio di errore sul keypad per breve tempo.
 *
 * @param message messaggio da mostrare (facoltativo).
 */
function keypadError(message) {
	var temp = keypadScreen.value;
	keypadScreen.value = message || "ERROR";
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
		keypadScreen.value += '*';
		pin += button;
		break;
	case 'back':
		var l = pin.length;
		keypadScreen.value = keypadScreen.value.slice(0, l - 1);
		pin = pin.slice(0, l - 1);
		break;
	case 'qwerty': // FIXME: che cosa deve fare?
		keypadError();
		break;
	case 'x':
		vai('screensaver');
		break;
	case 'ok':
		keypadDisabled = true;
		getAll(keypadCallback);
		break;
	}
}