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
const FRICTION = 100;
/**
 * Accelerazione delle icone. [pixel / sec^2].
 *
 * <p>Le icone accelerano se partono piu' lente della velocita' di crociera.</p>
 */
const ICON_ACCELERATION = 100; 
/**
 * Velocita' di crociera delle icone. [pixel / sec]
 */
const ICON_CRUISE_SPEED = 320;
/**
 * Velocita' di avvicinamento delle icone a quella centrale. [pixel / sec]
 */
const ICON_APPROACHING_SPEED = 60;
/**
 * Distanza dal centro che fa decelerare le icone.
 *
 * <p>Quando l'icona piu' al centro arriva a questa distanza dal centro, essa
 * inizia a decelerare.</p>
 */
const ICON_LOCK_DISTANCE = 20;
/**
 * Periodo di aggiornamento del calcolo della velocita'. [msec]
 */
const REFRESH_PERIOD = 50;
/**
 * Massima velocita' [pixel / sec].
 */
const MAX_ICON_SPEED = 240;

const APPBAR_START_POSITION = 40;
/**
 * A che pixel si trovava l'appbar all'ultima selezione.
 */
var lastAppBarPosition = APPBAR_START_POSITION;
/**
 * A che pixel si trova l'appbar (durante lo scorrimento).
 */
var currentAppBarPosition = APPBAR_START_POSITION;
/**
 * Numero dell'icona attualmente centrata.
 */
var centeredAppBarIconNumber;
/**
 * Velocita' corrente della appbar.
 */
var currentAppBarSpeed = 0;
/**
 * True se le icone si sono fermate (bene).
 */
var centralIconLocked = false;
/**
 * True se stiamo andando verso un'icona precisa.
 */
var appBarGoing = false;
/**
 * Dove stiamo andando (posizione desiderata).
 *
 * <p>Questo valore deve essere controllato se appBarGoing e' true.</p>
 */
var targetAppBarPosition = 0;
/**
 * Ultimo valore di dX.
 *
 * <p>Questa variabile serve a rilevare se abbiamo fatto "salti" durante
 * lo scrolling, precisamente se abbiamo saltato il nostro obbiettivo. Questo
 * avviene se dX ha segno opposto a lastDx</p>
 */
var lastDx = 0;
/**
 * Istante in cui e' stata effettuata l'ultima iterazione dello scorrimento
 * della barra.
 *
 * <p>Questa variabile serve per mantenere la velocita' costante anche se il
 * periodo di refresh richiesto non viene rispettato.</p>
 */
var lastAppBarScrollTime = 0;

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
 * Ritorna il numero dell'icona al centro, a partire dalla posizione
 * della barra.
 *
 * <p>Se la barra non e' centrata su un'icona, viene ritornato un numero
 * decimale.</p>
 *
 * @return un numero tra 0 e appbar_num. Nota che entrambi questi i valori
 * limite corrispondono allo stesso servizio: l'ultimo. 
 */
function getCenteredAppBarIconNumber(appBarPosition) {
	// Posizione interna alla barra
	var p = (appBarPosition - 40) / 80;
	// Portiamo p nell'intervallo [0, appbar_num]
	p = (appbar_num) * (p/(appbar_num) - Math.floor(p/(appbar_num)));
	return p;
}
 
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

	// Posizione interna alla barra
	var p = getCenteredAppBarIconNumber(n);
	// Numero dell'icona selezionata
	centeredAppBarIconNumber = Math.round(p);
	if (p < 3) {
		p = p + appbar_num;
	}
	if (p >= appbar_num + 3) {
		p = p - appbar_num;
	}

	var s = "n="+n+" sel="+centeredAppBarIconNumber+" p="+p;
  
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
	
	var appBarOffset = (50 - 20 * k ) * k;
	appBarOffset = Math.round(appBarOffset);
   
	var scrollerObject = document.getElementById('scroller');
	scrollerObject.style.left = '-'+appBarOffset+'px';
	// s+=" offset="+offset;
  
	// headerObject = document.getElementById('status');
	// headerObject.innerHTML = s;
}

