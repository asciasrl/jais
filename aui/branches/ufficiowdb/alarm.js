/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */

/**
 * L'utente ha fatto click su un'allarme.
 *
 * <p>Se la tapparella si sta muovendo, la fermiamo. Altrimenti, apriamo il
 * controllo.</p>
 *
 * <p>TODO: Questa funzione non fa altro che cambiare l'icona.</p>
 */
function alarmClicked(event, alarmDiv) {
	var icon = alarmDiv.firstChild.firstChild;
	var statusElement = alarmDiv.attributes.getNamedItem("status");
	statusMessage(alarmDiv.attributes.getNamedItem("name").value);
	switch (statusElement.value) {
		case "open_ok": // La chiudiamo e la accendiamo
			statusElement.value = "close_on";
			icon.src = IMG_LOCK_CLOSE_OK;
			break;
		case "close_on": // Simuliamo un allarme
			statusElement.value = "open_alarm";
			icon.src = IMG_LOCK_OPEN_ALARM;
			break;
		case "open_alarm": // Togliamo l'allarme
			statusElement.value = "open_ok";
			icon.src = IMG_LOCK_OPEN;
			break;
	}
}