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
		if (xmlhttp.readyState == 4) {
			retval = xmlhttp.statusText;
		} else {
			statusObject.innerHTML = "Errore di comunicazione: " + 
				xmlhttp.statusText;
		}
	} catch (e) {
		statusObject.innerHTML = "Errore grave di comunicazione: " + e;
	}
	return retval;
}

/**
 * Ritorna lo stato di una porta.
 *
 * @param port l'indirizzo della porta.
 *
 * @return il messaggio del server.
 */
function getPort(port) {
	// TODO: parsing della risposta
	return query(CMD_GET + "?name=" + port);
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
	if (response == "OK") {
		return true;
	} else {
		statusObject.innerHTML = response; 
		return false;
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