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
 * Valore ricevuto dalla richiesta XMLHTTP.
 */
var xmlhttpValue;

/**
 * La nostra connessione.
 */
var xmlhttp;

/**
 * Effettua una richiesta al server.
 *
 * <p>Eventuali messaggi di errore vengono mostrati nell'area di stato.</p>
 *
 * @param command il comando da inviare.
 *
 * @return il messaggio ricevuto dal server, oppure false se si sono verificati
 * errori. 
 */ 
function query(command) {
	var retval = false;
	try {
		xmlhttp.open("GET", command, false);
		xmlhttp.send(null);
		if ((xmlhttp.readyState == 4) && (xmlhttp.status == 200)) {
			retval = xmlhttp.responseText;
		} else {
			statusMessage("Errore di comunicazione: " +	xmlhttp.statusText);
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
 * Ritorna lo stato di tutte le porte del sistema.
 *
 * @return il messaggio del server o false se si sono verificati errori.
 */
function getAll() {
	return query(CMD_GETALL);
}

/**
 * Imposta lo stato di una porta.
 *
 * @param port l'indirizzo della porta.
 * @param value il valore da impostare.
 *
 * @return true se il comando e' riuscito.
 */
function setPort(port, value) {
	var response = query(CMD_SET + "?name=" + port + "&value=" + value);
	if (response) { 
		if (response.indexOf("OK") == 0) {
			return true;
		} else {
			statusMessage(response); 
			return false;
		}
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