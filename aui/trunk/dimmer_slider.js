/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
 
/**
 * Minima variazione per cui il cursore viene spostato. [pixel]
 *
 * <p>Questo parametro riduce la fluidita' ma aumenta la velocita' di risposta
 * del controllo.</p>
 *
 * <p>Il valore deve essere maggiore o uguale a 1.</p>
 */
const DIMMER_JERKINESS = 5;

/**
 * Periodo del contatore che rilegge il dimmer [msec].
 *
 * @see dimmerGetInterval
 */
const DIMMER_GET_INTERVAL = 1000;

/**
 * Coordinata y dello slider.
 */
var dimmerSliderTop;

/**
 * Coordinata X dello slider.
 */
var dimmerSliderLeft;

/**
 * Valore attuale del dimmer.
 */
var currentDimmerValue = 0;

/**
 * Altezza attuale del cursore del dimmer.
 *
 * <p>Questo valore puo' differire dal corrispondente currentdimmerValue
 * per la jerkiness.</p>
 */
var currentDimmerCursorTop = DIMMER_TOP_MIN;
  
/**
 * Layer che contiene il controllo dimmer e lo sfondo scuro.
 */
var dimmerLayer = document.getElementById("dimmer");

/**
 * Layer che contiene lo slider del dimmer.
 */
var dimmerSliderLayer = document.getElementById("dimmer-slider");

/**
 * Layer che contiene il solo cursore del dimmer.
 */
var dimmerCursorLayer = document.getElementById("dimmer-tasto");

/**
 * Nome del dimmer.
 */
var dimmerName;

/**
 * Indirizzo del dimmer sul bus.
 */
var dimmerAddress;

/**
 * Elemento che contiene il valore a cui si trova il dimmer.
 */
var dimmerValueElement;

/**
 * Elemento nel quale scrivere il valore percentuale.
 *
 * <p>Questo &lt;div&gt; si trova dentro il cursore.</p>
 */
var dimmerCursorText = document.getElementById("dimmer-tasto-testo");

/**
 * Icona del dimmer sulla mappa.
 */
var dimmerIconOnMap;

/**
 * Elemento che contiene il testo vicino all'icona del dimmer sulla mappa.
 */
var dimmerTextOnMap;

/**
 * Contatore che reitera l'impostazione del dimmer.
 *
 * <p>Questo oggetto e' necessario perche', durante lo spostamento del cursore,
 * alcune richieste si possono perdere. Per essere sicuri che l'ultima richiesta
 * venga ricevuta, questo contatore richiama l'ultima impostazione 
 * periodicamente.</p>
 */
var dimmerGetInterval = false;

/**
 * Calcola la posizione del cursore del dimmer a partire dal valore (0-100).
 */
function dimmerValue2Cursor(value) {
	if (value >= 100) {
		return DIMMER_TOP_MIN;
	} else if (value <= 0) {
		return DIMMER_TOP_MAX;
	} 
	return Math.floor((100 - value) / 100 *
		(DIMMER_TOP_MAX - DIMMER_TOP_MIN) + DIMMER_TOP_MIN);
}

/**
 * Calcola il valore dell'intensita' luminosa a partire dalla posizione del
 * cursore.
 */
function dimmerCursor2Value(cursorTop) {
	var y = cursorTop - DIMMER_CURSOR_MIDDLE; // Centriamo il centro del cursore
	if (y <= DIMMER_TOP_MIN) {
		return 100;
	} else if (y >= DIMMER_TOP_MAX) {
		return 0;
	}
	return 100 - Math.floor((y - DIMMER_TOP_MIN) / 
		(DIMMER_TOP_MAX - DIMMER_TOP_MIN) * 100);
}

/**
 * Avvia il timer che aggiorna lo stato del dimmer periodicamente.
 */
function startDimmerRefreshTimer() {
	if (!dimmerGetInterval) {
		dimmerGetInterval = setInterval("refreshDimmer()", DIMMER_GET_INTERVAL);
	}
}

