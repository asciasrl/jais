/**
 * appbar.js
 * (C) 2008 Ascia S.r.l.
 */
 
/**
 * Fattore di moltiplicazione per ottenere la velocita' di scorrimento a partire
 * dalla velocita' del mouse/dito.
 */
const FINGER_SPEED_FACTOR = 5;
/**
 * Decelerazione dello scorrimento. [pixel / sec^2]
 */
const FRICTION = 3;
/**
 * Rigidita' della molla che attira l'icona centrale verso il centro.
 */
const STIFFNESS = 20;
/**
 * Area di attrazione per l'icona centrale ("centro allargato").
 */
const LOCK_AREA_WIDTH = 2;
/**
 * Periodo di aggiornamento del calcolo della velocita'. [msec]
 */
const REFRESH_PERIOD = 50;
/**
 * Massima velocita' [pixel / sec].
 */
const MAX_ICON_SPEED = 100000;
/**
 * A che pixel si trovava l'appbar all'ultima selezione.
 */
var lastAppBarPosition = 0;
/**
 * A che pixel si trova l'appbar (durante lo scorrimento).
 */
var currentAppBarPosition = 40;
/**
 * Velocita' corrente della appbar.
 */
var currentAppBarSpeed = 0;
/**
 * True se le icone si sono fermate (bene).
 */
var centralIconLocked = false;
/**
 * DEBUG: dove scrivere messaggi di stato.
 */
var statusObject = document.getElementById('status');

/**
 * Imposta la dimensione e l'opacita' di un'icona.
 *
 * @param name nome (id) del div che contiene l'immagine.
 * @param size dimensione da dare all'icona (0 - max)
 * @param opacity opacita' da dare all'icona.
 */
function appbar_icoset(name,size,opacity) {
	div_obj = document.getElementById(name);
	img_obj = document.getElementById(name+'-img');	
	if (size == 0) {
	  div_obj.style.display='none';
	} else {
	  div_obj.style.display='';
	  div_obj.style.width=size+'px';	  
	  if (size < 80) {
	    height = (size*65/80); // nasconde la scritta
	  } else {
	    height = size;
	  }
	  div_obj.style.height=height+'px';
	  div_obj.style.marginTop=((80-size)/2)+'px';
	  img_obj.style.width=size+'px';
	  img_obj.style.height=size+'px';
	  
	  if (opacity < 100) {
	    div_obj.style.opacity=(opacity / 100);
	  } else {
	    div_obj.style.opacity='1';
	  }
	  div_obj.style.filter='alpha(opacity='+opacity+')';
	}
}

///**
// * Mostra le icone, centrando la numero n.
// */
//function appbar_select(n) {
//  if (n < 1) {
//    n = 1;
//  }
//  if (n > appbar_num) {
//    n = appbar_num;
//  }
//  if (n < 3) {
//    n = n + appbar_num;
//  }
//  if (n >= (appbar_num + 3)) {
//    n = n - appbar_num;
//  }
//  //if (n >= 3 && n <= (appbar_num - 2)) {
//  left = n - 2;  
// right = n + 2;
//  // nasconde tutte le icone a sinistra
//  for (i=1;i<left;i++) {
//    appbar_icoset('app-'+i,0,0);
//  } 
//  // icone estrema sinistra e destra 50x50 al 20%
//  appbar_icoset('app-'+left,50,20);
//  appbar_icoset('app-'+right,50,20);
//  
//  // icona centrale al 100%
//  appbar_icoset('app-'+n,80,100);
//  
//  // icone sinistra e destra 70x70 al 40%
//  appbar_icoset('app-'+(n-1),70,40);
//  appbar_icoset('app-'+(n+1),70,40);
//  
//  // nasconde tutte le icone a destra
//  for (i=right+1;i <= (appbar_num + 5);i++) {
//    appbar_icoset('app-'+i,0,0);
//  } 
//}

//appbar_select(3);

/**
 * Posiziona la barra delle applicazioni.
 *
 * <p>Questa funzione imposta currentAppBarPosition.</p>
 *
 * @param n un numero intero che indica i pixel di spostamento;
 * la prima icona corrisponde all'intervallo 1-80, la seconda 81-160 ecc 
 */
