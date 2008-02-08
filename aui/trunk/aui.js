/**
 * (C) 2008 ASCIA S.r.l.
 * @author Sergio Strampelli 
 */

// http://www.webreference.com/programming/javascript/mk/column2/

/**
 * L'oggetto che sta ricevendo un "drag" o un click del mouse.
 *
 * <p>Questo oggetto deve essere riconosciuto in base al proprio id</p>.
 */
var dragObject  = null;
/**
 * Qui viene messa la posizione del mouse, al momento della
 * pressione di un bottone, relativa all'oggetto che ha ricevuto il click.
 *
 * <p>Questo valore serve a ricordare dove e' stato fatto il click, per gestire
 * un'operazione di trascinamento.</p>
 */
var mouseOffset = null;
/**
 * La posizione, relativa al documento, dell'oggetto che ha ricevuto il click.
 */
var dragObjectPosition = null;
/**
 * True se l'utente sta trascinando qualcosa.
 */
var dragging = false;
/**
 * In quale istante si e' registrato l'ultimo evento di spostamento.
 */
var lastDragTimeStamp = 0;
/**
 * Servizio attivo.
 */
var activeService = "";

/**
 * Posizione del mouse relativa all'oggetto.
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

///**
// * Ritorna la posizione del mouse relativa a un frame.
// *
// * @param target il frame in cui calcolare la posizione.
// * @param ev un evento che contiene la posizione del mouse.
// *
// * @return le coordinate negli elementi {x, y}
// */
//	function getMouseOffset(target, ev){
//		ev = ev || window.event;
//	
//		var docPos    = getPosition(target);
//		var mousePos  = mouseCoords(ev);
//		return {x:mousePos.x - docPos.x, y:mousePos.y - docPos.y};
//	}

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
	// statusObject.innerHTML=("getPosition(" + e + ") = " + left + ", " + top); 
	return {x:left, y:top};
}

/**
 * Ritorna l'offset di un elemento.
 *
 * @return gli offset negli attributi {x, y}.
 */ 
function getOffset(e) {
	return {x:e.offsetLeft, y:e.offsetTop};
}

/**
 * Funzione collegata al movimento del mouse.
 */
function mouseMove(ev){
	if (dragObject) {
		ev = ev || window.event;
		var mousePos = mouseCoords(ev, dragObjectPosition);
		var d = new Date();
		dragging = true;
		lastDragTimeStamp = d.getTime();
		switch (dragObject.id) {
			case 'piano-01A-big':
				dragMap(mousePos);
				break;
			case 'appbar':
				dragAppBar(mousePos);
				break;
			case 'dimmer-sfondo':
				dragDimmerCursor(mousePos);
				break;
		}
	}
}

document.onmousemove = mouseMove;


/**
 * Quando viene rilasciato il bottone del mouse.
 */
function mouseUp(ev){
	ev = ev || window.event;
	if (dragObject) {
		var d = new Date();
		switch (dragObject.id) {
			case 'piano-01A-big':
				dragMapStop();
				break;
			case 'appbar':
				dragAppBarStop(mouseCoords(ev, dragObjectPosition), d.getTime());
				break;
		}
	}
	dragging = false;		
	dragObject = null;	
}

document.onmouseup   = mouseUp;

/**
 * Associa all'oggetto la funzione che gestisce la pressione del mouse.
 *
 * @param item elemento che deve gestire l'evento.
 */
function makeDraggable(item){
	if(!item) return;
	item.onmousedown = function(ev){
		dragObject  = this;
		//mouseOffset = getMouseOffset(this, ev);
		dragObjectPosition = getPosition(this);
		mouseOffset = mouseCoords(ev, dragObjectPosition);
		dragging = false;
		return false;
	}
}

function setHeader(s)
{
	headerObject = document.getElementById('header');
	headerObject.innerHTML = s;
}


var da_pagina = 'screensaver';
var debug = 0;

var double_click_time = 300;

function vai(a_pagina) {
  da_el = document.getElementById(da_pagina);
  a_el = document.getElementById(a_pagina);
  da_el.style.display='none';
  a_el.style.display='';
  //alert('da:'+da_pagina+' a:'+a_pagina);
  da_pagina = a_pagina;
}

var first_click = true;
var click_timer;

/**
 * Gestisce uno o due click, chiamando clicca1().
 *
 * @param da id del'oggetto da far sparire.
 * @param a id dell'oggetto da far apparire con un solo click.
 * @param ret id dell'oggetto da far apparire con un doppio click.
 *
 * @see clicca1()
 */
function clicca(da,a,ret) {
	if (dragging) return;
	if (first_click) {
		first_click = false;
		//setHeader("1° "+da+">"+a);
		click_timer = setTimeout("clicca1('"+da+"','"+a+"')",double_click_time);
	} else {
		first_click = true;
		clearTimeout(click_timer);
		//setHeader("2° "+da+">"+ret);
		clicca1(da,ret);
	}
}