/**
 * Blocca il timer che aggiorna lo stato del dimmer periodicamente.
 */
function stopDimmerRefreshTimer() {
	if (dimmerGetInterval) {
		window.clearInterval(dimmerGetInterval);
		dimmerGetInterval = false;
	}
}
/**
 * Mostra il cursore del dimmer.
 *
 * @param divOnMap elemento div che contiene l'icona del dimmer
 */
function showDimmer(divOnMap) {
	var maxTop, maxLeft, minTop, minLeft;
	var iconTop = parseInt(divOnMap.style.top.slice(0, -2));
	var iconLeft = parseInt(divOnMap.style.left.slice(0, -2));
	// Se siamo su fisso, vogliamo che lo slider appaia nello schermo
	if (MOBILE) {
		maxTop = mapSize.y - DIMMER_SLIDER_TOTAL_HEIGHT;
		maxLeft = mapSize.x - DIMMER_SLIDER_WIDTH;
		minTop = STATUS_BAR_HEIGHT;
		minLeft = 0;
	} else {
		maxTop = -currentMapPosition.y + MAP_AREA_HEIGHT - 
			DIMMER_SLIDER_TOTAL_HEIGHT;
		maxLeft = -currentMapPosition.x + MAP_AREA_WIDTH - 
			DIMMER_SLIDER_WIDTH;
		minTop = -currentMapPosition.y + STATUS_BAR_HEIGHT;
		minLeft = -currentMapPosition.x;
	}
	dimmerSliderTop = iconTop -	DIMMER_SLIDER_TOTAL_HEIGHT / 2;
	dimmerSliderLeft = iconLeft - DIMMER_SLIDER_WIDTH / 4; // un pochino sopra
	// Non usciamo dall'area della mappa
	if (dimmerSliderTop < minTop) {
		dimmerSliderTop = minTop;
	} else if (dimmerSliderTop > maxTop) {
		dimmerSliderTop = maxTop;
	}
	if (dimmerSliderLeft < minLeft) {
		dimmerSliderLeft = minLeft;
	} else if (dimmerSliderLeft > maxLeft) {
		dimmerSliderLeft = maxLeft;
	}
	dimmerSliderLayer.style.top = dimmerSliderTop + "px";
	dimmerSliderLayer.style.left = dimmerSliderLeft + "px";
	// Lo sfondo scuretto deve coprire tutta la mappa
	dimmerLayer.style.left = currentMapPosition.x;
	dimmerLayer.style.top = currentMapPosition.y;
	dimmerLayer.style.width = mapSize.width + "px";
	dimmerLayer.style.height = mapSize.height + "px";
	dimmerLayer.style.display = "";
	dimmerName = divOnMap.attributes.getNamedItem("name").value;
	var attributes = divOnMap.attributes;
	dimmerValueElement = attributes.getNamedItem("lit");
	dimmerIconOnMap = divOnMap.firstChild.firstChild;
	dimmerTextOnMap = divOnMap.lastChild.firstChild;
	dimmerAddress = attributes.getNamedItem("busaddress").value;
	currentDimmerValue = dimmerValueElement.value;
	currentDimmerCursorTop = dimmerValue2Cursor(currentDimmerValue); 
	dimmerCursorLayer.style.top = currentDimmerCursorTop + "px";
	dimmerCursorText.textContent = currentDimmerValue + "%";
	statusMessage(dimmerName);
	stopGetAllTimer();
	startDimmerRefreshTimer()
}

/**
 * Nasconde il cursore del dimmer.
 *
 * <p>Questa funzione viene chiamata quando il dimmer e' visualizzato e l'utente
 * fa click fuori dal cursore.</p>
 */
function hideDimmer() {
	dimmerLayer.style.display = "none";
	stopDimmerRefreshTimer();
	startGetAllTimer();
}

/**
 * Disegna il cursore del dimmer nella posizione specificata (in percentuale).
 */
