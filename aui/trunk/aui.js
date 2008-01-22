/**
 * (C) 2008 ASCIA S.r.l.
 * @author Sergio Strampelli 
 */

// http://www.webreference.com/programming/javascript/mk/column2/

var dragObject  = null;
var mouseOffset = null;
var objectOffset = null;

dragging = false;

/**
 * Posizione del mouse relativa al documento
 */
function mouseCoords(ev){
	ev = ev || window.event;
	if(ev.pageX || ev.pageY){
		return {x:ev.pageX, y:ev.pageY};
	}
	return {
		x:ev.clientX + document.body.scrollLeft - document.body.clientLeft,
		y:ev.clientY + document.body.scrollTop  - document.body.clientTop
	};
}

function getMouseOffset(target, ev){
	ev = ev || window.event;

	var docPos    = getPosition(target);
	var mousePos  = mouseCoords(ev);
	return {x:mousePos.x - docPos.x, y:mousePos.y - docPos.y};
}

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

	return {x:left, y:top};
}

function getOffset(e) {
	return {x:e.offsetLeft, y:e.offsetTop};
}


document.onmousemove = mouseMove;

/**
 * Funzione collegata al movimento del mouse
 */
function mouseMove(ev){
	ev = ev || window.event;
	var mousePos = mouseCoords(ev);
	if (dragObject) {
		switch (dragObject.id) {
			case 'piano-01A-big':
				dragMap(mousePos);
				break;
			case 'funzioni':
				dragFunzioni(mousePos);
				break;
		}
	}
}

function dragFunzioni(mousePos) {
  new_left = mousePos.x - mouseOffset.x + objectOffset.x;
  setHeader(new_left);
  dragObject.style.left =  new_left + 'px';
}

function dragFunzioniStop() {
  dragObject.style.left =  '0px';
}

function dragMap(mousePos) {
	//statusObject.innerHTML = mousePos.x + ',' + mousePos.y;

	el = document.getElementById('piano-01A-big');
  //statusObject.innerHTML += ' E '+el.offsetLeft+','+el.offsetTop;
  
	if (dragObject) {

	  dragging = true;

		new_top = mousePos.y - mouseOffset.y + objectOffset.y;
		new_left = mousePos.x - mouseOffset.x + objectOffset.x;

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
		//statusObject.innerHTML += ' D '+(mousePos.x - mouseOffset.x)+','+(mousePos.y - mouseOffset.y);

		return false;
	}
}

document.onmouseup   = mouseUp;

/**
 * Quando viene rilasciato il bottone del mouse
 */
function mouseUp(){
	// TODO: riposizionare icona delle funzioni in posizione centrale
	if (dragObject) {
		switch (dragObject.id) {
			case 'funzioni':
				dragFunzioniStop();
				break;
		}
	}	
	dragObject = null;	
}

/**
 * Associa all'oggetto la funzione che gestisce la pressione del mouse
 */
function makeDraggable(item){
	if(!item) return;
	item.onmousedown = function(ev){
		dragObject  = this;
		//mouseOffset = getMouseOffset(this, ev);
	  mouseOffset = mouseCoords(ev);
	  objectOffset = getOffset(this);
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

function clicca(da,a,ret) {
  if (dragging) return;
  if (first_click) {
    first_click = false;
    //setHeader("1� "+da+">"+a);
    click_timer = setTimeout("clicca1('"+da+"','"+a+"')",double_click_time);
  } else {
    first_click = true;
    clearTimeout(click_timer);
    //setHeader("2� "+da+">"+ret);
    clicca1(da,ret);
  }
}

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

function ingrandisci(ev,da,a,ret) {
  if (first_click) {
	  //setHeader("1� "+da+">"+a);
    first_click = false;
    click_timer = setTimeout("ingrandisci1("+ev.clientX+","+ev.clientY+",'"+da+"','"+a+"')",double_click_time);
  } else {
	  //setHeader("2� "+da+">"+ret);
    first_click = true;
    clearTimeout(click_timer);
    clicca1(da,ret);
  }
}

function ingrandisci1(X,Y,da,a) {
  first_click = true;
  //ev = window.event;
  da_el = document.getElementById(da);
  a_el = document.getElementById(a);
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
  el_div = da_el.parentNode;
  click_x = X - el_div.offsetLeft;
  click_y = Y - el_div.offsetTop;

  if (debug) alert('ev.x='+ev.clientX+' ev.y='+ev.clientY+' click_x='+click_x+' click_y='+click_y);
  
  // calcolo del punto equivalente sulla nuova mappa
/*
  new_x = a_el.clientWidth * click_x / da_el.clientWidth;
  new_y = a_el.clientHeight * click_y / da_el.clientHeight;
*/
  new_x = a_el.width * click_x / da_el.width;
  new_y = a_el.height * click_y / da_el.height;

  if (debug) alert('new_x='+new_x+' new_y='+new_y);
  
  // prova a mettere il nuovo punto esattamente nel centro
  new_left = new_x - 240 / 2;
  new_top = new_y - 240 / 2;
  
  // troppo a destra
  if (new_left + 240 > a_el.width) {
    new_left = a_el.width - 240;
  }
  // troppo a sinistra
  if (new_left < 0) {
    new_left = 0;
  }
  // troppo in basso
  if (new_top + 240 > a_el.height) {
    new_top = a_el.height - 240;
  }
  // troppo in alto
  if (new_top < 0) {
    new_top = 0;
  }
  
  if (debug) alert('left='+new_left+' top='+new_top);
  
  // scambia visualizzazione
  da_el.style.display='none';
  a_el.style.display='';
  a_el.style.left = '-' + new_left + 'px';
  a_el.style.top = '-' + new_top + 'px';
      
  //alert('ev.x='+ev.x+' ev.y='+ev.y+' dx='+dx+' dy='+dy);
}

makeDraggable(document.getElementById('piano-01A-big'));
makeDraggable(document.getElementById('funzioni'));
