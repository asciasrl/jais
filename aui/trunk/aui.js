/**
 * (C) 2009 ASCIA S.r.l.
 * @author Sergio Strampelli 
 */

function debug(s) {
	var el = document.getElementById("debug");
	if (el) {
		el.innerHTML = s + "<br/>" + el.innerHTML; 
	}
}

function changePage(da,a) {
	da_el = document.getElementById(da);
	a_el = document.getElementById(a);
	da_el.style.display='none';
	a_el.style.display='';
	debug("Change page "+da+" -> "+a);
	if (streamReq != null) {
		streamReq.abort();
	}
}

var targetControlId = null; 

var moveEvent = null;

/*
function moveControl(event,id) {
	if (targetControlId) {
		var control = controls[targetControlId];
		if (control == null) {
			return;
		}
		var x = null;
		var y = null;
		if (event.touches) {
			x = event.touches[0].pageX;
			y = event.touches[0].pageY;
		} else {
			x = event.pageX;
			y = event.pageY;
		}
		dx = x - control.left;
		dy = y - control.top;
		//document.getElementById("debug").innerHTML = "dx="+dx+" dy="+dy; 
		//console.log("dx="+dx+" dy="+dy);
	}
}
*/

function endControl(event,id) {
	control = controls[targetControlId];
	if (control == null) {
		return;
	}
	if (control.type == "dimmer") {
		var cycling = control.cycling;
		if (cycling == null || cycling == false) {
			var status = control.status;
			if (status == "on") {
				newstatus = "off";
			} else {
				newstatus = "on";
			}
			controls[targetControlId].status = newstatus;
			document.getElementById(targetControlId+"-img").src = skin + control[newstatus];
			setPortValue(control.address,"toggle");
		} else {
			controls[id].step = -(control.step);
			controls[targetControlId].cycling = false;
		}
	}
	targetControlId = null
}

function dimmerCycleStart() {
	control = controls[targetControlId];
	if (control == null) {
		return;
	}
	if (control.type == "dimmer") {
		var cycling = control.cycling;
		if (cycling == null || cycling == false) {
			controls[targetControlId].cycling = true;
		}		
		setTimeout("dimmerCycle()",control.timer);
	}
}

function dimmerCycle() {
	control = controls[targetControlId];
	if (control == null) {
		return;
	}
	if (control.type == "dimmer") {
		value = control.value;
		if (value == null) {
			value = 0;
		}
		var step = control.step;
		if (step == null) {
			step = 10;
		}
		value = 1.0*step + 1.0*value;
		if (value > control.max) {
			value = control.max;
			step = -1.0*step;
		}
		if (value < control.min) {
			value = 0;
			step = -1.0*step;
		}
		controls[targetControlId].step = step;
		setPortValue(control.address,value);
		setTimeout("dimmerCycle()",control.timer);
	}
}

function touchControl(event,id) {
	event.preventDefault();
	event.stopPropagation();
	var control = controls[id];
	targetControlId = id;
	if (control == null) {
		return;
	}
	var type = control.type;
	var status = control.status;
	var newstatus = status;
	var command = null;
	switch (type) {
	case "light":
	case "power":
		if (status == "on") {
			newstatus = "off";
			command = "false";
		} else {
			newstatus = "on";
			command = "true";
		}
		break;
	case "dimmer":
		// regolazione fatta da move		
		//controls[id].position = getPosition(document.getElementById(id));
		setTimeout("dimmerCycleStart()",control.timer);
		break;
	case "blind":
		switch (status) {
		case "stopped":
			command = "open";
			newstatus = "opening";
			break;
		case "opening":
			command = "stop";
			newstatus = "opened";
			break;
		case "opened":
			command = "close";
			newstatus = "closing";
			break;
		case "closed":
			command = "open";
			newstatus = "opening";
			break;
		case "closing":
			command = "stop";
			newstatus = "closed";
			break;
		default:
			newstatus = "stop";
			command = "stop"
		}
		break;
	default:
		break;
	}
	if (newstatus != status) {
		controls[id].status = newstatus;
		document.getElementById(id+"-img").src = skin + control[newstatus];
	}
	if (command) {
		setPortValue(control.address,command);
	}
}

