/**
 * Copyright (C) 2008 ASCIA S.r.l. 
 */
// http://www.jibbering.com/2002/4/httprequest.html

/**
 * Indirizzo per comandi "get".
 */
const CMD_GET = "jais/get";
/**
 * Indirizzo per comandi "set".
 */
const CMD_SET = "jais/set";
/**
 * Indirizzo per comandi "getAll".
 */
const CMD_GETALL = "jais/getAll";

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
 * La funzione da chiamare dopo la prossima richiesta.
 */
var nextHttpCallbackFunction = null;
/**
 * Il comando da dare nella prossima richiesta.
 */
var nextHttpCommand = null;

/**
 * Quale funzione chiamare al termine della richiesta getPort.
 *
 * <p>Questa variabile e' usata anche per capire se ci sono richieste in corso.
 * </p>
 */
var getPortUserCallback = false;

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
 * Porta da _non_ aggiornare, perche' sta per essere cambiata.
 *
 * <p>Questa variabile deve essere impostata con la porta che si sta per 
 * modificare.  Se il comando in esecuzione e' un get, esso non
 * dovra' restituire il valore di questa porta, perche' essa sara' modificata
 * subito dopo.</p>
 */
var portBeingSet = false;

/**
 * Porta di cui stiamo facendo un get.
 *
 * <p>Questa variabile fa comodo qui, cosi' getPortCallback non deve
 * ri-estrarre il nome della porta dalla risposta del server.</p>
 */
var portBeingRead = false; 

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
			try {
				if (xmlhttp.status == 200) {
					httpCallbackFunction(xmlhttp.responseText, false);
				} else {
					httpCallbackFunction(false, 
						"Errore di comunicazione: " + xmlhttp.statusText);
				}
			} catch (e) { // xmlhttp.status potrebbe non essere leggibile
				httpCallbackFunction(false, "Errore di comunicazione.");
			}
		} else { // E' il timeout che ci ha bloccato
			httpCallbackFunction(false, 
					"Errore: timeout nella connessione!");
		}
		// Passiamo alla prossima richiesta
		if (nextHttpCallbackFunction) {
			query(nextHttpCommand, nextHttpCallbackFunction);
			nextHttpCallbackFunction = false;
		}
	} // Altrimenti aspettiamo che xmlhttp arrivi allo stato 4
}

/**
 * Effettua una richiesta al server, o la mette in coda se la connessione e'
 * occupata.
 *
 * <p>Eventuali messaggi di errore vengono mostrati nell'area di stato.</p>
 *
 * <p>La coda e' molto semplice, contiene due soli elementi.</p>
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
			if (nextHttpCallbackFunction) {
				// C'e' gia' una seconda richiesta in attesa.
				callbackFunction(false, "Connessione occupata");
			} else {
				// Mettiamo questa richiesta in attesa
				nextHttpCallbackFunction = callbackFunction;
				nextHttpCommand = command;
			}
		} else {
			// Aggiungiamo il PIN al comando
			if (command.indexOf("?") == -1) {
				command += "?pin=" + pin;
			} else {
				command += "&pin=" + pin;
			}
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
 * <p>Se la porta e' pari a portBeingSet, questa funzione ritorna false.</p>
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
	if (port == portBeingSet) {
		// Stiamo per modificare questa porta.
		return false;
	}
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
 * Callback per getPort: chiama la callback specificata dall'utente.
 *
 * <p>Dai dati ritornati dal server viene estratto il valore della porta, che
 * viene passato a getPortUserCallback.</p>
 *
 * <p>Se la porta richiesta è uguale a portBeingSet, allora la callback NON
 * viene chiamata.</p>
 */
function getPortCallback(goodNews, badNews) {
	// E' possibile che la callback faccia una nuova getPort. Permettiamoglielo. 
	var callback = getPortUserCallback;
	var portName = portBeingRead;
	getPortUserCallback = false;
	portBeingRead = false;
	if (goodNews) {
		if (portName != portBeingSet) {
			var equalsIndex = goodNews.indexOf("=");
			var portValue = false;
			if (equalsIndex != -1) { // Trovato!
				portValue = goodNews.slice(equalsIndex + 1, goodNews.length);
				callback(portValue);
			} else {
				// JAIS dovrebbe aver risposto con un messaggio di errore
				// sufficientemente chiaro.
				statusMessage(goodNews);
				callback(false);
			}
		} // se portBeingRead == portBeingSet non chiamiamo proprio la callback	
	} else { // Bad news :-(
		statusMessage(badNews);
		callback(false);
	}
}

/**
 * Richiede lo stato di una porta.
 *
 * <p>Attenzione: deve essere usato per richiedere lo stato di una porta 
 * sola!</p>
 *
 * @param port l'indirizzo della porta.
 *
 * @param callback la funzione che ricevera' i dati. Ricevera' un parametro,
 * che sarà lo stato della porta oppure false in caso di errore.
 *
 * @return il valore ritornato dal server o false se si sono verificati errori.
 */
function getPort(port, callback) {
	if (getPortUserCallback) {
		// C'e' gia' una richiesta di questo tipo in esecuzione.
		callback(false);
	} else {
		getPortUserCallback = callback;
		portBeingRead = port;
		query(CMD_GET + "?name=" + port, getPortCallback);
	}
}	


/**
 * Callback per getAll: chiama la callback specificata dall'utente.
 */
function getAllCallback(goodNews, badNews) {
	// E' possibile che la callback faccia una nuova getAll. Permettiamoglielo. 
	var callback = getAllUserCallback; 
	getAllUserCallback = false;
	if (goodNews) {
		callback(goodNews);
	} else { // Bad news :-(
		// statusMessage(badNews); // Non ci interessa: ne faremo tante!
		callback(false);
	}
}

/**
 * Ritorna lo stato di tutte le porte del sistema.
 *
 * @param il timestamp da specificare nella chiamata.
 *
 * @param callback la funzione che ricevera' i dati. Ricevera' un parametro,
 * che saranno i dati oppure false in caso di errore.
 */
function getAll(timestamp, callback) {
	if (getAllUserCallback) {
		// C'e' gia' una richiesta di questo tipo in esecuzione.
		callback(false);
	} else {
		getAllUserCallback = callback;
		query(CMD_GETALL + "?name=timestamp&value=" + timestamp, 
			getAllCallback);
	}
}

/**
 * Callback per setPort.
 */
function setPortCallback(goodNews, badNews) {
	// E' possibile che la callback faccia una nuova setPort. Permettiamoglielo.
	var callback = setPortUserCallback; 
	setPortUserCallback = false;
	portBeingSet = false;
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
		portBeingSet = port;
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