//appbar_scroll(300);
//appbar_scroll(320);


/**
 * Calcola la distanza dal centro dell'icona più centrale o di quella 
 * selezionata.
 */
function centralIconDeltaX() {
	var deltaX;
	if (appBarGoing) {
		// Stiamo andando verso un'icona precisa
		deltaX = targetAppBarPosition - currentAppBarPosition;
	} else {
		// Calcoliamo rispetto all'icona piu' al centro
		if (currentAppBarPosition > 0) {
			deltaX = 40 - (currentAppBarPosition % 80);
		} else {
			deltaX = (-currentAppBarPosition % 80) - 40;
		}
	}
	return deltaX;
}

/**
 * Attiva il servizio rappresentato dall'icona piu' vicina al centro.
 */
function activateCenteredIcon() {
	var selectedIconNumber = 
		Math.round(getCenteredAppBarIconNumber(currentAppBarPosition));
	if (selectedIconNumber == 0) {
		selectedIconNumber = appbar_num;
	}
	var selectedService = SERVICES[selectedIconNumber - 1];
	iconSelected(selectedService);
}

/**
 * Anima le icone, calcolando le accelerazioni.
 */
function appbar_timer() {
	if ((!dragging) && (!centralIconLocked)) {
		var currentTime = new Date().getTime();
		var measuredRefreshPeriod;
		if (lastAppBarScrollTime != 0) {
			// "Saltiamo" se l'ultima iterazione e' stata fatta troppo tempo fa
			measuredRefreshPeriod = currentTime - lastAppBarScrollTime;
			currentAppBarPosition += currentAppBarSpeed * (measuredRefreshPeriod - REFRESH_PERIOD) / 1000;
		} else {
			measuredRefreshPeriod = REFRESH_PERIOD;
		}
		// statusMessage(measuredRefreshPeriod);
		lastAppBarScrollTime = currentTime;
		var dX = centralIconDeltaX();
		var speedSign = 0;
		if (currentAppBarSpeed == 0) {
			// Stiamo partendo da fermi -> partiamo lenti
			if (dX > 0) {
				currentAppBarSpeed = ICON_APPROACHING_SPEED;
			} else if (dX < 0) {
				currentAppBarSpeed = -ICON_APPROACHING_SPEED;
			}
		}
		if (currentAppBarSpeed > 0) {
			speedSign = 1;
		} else if (currentAppBarSpeed < 0) {
			speedSign = -1;
		}
		if (Math.abs(currentAppBarSpeed) > ICON_CRUISE_SPEED) {
			// Siamo molto veloci: freniamo e continuiamo a scorrere
			currentAppBarSpeed -= FRICTION * REFRESH_PERIOD * speedSign / 1000;
		} else {
			// Abbiamo saltato l'icona centrale?
			if ((dX * lastDx) < 0) { // Si'! Compensiamo.
				currentAppBarPosition += dX;
				dX = 0; // Per i prossimi check.
			}
			// Andiamo verso l'icona centrale?
			if ((dX * speedSign) >= 0) { 
				// Valutiamo la possibilita' di decelerare
				if (Math.abs(dX) <= ICON_LOCK_DISTANCE) {
					// Ci avviciniamo lentamente all'icona, che attiviamo
					activateCenteredIcon();
					if (dX > 0) {
						currentAppBarSpeed = ICON_APPROACHING_SPEED;
					} else {
						currentAppBarSpeed = -ICON_APPROACHING_SPEED;
					}
					// Al passo successivo andremmo oltre?
					if (Math.abs(dX) <= 
						ICON_APPROACHING_SPEED * REFRESH_PERIOD / 1000 ) {
						currentAppBarSpeed = 0;
						// Siamo arrivati!
						centralIconLocked = true;
						going = false;
						lastAppBarScrollTime = 0;
						dX = 0; // Trucco :-)
					} 
				} else {
					// Portiamoci a velocita' di crociera, accelerando se
					// necessario 
					var acceleratedSpeed = currentAppBarSpeed + speedSign *
						ICON_ACCELERATION;
					if (Math.abs(acceleratedSpeed) <= ICON_CRUISE_SPEED) {
						currentAppBarSpeed = acceleratedSpeed;
					} else {
						currentAppBarSpeed = ICON_CRUISE_SPEED * speedSign;
					}
				}
			} else {
				// Ci allontaniamo dall'icona centrale a velocita' di crociera
				currentAppBarSpeed = ICON_CRUISE_SPEED * speedSign;
			}
		} // Se siamo troppo veloci
		new_left = Math.round(currentAppBarPosition + 
			currentAppBarSpeed * (REFRESH_PERIOD / 1000));
		appbar_scroll(new_left);
		lastAppBarPosition = currentAppBarPosition;
		lastDx = dX;
	}
	// Andremo a fare un'altra iterazione solo se necessario. 
	if (!centralIconLocked) {
		setTimeout("appbar_timer()", REFRESH_PERIOD);
	}
}

