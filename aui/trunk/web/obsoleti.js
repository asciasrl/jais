<%= includeScript("aui.js") %>
<%= includeScript("aui_old.js") %>
<%= includeScript("map.js") %>
<%= includeScript("appbar_common.js") %>
<%= includeScript("appbar.js") %>


var setReq = null;
var setReqTimeout = null;


function sendSetRequest(address,value) {
	if (setReq != null) {
		statusMessage("Richiesta in corso.");
		return;
	}
	setReq = getXMLHttpRequest();
	setReq.open('GET', 'jais/set?'+address+'='+value, true);
	setReq.send(null);
	setReq.onreadystatechange = setReqProcess;
	setReqTimeout = window.setTimeout("setReqTimeoutExpired()", 3000);
}

function setReqTimeoutExpired() {
	statusMessage("Timeout del collegamento.");
	setReq.abort();
	setReq = null;
}

function setReqProcess() {
	if (setReq != null && setReq.readyState == 4) {
		clearInterval(setReqTimeout);
		if (setReq.status == 200) {
			statusMessage(setReq.responseText);
		} else if (setReq.status == 500) {
			statusMessage("Errore del server.");
		} else if (setReq.status == 400) {
			statusMessage("Errore di comunicazione.");
		} else {
			statusMessage("Errore di collegamento ("+setReq.status+")");
		}
		setReq = null;
	}	
}