/**
 * Riceve un click e scambia due layer.
 *
 * @param da id dell'oggetto da far sparire
 * @param a id dell'oggetto da far apparire
 */
function clicca1(da,a) {
	first_click = true;
	da_el = document.getElementById(da);
	a_el = document.getElementById(a);
	if (da_el && a_el) {
		da_el.style.display='none';
		a_el.style.display='';
		//setHeader(a_el.getProperty('header'));
	} else {
		window.alert("da="+da+" ("+da_el+") a="+a+" ("+a_el+")");
	}
	//window.event.cancelBubble = true;
}

/**
 * Gestisce uno o due click, chiamando ingrandisci1().
 *
 * @param ev evento click.
 * @param da id del'oggetto da far sparire.
 * @param a id dell'oggetto da far apparire con un solo click.
 * @param ret id dell'oggetto da far apparire con un doppio click.
 *
 * @see ingrandisci1()
 */
function ingrandisci(ev,da,a,ret) {
	if (first_click) {
		//setHeader("1° "+da+">"+a);
		first_click = false;
		var target = ev.target || ev.srcElement; // Firefox vs. IE
		var targetPos = getPosition(target);
		var relativeX = ev.clientX - targetPos.x;
		var relativeY = ev.clientY - targetPos.y; 
		click_timer = setTimeout("ingrandisci1("+ relativeX + "," + relativeY + 
			",'" + da + "','" + a + "')", double_click_time);
	} else {
		//setHeader("2° "+da+">"+ret);
		first_click = true;
		clearTimeout(click_timer);
		clicca1(da,ret);
	}
}

/**
 * Scambia due oggetti, mostrando il secondo alla posizione indicata.
 *
 * @param X coordinata X relativa all'oggetto che ha ricevuto il click.
 * @param Y coordinata Y relativa all'oggetto che ha ricevuto il click.
 * @param da id dell'oggetto da far sparire.
 * @param a id dell'oggetto da far apparire.
 */
function ingrandisci1(X,Y,da,a) {
	first_click = true;
	//ev = window.event;
	var da_el = document.getElementById(da);
	var a_el = document.getElementById(a);
	currentMap = a_el;
	//setHeader(a_el.getProperty('header'));
  
	// somma gli offset della gerarchia
/*
  el = da_el;
  s = '';
  x = 0;
  y = 0;
  while (el) {
    s += ' '+el.tagName+'('+el.offsetLeft+','+el.offsetTop+')';
	  x += el.offsetLeft;
  	y += el.offsetTop;    
  	
  }
  if (debug) alert(s);
*/
  
	// coordinate del click
	var el_div = da_el.parentNode;
	if (debug) alert('click_x='+X+' click_y='+Y);
  
	// calcolo del punto equivalente sulla nuova mappa
/*
  new_x = a_el.clientWidth * click_x / da_el.clientWidth;
  new_y = a_el.clientHeight * click_y / da_el.clientHeight;
*/
	// Ricaviamo le dimensioni degli elementi
	var a_width = a_el.style.width.slice(0, a_el.style.width.length - 2);
	var a_height = a_el.style.width.slice(0, a_el.style.height.length - 2);
	var da_width = da_el.style.width.slice(0, da_el.style.width.length - 2);
	var da_height = da_el.style.width.slice(0, da_el.style.height.length - 2);
	new_x = a_width * X / da_width;
	new_y = a_height * Y / da_height;

	if (debug) alert('new_x='+new_x+' new_y='+new_y);
  
	// prova a mettere il nuovo punto esattamente nel centro
	new_left = new_x - 240 / 2; // FIXME: parametrizza questo "240"
	new_top = new_y - 240 / 2;
  
	// troppo a destra
	if (new_left + 240 > a_el.width) {
		new_left = a_el.width - 240;
	}
	// troppo a sinistra
	if (!(new_left > 0)) { // Filtriamo NaN
		new_left = 0;
	}
	// troppo in basso
	if (new_top + 240 > a_el.height) {
		new_top = a_el.height - 240;
	}
	// troppo in alto
	if (!(new_top > 0)) { // Filtriamo NaN
		new_top = 0;
	}

	if (debug) alert('left='+new_left+' top='+new_top);
  
	// scambia visualizzazione
	da_el.style.display='none';
	a_el.style.display='';
	a_el.style.left = '-' + new_left + 'px';
	a_el.style.top = '-' + new_top + 'px';
    lastMapPosition = xurrentMapPosition = {x:-new_left, y:-new_top};  
  //alert('ev.x='+ev.x+' ev.y='+ev.y+' dx='+dx+' dy='+dy);
}

makeDraggable(document.getElementById('piano-01A-big'));
makeDraggable(document.getElementById('appbar'));
makeDraggable(document.getElementById('dimmer-sfondo'));