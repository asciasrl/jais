/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
/**
 * Elemento (&lt;div&gt;) che contiene la mappa visualizzata.
 */
var currentMap;
/**
 * Attuale posizione della mappa.
 */
var currentMapPosition = {x:0, y:0};
/**
 * Ultima posizione della mappa.
 */
var lastMapPosition = {x:0, y:0};
/**
 * Dimensione della mappa.
 */
var mapSize;


/**
 * Imposta la mappa correntemente visualizzata.
 *
 * <p>Imposta le variabili globali di questo file con le caratteristiche
 * della mappa indicata.</p>
 *
 * @param element elemento &lt;div&gt; che contiene la mappa
 */
function setCurrentMap(element) {
	currentMap = element;
	mapSize = {width:element.style.width.slice(0, -2), 
			height:element.style.height.slice(0, -2)};
	refreshServicesLayer();
}

/**
 * Sposta la mappa in base alla posizione del mouse.
 *
 * @param mousePos posizione del mouse relativa all'oggetto.
 */
function dragMap(mousePos) {
	// statusMessage("Mouse:" + mousePos.x + ',' + mousePos.y);
  	// statusObject.innerHTML += ' Da:'+dragObject.offsetLeft+','+dragObject.offsetTop;
	var new_top = mousePos.y - mouseOffset.y + lastMapPosition.y;
	var new_left = mousePos.x - mouseOffset.x + lastMapPosition.x;
	// statusObject.innerHTML += ' D '+(mousePos.x - mouseOffset.x)+','+(mousePos.y - mouseOffset.y);
	currentMap = dragObject;
	drawMapAt(new_left, new_top);
	return false;
}

/**
 * Disegna la mappa ingrandita alla posizione indicata.
 *
 * <p>Se la posizione non e' valida, viene corretta in modo che una parte della
 * mappa sia sempre visibile.</p>
 *
 * <p>Richiede che mapSize e currentMap siano inizializzati.</p>
 *
 * @param new_left nuovo attributo "left" da imporre all'immagine
 * @param new_top nuovo attributo "top" da imporre all'immagine
 */
function drawMapAt(new_left, new_top) {
// statusMessage(' A:'+new_left+' / ' + mapSize.width + ' ,'+new_top);
	// troppo a destra
	if (-new_left + MAP_AREA_WIDTH > mapSize.width) {
		new_left = MAP_AREA_WIDTH - mapSize.width;
	}
	// troppo a sinistra
	if (new_left > 0) {
		new_left = 0;
	}
	// troppo in basso
	if (-new_top + MAP_AREA_HEIGHT > mapSize.height) {
		new_top = MAP_AREA_HEIGHT - mapSize.height;
	}
	// troppo in alto
	if (new_top > 0) {
		new_top = 0;
	}
	currentMap.style.top =  new_top + 'px';
	currentMap.style.left =  new_left + 'px';
	currentMapPosition = {x:new_left, y:new_top};	
}

/**
 * Quando termina il trascinamento della mappa.
 */
function dragMapStop() {
	if (dragging) {
		lastMapPosition = currentMapPosition; 
	}
}

/**
 * Fa apparire il layer corrispondente al servizio selezionato.
 *
 * <p>La mappa attuale e' letta dalla variabile currentMap. Il servizio
 * attivo e' letto dalla variabile activeService.</p>
 *
 * @param funzione il servizio selezionato.
 */
function refreshServicesLayer() {
	if ((!currentMap) || (!activeService)) { // Sanity check
		return;
	}
	var currentMapId = currentMap.id;
	// Accendiamo il solo layer giusto.
	for (i = 0; i < SERVICES.length; i++) {
		var service = SERVICES[i];
		var element = 
			document.getElementById(currentMapId + "-" + service);
		if ((SERVICES[i] == activeService) && (element)) {
			element.style.display = "";
		} else {
			if (element != null) {
				element.style.display = "none";
			}
		}
	} // For sui servizi
}