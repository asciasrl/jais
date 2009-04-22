/**
 * (C) 2009 ASCIA S.r.l.
 * @author Sergio Strampelli 
 */

if(!window.XMLHttpRequest) {
	alert("Utilizzare un browser piu' recente!")
}

function debug(s) {
	var el = document.getElementById("debug");
	if (el) {
		el.innerHTML = s + "<br/>" + el.innerHTML; 
	}
}


var layerId = null;

function layerMove(event,id) {
	if (layerId) {
		var layer = layers[layerId];
		if (layer == null) {
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
		console.log("dx="+dx+" dy="+dy);
	}
}

var targetControlId = null; 

function endControl(event,id) {
	var control = controls[targetControlId];
	if (control == null) {
		return;
	}
	if (control.type == "dimmer") {
		var cycling = control.cycling;
		if (cycling == null || cycling == false) {
			var status = control.status;
			var newstatus = null;
			if (status == "on") {
				newstatus = "off";
			} else {
				newstatus = "on";
			}
			controls[targetControlId].status = newstatus;
			document.getElementById(targetControlId+"-img").src = skin + control[newstatus];
			AUI.SetRequest.send(control.address,"toggle");
		} else {
			controls[id].step = -(control.step);
			controls[targetControlId].cycling = false;
		}
	}
	targetControlId = null
}

function dimmerCycleStart() {
	var control = controls[targetControlId];
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
	var control = controls[targetControlId];
	if (control == null) {
		return;
	}
	if (control.type == "dimmer") {
		var value = control.value;
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
		AUI.SetRequest.send(control.address,value);
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
	var src = null;
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
	case "webcam":
		if (status == "play") {
			newstatus = "pause";
			src = skin + control.pause;
		} else {
			newstatus = "play";
			src = control.video;
		}		
		break;
	default:
		break;
	}
	if (newstatus != status) {
		controls[id].status = newstatus;
		if (src == null) {
			document.getElementById(id+"-img").src = skin + control[newstatus];
		} else {
			document.getElementById(id+"-img").src = src;
		}
	}
	if (command) {
		//sendSetRequest(control.address,command);
		AUI.SetRequest.send(control.address,command);
	}
}

var activeService = ""; // TODO 

function touchLayer(event,id) {
	event.preventDefault();
	event.stopPropagation();
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

 /**
  * 
  * @return Oggetto XMLHttpRequest 
  */
 function getXMLHttpRequest() {   
    var req = false;
    try {
        req = new XMLHttpRequest();
    } catch (e0) {
        try {   
            req=new ActiveXObject("Msxml2.XMLHTTP");   
            alert("2");
        } catch (e1) {   
            try {   
                req=new ActiveXObject("Microsoft.XMLHTTP");   
                alert("1");
            } catch (e2) {   
                try {   
                    req=new ActiveXObject("Msxml2.XMLHTTP.4.0");   
                    alert("4");
                } catch (e3) {   
                    req=null;   
                }   
            }   
        }   
    }   
  
    if (!req) alert("Browser non compatibile");   
  
    return req;   
}   