function fireDevicePortChangeEvent(evt) {
	var address = evt.A; 
	var id = addresses[address];
	if (id == null) {
		return;
	}
	var control = controls[id];
	if (control == null) {
		return;
	}
	var newValue = evt.V;
	controls[id].value=newValue;
	var label = control.label;
	if (label == null) {
		label = document.getElementById(id+"-label");
		controls[id].label = label;
	}
	var img = control.img;
	if (img == null) {
		img = document.getElementById(id+"-img");
		controls[id].img = img;
	}
	var newstatus = null;
	if (control.type == "dimmer") {
		if (newValue > 0) {
			newstatus = "on";
		} else {
			newstatus = "off";
		}
		label.innerHTML = newValue + "%";
	} else if (control.type == "light" || control.type == "power") {
		if (newValue == true) {
			newstatus = "on";
		} else if (newValue == false) {
			newstatus = "off";
		}
	} else if (control.type == "thermo") {
		if (address.split(":")[1] == "temp") { 
			label.innerHTML = newValue + "°C";
		}
	} else if (control.type == "blind") {
		newstatus = newValue;
	}
	if (newstatus != null && newstatus != status) {
		controls[id].status = newstatus;
		img.src = skin + control[newstatus];
	}
}


var streamReq = null;
var streamStart = 0;
var eventCounter = 0;
var errorCounter = 0;

function sendStreamRequest() {
	if (streamReq != null) {
		debug("Chiudo stream, state="+streamReq.readyState);
		streamReq.abort();
	}
	streamReq = new XMLHttpRequest();
	streamReq.open('GET', 'stream/', true);
	streamReq.send(null);
	streamReq.onreadystatechange = processStreamChange;
	streamStart = 0;
	debug("Aperto stream");
}

function processStreamChange() {
	debug("readyState:"+streamReq.readyState);
	if ((streamReq.readyState == 3 || streamReq.readyState == 4) && streamReq.status == 200) {
		var res = streamReq.responseText.substring(streamStart);
		var i = 0;
		while ((i = res.indexOf("\n")) > 0) {
			var res1 = res.substring(0, i);
			res = res.substring(i+1);
			streamStart += i+1;
			eventCounter++;
			try {
				debug(eventCounter+"/"+errorCounter+":"+res1);
				var evt;
				eval("evt="+res1+";");
				fireDevicePortChangeEvent(evt);
			} catch (e) {
				errorCounter++;
				// TODO: handle exception
				debug(e);
			}			
		}
	}
	if (streamReq.readyState == 4) {
		debug("in processStreamChange eseguo sendStreamRequest()");
		sendStreamRequest();		
	}
}

function setPortValue(address,value) {
	var setReq = new XMLHttpRequest();
	//setReq.open('GET', 'jais/set?address='+address+'&value='+value, true);
	setReq.open('GET', 'jais/set?'+address+'='+value, true);
	setReq.send(null);
	// TODO esattamente in quali casi rifare la richiesta ? 
	if (streamReq == null || streamReq.readyState == 0 || streamReq.readyState == 4) {
		debug("in setPortValue eseguo sendStreamRequest()");
		sendStreamRequest();
	}
}

/**
 * Ritorna la posizione di un elemento relativa al documento (?).
 *
 * <p>Questa funzione somma tutti gli offset dei parenti dell'elemento.</p>
 *
 * @param e l'elemento di cui calcolare la posizione.
 */
function getPosition(e){
	var left = 0;
	var top  = 0;

	while (e.offsetParent){
		left += e.offsetLeft;
		top  += e.offsetTop;
		e     = e.offsetParent;
	}

	left += e.offsetLeft;
	top  += e.offsetTop;
	// statusMessage("getPosition(" + e + ") = " + left + ", " + top); 
	return {x:left, y:top};
}

/**
 * Posizione del mouse relativa all'oggetto.
 *
 * @param ev evento che contiene un click o simile
 * @param objectPosition la posizione dell'oggetto che ha ricevuto il click
 * (calcolata da getPosition)
 */
function mouseCoords(ev, objectPosition){
	ev = ev || window.event;
	if(ev.pageX || ev.pageY){
		return {x:ev.pageX - objectPosition.x, y:ev.pageY -	objectPosition.y};
	}
	return {
		x:ev.clientX + document.body.scrollLeft - document.body.clientLeft -
			objectPosition.x,
		y:ev.clientY + document.body.scrollTop  - document.body.clientTop -
			objectPosition.y
	};
}

setTimeout('sendStreamRequest();', 1000);

