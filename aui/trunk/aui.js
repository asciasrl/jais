/**
 * (C) 2009 ASCIA S.r.l.
 * @author Sergio Strampelli 
 */

function changePage(da,a) {
	da_el = document.getElementById(da);
	a_el = document.getElementById(a);
	da_el.style.display='none';
	a_el.style.display='';
}

function touchControl(event,id) {
	event.preventDefault();
	event.stopPropagation();
	var control = controls[id];
	if (control == null) {
		return;
	}
	var type = control.type;
	var status = control.status;
	var newstatus = status;
	switch (type) {
	case "light":
	case "dimmer":
		if (status == "on") {
			newstatus = "off";
		} else {
			newstatus = "on";
		}
		break;

	case "blind":
		switch (status) {
		case "opening":
			newstatus = "stop";
			break;
		case "opened":
			newstatus = "closing";
			break;
		case "closed":
			newstatus = "opening";
			break;
		case "closing":
			newstatus = "stop";
			break;
		default:
			newstatus = "opening";
		}
		break;
	default:
		break;
	}
	if (newstatus != status) {
		controls[id].status = newstatus;
		document.getElementById(id+"-img").src = control[newstatus];
		setPortValue(control.address,newstatus);
	}
}

function fireDevicePortChangeEvent(evt) {
	id = addresses[evt.fullAddress];
	if (id == null) {
		return;
	}
	control = controls[id];
	if (control == null) {
		return;
	}
	newstatus = null;
	if (control.type == "dimmer") {
		if (evt.newValue > 0) {
			newstatus = "on";
		} else {
			newstatus = "off";
		}		
		document.getElementById(id+"-label").innerHTML = evt.newValue + "%";
	} else if (control.type == "light") {
		if (evt.newValue == true) {
			newstatus = "on";
		} else if (evt.newValue == false) {
			newstatus = "off";
		}
	}
	if (newstatus != null && newstatus != status) {
		controls[id].status = newstatus;
		document.getElementById(id+"-img").src = control[newstatus];
	}
}


var streamReq = null;
var streamStart = 0;

function sendStreamRequest() {
	streamReq = new XMLHttpRequest();
	streamReq.onreadystatechange = processStreamChange;
	streamReq.open('GET', 'stream/', true);
	streamReq.send(null);
	streamStart = 0;
}

function processStreamChange() {
	var res = null;
	if (streamReq.readyState == 4 && ! streamReq.status == 200) {
		sendStreamRequest();
	} else if (streamReq.readyState == 3 && streamReq.status == 200) {
		res = streamReq.responseText.substring(streamStart);
		streamStart += res.length;
		if (streamStart > 100000) {
			streamReq.abort();
			sendStreamRequest();
		}
	} else if (streamReq.readyState == 4 && streamReq.status == 200) {
		res = streamReq.responseText.substring(resStart);
		sendStreamRequest();
	}
	if (res != null && res.length > 0) {
		try {
			//window.alert(res);
			eval( res);
			//window.alert(evt);
			//fireDevicePortChangeEvent(evt);
		} catch (e) {
			// TODO: handle exception
			window.alert(e);
		}
	}
}

function setPortValue(address,value) {
	var setReq = new XMLHttpRequest();
	setReq.open('GET', 'jais/set?address='+address+'&value='+value, true);
	setReq.send(null);
	if (streamReq == null || streamReq.readyState == 0) {
		sendStreamRequest();
	}
}

document.onreadystatechange=fnStartInit;
function fnStartInit()
{
	console.log("doc:"+document.readyState);
	if (document.readyState=="complete")
	{
		sendStreamRequest();
	}
}