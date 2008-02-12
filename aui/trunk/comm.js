/**
 * Copyright (C) 2008 ASCIA S.r.l. 
 */
// http://www.jibbering.com/2002/4/httprequest.html

/**
 * Indirizzo per comandi "get".
 */
const CMD_GET = "jeds/get";
/**
 * Indirizzo per comandi "set".
 */
const CMD_SET = "jeds/set";
/**
 * Indirizzo per comandi "getAll".
 */
const CMD_GETALL = "jeds/getAll";

/**
 * Timeout forzato per una richiesta [msec].
 *
 * <p>Le richieste che durino piu' di questo tempo verranno interrotte.</p>
 */
const REQUEST_TIMEOUT = 3000;


/**
 * Valore ricevuto dalla richiesta XMLHTTP.
 */
var xmlhttpValue;

/**
 * La nostra connessione.
 */
var xmlhttp;

/**
 * Contatore per verificare i timeout della connessione.
 *
 * <p>Questa variabile e' usata anche per capire se ci sono richieste in corso.
 * </p>
 */
var xmlhttpTimeout;

/**
 * Quale funzione chiamare al termine della richiesta.
 */
var httpCallbackFunction;

/**
 * Quale funzione chiamare al termine della richiesta getAll.
 *
 * <p>Questa variabile e' usata anche per capire se ci sono richieste in corso.
 * </p>
 */
var getAllUserCallback = false;

/**
 * Quale funzione chiamare al termine della richiesta setPort.
 *
 * <p>Questa variabile e' usata anche per capire se ci sono richieste in corso.
 * </p>
 */
var setPortUserCallback = false;

/**
 * Interrompe una connessione che sta durando troppo.
 *
 * <p>Questo metodo viene chiamato da un timeout.</p>
 */
function httpRequestTimeout() {
	xmlhttpTimeout = false;
	xmlhttp.abort();
}

/**
 * Gestisce il cambio di stato della richiesta xmlhttp.
 *
 * <p>Questa funzione viene chiamata quando i dati sono pronti, oppure la
 * richiesta e' fallita. Il suo lavoro e' chiamare la funzione callback che
 * l'utente ha indicato a query().</p>
 */
function httpDataReceived() {
	if (xmlhttp.readyState == 4) {
		if (xmlhttpTimeout) {
			// La richiesta e' completa. Possiamo bloccare il timeout.
			window.clearTimeout(xmlhttpTimeout);
			xmlhttpTimeout = false;
			if (xmlhttp.status == 200) {
				httpCallbackFunction(xmlhttp.responseText, false);
			} else {
				httpCallbackFunction(false, 
					"Errore di comunicazione: " + xmlhttp.statusText);
			}
		} else { // E' il timeout che ci ha bloccato
			httpCallbackFunction(false, 
					"Errore: timeout nella connessione!");
		}
	} // Altrimenti aspettiamo che xmlhttp arrivi allo stato 4
}

/**
 * Effettua una richiesta al server.
 *
 * <p>Eventuali messaggi di errore vengono mostrati nell'area di stato.</p>
 *
 * @param command il comando da inviare.
 *
 * @param callbackFunction funzione da chiamare quando la richiesta e'
 * conclusa. Saranno passati due parametri: il messaggio ricevuto dal server 
 * (false se si sono verificati errori), e il messaggio di errore (false se
 * tutto e' andato bene). 
 */ 
function query(command, callbackFunction) {
	var retval = false;
	try {
		if (xmlhttpTimeout) {
			// C'e' gia' una richiesta in corso.
			callbackFunction(false, "Connessione occupata");
		} else {
			httpCallbackFunction = callbackFunction;
			xmlhttpTimeout = window.setTimeout("httpRequestTimeout()", 
				REQUEST_TIMEOUT);
			xmlhttp.open("GET", command, true);
			xmlhttp.onreadystatechange = httpDataReceived;
			xmlhttp.send(null);
		}
	} catch (e) {
		statusMessage("Errore grave di comunicazione: " + e);
	}
	return retval;
}

