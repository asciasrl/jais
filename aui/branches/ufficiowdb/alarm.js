/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */

/**
 * L'utente ha fatto click su una porta.
 *
 * <p>Apriamo o chiudiamo la porta.</p>
 *
 * <p>TODO: Questa funzione non fa altro che cambiare lo stato e l'icona.</p>
 */
function doorClicked(event, alarmDiv) {
	var icon = alarmDiv.firstChild.firstChild;
	var statusElement = alarmDiv.attributes.getNamedItem("status");
	statusMessage(alarmDiv.attributes.getNamedItem("name").value);
	switch (statusElement.value) {
	case "open": // La chiudiamo
		statusElement.value = "close";
		break;
	case "close": // La apriamo
		statusElement.value = "open";
		break;
	}
	updateDoorIcon(alarmDiv);
}

/**
 * Aggiorna l'icona di una porta, leggendone lo stato.
 */
function updateDoorIcon(doorDiv) {
	var icon = doorDiv.firstChild.firstChild;
	var statusElement = doorDiv.attributes.getNamedItem("status");
	var alarmElement = doorDiv.attributes.getNamedItem("alarm");
	var alarmSet = (alarmElement.value == "on"); 
	switch (statusElement.value) {
	case "open":
		if (alarmSet) {
			icon.src = IMG_DOOR_OPEN_ALARM;
		} else {
			icon.src = IMG_DOOR_OPEN;
		}
		break;
	case "close": // La apriamo
		if (alarmSet) {
			icon.src = IMG_DOOR_CLOSE_OK;
		} else {
			icon.src = IMG_DOOR_CLOSE;
		}
		break;
	}
}


function lockClicked(event, alarmDiv) {
	var icon = alarmDiv.firstChild.firstChild;
	var statusElement = alarmDiv.attributes.getNamedItem("status");
	// FIXME
	var myDoor = document.getElementById("p1a-porta1");
	var doorAlarm = myDoor.attributes.getNamedItem("alarm");
	statusMessage(alarmDiv.attributes.getNamedItem("name").value);
	switch (statusElement.value) {
	case "off": // Lo chiudiamo e mettiamo gli allarmi
		statusElement.value = "on";
		icon.src = IMG_LOCK_CLOSE;
		doorAlarm.value = "on";
		break;
	case "on": // Lo apriamo e togliamo gli allarmi
		statusElement.value = "off";
		icon.src = IMG_LOCK_OPEN;
		doorAlarm.value = "off";
		break;
	}
	updateDoorIcon(myDoor);
}