/**
 * Muove l'appbar.
 *
 * @param mousePos posizione attuale del mouse relativa all'appbar.
 */
function dragAppBar(mousePos) {
	var new_left = mouseOffset.x - mousePos.x;
	// statusMessage(new_left); 
	appbar_scroll(new_left + lastAppBarPosition);
	centralIconLocked = false;
	appBarGoing = false;
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
 * Satura la velocita' delle icone.
 */
function saturateAppBarSpeed() { 
	if (currentAppBarSpeed > MAX_ICON_SPEED) {
		currentAppBarSpeed = MAX_ICON_SPEED;
	} else if (currentAppBarSpeed < - MAX_ICON_SPEED) {			
		currentAppBarSpeed = - MAX_ICON_SPEED;
	}
}

/**
 * Calcola la velocita' dell'ultimo trascinamento e attiva lo scorrimento 
 * automatico delle icone, oppure risponde a un click.
 *
 * <p>Questa funzione viene chiamata quando il bottone del mouse viene 
 * rilasciato.</p>
 *
 * @param mousePos posizione del mouse relativa all'appbar.
 * @param timeStamp istante in cui il bottone viene rilasciato.
 */
function dragAppBarStop(mousePos, timeStamp) {
	var new_left = mouseOffset.x - mousePos.x;
	if (dragging) {
		// E' la fine di un trascinamento!
		if (timeStamp != 0) { // Abbiamo il timeStamp?
			// Calcoliamo e saturiamo la velocita'
			currentAppBarSpeed = Math.round(new_left / 
				(timeStamp - lastDragTimeStamp) * FINGER_SPEED_FACTOR);
			saturateAppBarSpeed();
		} else {
			currentAppBarSpeed = 0;
		}
	} else { // Era un click
		appBarGoing = true;
		targetAppBarPosition = mousePos.x + currentAppBarPosition - 
			 dragObject.offsetWidth / 2;
		// Trucco: approssimiamo le coordinate del click, in modo da prendere
		// il centro di un'icona
		targetAppBarPosition = 
			Math.round((targetAppBarPosition - 40) / 80) * 80 + 40; 
		// Capiamo quale icona e' interessata
		var selectedIconNumber = 
			Math.round(getCenteredAppBarIconNumber(targetAppBarPosition));
		if (selectedIconNumber == 0) {
			selectedIconNumber = appbar_num;
		}
		var selectedService = SERVICES[selectedIconNumber - 1];
		iconSelected(selectedService);
	}
	lastAppBarPosition = currentAppBarPosition;
	centralIconLocked = false;
	/* statusMessage(new_left + " / " + 
		(timeStamp - lastDragTimeStamp) * FINGER_SPEED_FACTOR); */
	// Attiviamo lo scorrimento automatico.
	setTimeout("appbar_timer()", REFRESH_PERIOD);
}

appbar_scroll(currentAppBarPosition);
activateCenteredIcon();
makeDraggable(document.getElementById('appbar'));