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
		if (status == "on") {
			newstatus = "off";
		} else {
			newstatus = "on";
		}
		break;

	case "blind":
		switch (status) {
		case "opening":
			newstatus = "opened";
			break;
		case "opened":
			newstatus = "closing";
			break;
		case "closed":
			newstatus = "opening";
			break;
		case "closing":
			newstatus = "closed";
			break;
		default:
			newstatus = "opening";
		}
		break;
	case "dimmer":
		alert("dimmer");		
		break;

	default:
		break;
	}
	if (newstatus != status) {
		controls[id].status = newstatus;
		document.getElementById(id+"-img").src = control[newstatus];
	}
}