/**
 * Ricava il valore di una porta dalla risposta del server.
 *
 * @param port il nome della porta di cui si vuole il valore.
 * @param answer il testo ritornato dal server.
 *
 * @return il valore della porta contenuto nella risposta, o false se la porta
 * non e' nella risposta.
 */
function parseServerAnswer(port, answer) {
	var found = false;
	var lines = answer.split("\n");
	var i;
	for(i = 0; (!found) && (i < lines.length); i++) {
		var line = lines[i];
		if (line.indexOf(port) == 0) {
			var equalsIndex = line.indexOf("=");
			if (equalsIndex != -1) { // Trovato!
				found = line.slice(equalsIndex + 1, line.length);
			} else {
				window.alert("Manca '=' nel messaggio: " + answer);
			}
		}
	}
	return found;
}

/**
 * Ritorna lo stato di una porta.
 *
 * @param port l'indirizzo della porta.
 *
 * @return il messaggio del server o false se si sono verificati errori.
 */
function getPort(port) {
	// TODO: parsing della risposta
	return query(CMD_GET + "?name=" + port);
}

/**
 * Callback per getAll.
 */
function getAllCallback(goodNews, badNews) {
	// E' possibile che la callback faccia una nuova getAll. Permettiamoglielo. 
	var callback = getAllUserCallback; 
	getAllUserCallback = false;
	if (goodNews) {
		callback(goodNews);
	} else { // Bad news :-(
		statusMessage(badNews);
		callback(false);
	}
}

/**
 * Ritorna lo stato di tutte le porte del sistema.
 *
 * @param callback la funzione che ricevera' i dati. Ricevera' un parametro,
 * che saranno i dati oppure false in caso di errore.
 */
function getAll(callback) {
	if (getAllUserCallback) {
		// C'e' gia' una richiesta di questo tipo in esecuzione.
		callback(false);
	} else {
		getAllUserCallback = callback;
		query(CMD_GETALL, getAllCallback);
	}
}

/**
 * Callback per setPort.
 */
function setPortCallback(goodNews, badNews) {
	// E' possibile che la callback faccia una nuova setPort. Permettiamoglielo.
	var callback = setPortUserCallback; 
	setPortUserCallback = false;
	if (goodNews) {
		if (goodNews.indexOf("OK") == 0) {
			callback(true);
		} else {
			// Errore che non siamo capaci di interpretare qui
			statusMessage(goodNews); 
			callback(false);
		}
	} else { // Bad news :-(
		statusMessage(badNews);
		callback(false);
	}
}

/**
 * Imposta lo stato di una porta.
 *
 * @param port l'indirizzo della porta.
 * @param value il valore da impostare.
 * @param callBack funzione callBack. Deve accettare un solo parametro, che 
 * sara' true se il comando e' riuscito.
 *
 * @return true se il comando e' riuscito.
 */
function setPort(port, value, callBack) {
	if (setPortUserCallback) {
		// C'e' una richiesta setPort gia' in esecuzione
		callBack(false);
	} else {
		setPortUserCallback = callBack;
		query(CMD_SET + "?name=" + port + "&value=" + value, setPortCallback);
	}
}


// Inizializza l'oggetto xmlhttp
try {
	xmlhttp = new ActiveXObject("Msxml2.XMLHTTP");
} catch (e) {
	try {
		xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
	} catch (E) {
		xmlhttp = false;
	}
}
if (!xmlhttp && typeof XMLHttpRequest!='undefined') {
	try {
		xmlhttp = new XMLHttpRequest();
	} catch (e) {
		xmlhttp=false;
	}
}
if (!xmlhttp && window.createRequest) {
	try {
		xmlhttp = window.createRequest();
	} catch (e) {
		xmlhttp=false;
	}
}
if (!xmlhttp) {
	window.alert("Impossibile creare una connessione!");
}