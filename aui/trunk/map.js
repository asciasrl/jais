/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
/**
 * Elemento che contiene la mappa visualizzata.
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
 * Sposta la mappa in base alla posizione del mouse.
 *
 * @param mousePos posizione del mouse relativa all'oggetto.
 */
function dragMap(mousePos) {
	// statusObject.innerHTML = "Mouse:" + mousePos.x + ',' + mousePos.y;
  	// statusObject.innerHTML += ' Da:'+dragObject.offsetLeft+','+dragObject.offsetTop;

	new_top = mousePos.y - mouseOffset.y + lastMapPosition.y;
	new_left = mousePos.x - mouseOffset.x + lastMapPosition.x;
	// statusObject.innerHTML += ' A:'+new_top+','+new_left;
	// troppo a destra
	if (-new_left + 240 > dragObject.width) {
		new_left = 240 - dragObject.width;
	}
	// troppo a sinistra
	if (-new_left < 0) {
		new_left = 0;
	}
	// troppo in basso
	if (-new_top + 240 > dragObject.height) {
		new_top = 240 - dragObject.height;
	}
	// troppo in alto
	if (-new_top < 0) {
		new_top = 0;
	}
	dragObject.style.top =  new_top + 'px';
	dragObject.style.left =  new_left + 'px';
	currentMapPosition = {x:new_left, y:new_top};
	// statusObject.innerHTML += ' D '+(mousePos.x - mouseOffset.x)+','+(mousePos.y - mouseOffset.y);

	return false;
}

/**
 * Quando termina il trascinamento della mappa.
 */
function dragMapStop() {
	if (dragging) {
		lastMapPosition = currentMapPosition;
		statusObject.innerHTML = "Stopping at " + dragObject.style.left + ", " +
			dragObject.style.top;
	}
}