function appbar_scroll(n) {
	currentAppBarPosition = n;
	// minimalista
	//appbar_select(Math.round(n/80));  

	var p = (n + 40) / 80;
	p = appbar_num * (p/appbar_num - Math.floor(p/appbar_num));
	sel = Math.round(p);
	if (p < 3) {
		p = p + appbar_num;
	}
	if (p >= appbar_num + 3) {
		p = p - appbar_num;
	}

	var s = "n="+n+" sel="+sel+" p="+p;
  
	// determino per ogni icona la grandezza
	for (i=1; i <= appbar_num + 5; i++) {
		var size, opacity;
		var d = Math.abs(p - i);
		if (d >= 3) {
			size = 0;
			opacity = 0;
		} else if (d > 0.5) {
			size = Math.round(80 - 20 * (d - 0.5));
			opacity = Math.round(100 - 40 * (d - 0.5));
		} else {
			size = 80;
			opacity = 100;
		}
		appbar_icoset('app-'+i,size,opacity);
		// s=s+" i="+i+" d="+d+" size="+size;    	
	}
  
	// sistemo l'offset
	//  n    off   size   k
	// 279 -> 30    30    0.99     80 - 20 * 2.49 = 30    
	// 280 ->  0    50    0.00     80 - 20 * 1.50 = 50
	// 281 ->  0    50    0.01     80 - 20 * 1.49 = 50   
	// 300 -> 11    45    0.25     80 - 20 * 1.75 = 45
	// 319 -> 20    40    0.49     80 - 20 * 1.99 = 40
	// 320 -> 20    40    0.5      80 - 20 * 2.00 = 40
	// 359 -> 30    30    0.99     80 - 20 * 2.49 = 30
	    
	var k = p - Math.floor(p)
	// s+=" k="+k;
	
	var offset = (50 - 20 * k ) * k;
	offset = Math.round(offset);
   
	var scrollerObject = document.getElementById('scroller');
	scrollerObject.style.left = '-'+offset+'px';
	// s+=" offset="+offset;
  
	// headerObject = document.getElementById('status');
	// headerObject.innerHTML = s;
}

//appbar_scroll(300);
//appbar_scroll(320);


/**
 * Calcola la distanza dell'icona piu' centrale dal centro.
 */
function centralIconDeltaX() {
	var deltaX;
	if (currentAppBarPosition > 0) {
		deltaX = 40 - (currentAppBarPosition % 80);
	} else {
		deltaX = (-currentAppBarPosition % 80) - 40;
	}
	return deltaX;
}

/**
 * Anima le icone, calcolando le accelerazioni.
 */
function appbar_timer() {
	if ((!dragging) && (!centralIconLocked)) {
		var new_left;
		// Attrito cinematico
		var accel = -FRICTION * currentAppBarSpeed;
		var dX = centralIconDeltaX();
		var adX = Math.abs(dX);
		// Molla lineare
		if (dX > 0) {
			accel += STIFFNESS * adX;
		} else {
			accel -= STIFFNESS * adX;
		}
		statusObject.innerHTML = "accel: " + accel;
		currentAppBarSpeed += ((accel * REFRESH_PERIOD / 1000));
		// Trucco: se stiamo lenti e vicini allo 0, ci fermiamo li'
		if ((adX < LOCK_AREA_WIDTH) && 
			(Math.abs(currentAppBarSpeed) <	(STIFFNESS * adX * 2))) {
			// Lock!
			currentAppBarSpeed = 0;
			currentAppBarPosition += centralIconDeltaX();
			centralIconLocked = true;
		}
		new_left = currentAppBarPosition + 
			currentAppBarSpeed * (REFRESH_PERIOD / 1000);
		appbar_scroll(new_left);
		lastAppBarPosition = currentAppBarPosition;
	}
	setTimeout("appbar_timer()", REFRESH_PERIOD);
}

/**
 * Muove l'appbar.
 *
 * @param mousePos posizione attuale del mouse.
 * @param timeStamp istante di tempo (in millisecondi) al quale si e' verificato
 * l'evento.
 */
function dragAppBar(mousePos) {
	var new_left = mouseOffset.x - mousePos.x;
	// statusObject.innerHTML = new_left; 
	appbar_scroll(new_left + lastAppBarPosition);
	centralIconLocked = false;
}

/**
 * Calcola la velocita' necessaria per centrare l'app-bar sull'icona piu'
 * vicina al centro.
 */
function attractCentralAppBarIcon() {
	var deltaX = centralIconDeltaX();
	if (deltaX > 0) {
		currentAppBarSpeed = Math.sqrt(2 * FRICTION * deltaX);
	} else if (deltaX < 0) {
		currentAppBarSpeed = -Math.sqrt(-2 * FRICTION * deltaX);
	} else { // Siamo gia' centrati.
		currentAppBarSpeed = 0;
	}
}

/**
 * Calcola la velocita' dell'ultimo trascinamento e attiva lo scorrimento 
 * automatico delle icone.
 *
 * <p>Questa funzione viene chiamata quando termina un'operazione di 
 * trascinamento.</p>
 */
function dragAppBarStop(mousePos, timeStamp) {
	var new_left = mouseOffset.x - mousePos.x;
	if (timeStamp != 0) { // Abbiamo il timeStamp?
		// Calcoliamo e saturiamo la velocita'
		currentAppBarSpeed = new_left / 
			(timeStamp - lastDragTimeStamp) * FINGER_SPEED_FACTOR;
		if (currentAppBarSpeed > MAX_ICON_SPEED) {
			currentAppBarSpeed = MAX_ICON_SPEED;
		} else if (currentAppBarSpeed < - MAX_ICON_SPEED) {
			currentAppBarSpeed = - MAX_ICON_SPEED;
		}
	} else {
		currentAppBarSpeed = 0;
	}
	lastAppBarPosition = currentAppBarPosition;
	centralIconLocked = false;
	/* statusObject.innerHTML = new_left + " / " + 
		(timeStamp - lastDragTimeStamp) * FINGER_SPEED_FACTOR; */
}

setTimeout("appbar_timer()", REFRESH_PERIOD);
makeDraggable(document.getElementById('appbar'));