function drawDimmerCursor(newValue) {
	var newTop = dimmerValue2Cursor(newValue);
	dimmerCursorLayer.style.top = newTop + "px";
	currentDimmerCursorTop = newTop;
	currentDimmerValue = newValue;
	dimmerCursorText.textContent = newValue + "%";
}

/**
 * Riceve la risposta alla richiesta setPort sul dimmer.
 *
 * <p>Se la richiesta e' andata bene, aggiorna il testo e 
 * l'icona sulla mappa.</p>
 *
 * <p>Se la richiesta e' andata male, ci pensera' il contatore a reiterare
 * l'impostazione.</p>
 */
function dimmerSetCallback(ok) {
	if (ok) { 
		// Aggiorniamo l'icona sulla mappa
		if (currentDimmerValue == 0) {	
			dimmerIconOnMap.src = IMG_LIGHT_OFF;
		} else {
			dimmerIconOnMap.src = IMG_LIGHT_ON;
		}
		dimmerTextOnMap.textContent = currentDimmerValue + "%";
		dimmerValueElement.value = currentDimmerValue;
	}
}

/**
 * Segue il trascinamento del cursore.
 *
 * <p>Interrompe il timer di aggiornamento automatico del dimmer.</p>
 *
 * @param mousePos posizione del mouse relativa allo slider.
 * @param forceUpdate facoltativo, se true impone che il cursore venga spostato.
 */
function dragDimmerCursor(mousePos, forceUpdate) {
	var y = mousePos.y;
	var newValue = dimmerCursor2Value(y);
	var newTop = dimmerValue2Cursor(newValue);
	stopDimmerRefreshTimer();
	// Il massimo e il minimo valore sono anti-jerkiness, perche' e' brutto
	// se il cursore non arriva in fondo.
	if (forceUpdate || 
		(Math.abs(newTop - currentDimmerCursorTop) >= DIMMER_JERKINESS) ||
		(newValue == 0) || (newValue == 100)) {
		drawDimmerCursor(newValue);
	}
	setPort(dimmerAddress, newValue, dimmerSetCallback);
}

/**
 * Conclude il trascinamento del cursore (o risponde a un click).
 *
 * <p>Fa ripartire il timer di aggiornamento del dimmer.</p>
 *
 * @param mousePos posizione del mouse relativa allo slider.
 */
function dragDimmerStop(mousePos) {
	dragDimmerCursor(mousePos, true);
	startDimmerRefreshTimer();
}

/**
 * Riceve la risposta alla richiesta getPort sul dimmer.
 *
 * <p>Se la richiesta e' andata bene, aggiorna la posizione del cursore, il 
 * testo e l'icona sulla mappa.</p>
 *
 * <p>Se la richiesta e' andata male, ci pensera' il contatore a reiterare
 * l'impostazione.</p>
 */
function dimmerGetCallback(ok) {
	if (ok) {
		currentDimmerValue = parseInt(ok);
		// Aggiorniamo l'icona sulla mappa
		if (currentDimmerValue == 0) {	
			dimmerIconOnMap.src = IMG_LIGHT_OFF;
		} else {
			dimmerIconOnMap.src = IMG_LIGHT_ON;
		}
		drawDimmerCursor(currentDimmerValue);
		dimmerTextOnMap.textContent = currentDimmerValue + "%";
		dimmerValueElement.value = currentDimmerValue;
	}
}
/**
 * Reitera la lettura del dimmer.
 *
 * <p>Questa funzione viene chiamata da un contatore.</p>
 */
function refreshDimmer() {
	getPort(dimmerAddress, dimmerGetCallback);
}

/**
 * Chiamata quando lo slider riceve un click.
 *
 * <p>Blocca la propagazione del click, altrimenti questo sarebbe ricevuto dal
 * div padre che chiuderebbe lo slider.</p>
 */
function dimmerSliderClicked(event) {
	event = event || window.event;
	event.stopPropagation();
}