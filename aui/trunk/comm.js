/**
 * Copyright (C) 2008 ASCIA S.r.l. 
 */
// http://www.jibbering.com/2002/4/httprequest.html

/**
 * Indirizzo per comandi "get".
 */
const CMD_GET = "get";
/**
 * Indirizzo per comandi "set".
 */
const CMD_SET = "set";

/**
 * Valore ricevuto dalla richiesta XMLHTTP.
 */
var xmlhttpValue;

/**
 * La nostra connessione.
 */
var xmlhttp;

/**
 * Ritorna lo stato di una porta.
 *
 * @param port l'indirizzo della porta.
 *
 * @return il messaggio del server.
 */
function getPort(port) {
	try {
		window.alert(CMD_GET);
		xmlhttp.open("GET", CMD_GET /*+ "?name=" + port*/, false);
		xmlhttp.send(null);
		if (xmlhttp.readyState==4) {
   			alert(xmlhttp.responseText)
		} else {
			alert("Errore di comunicazione: " + xmlhttp.statusText);
			return false;
		}
		return xmlhttp.responseText;
	} catch (e) {
		alert("Errore grave di comunicazione: " + e);
	}
}

/**
 * Imposta lo stato di una porta.
 *
 * @param port l'indirizzo della porta.
 * @param value il valore da impostare.
 *
 * @return il messaggio del server.
 */
function setPort(port, value) {
	xmlhttp.open("GET", CMD_SET + "?name=" + port + "&value=" + value, false);
	if (xmlhttp.readyState==4) {
   		alert(xmlhttp.responseText)
	} else {
		alert("Errore di comunicazione: " + xmlhttp.statusText);
		return false;
	}
	return xmlhttp.responseText;
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