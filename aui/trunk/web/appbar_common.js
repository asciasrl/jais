/**
 * Copyright (C) 2008 Ascia S.r.l.
 *
 * <p>Questo file contiene le funzioni comune alle diverse appbar.</p>
 */

/**
 * Imposta la dimensione e l'opacita' di un'icona.
 *
 * @param name nome (id) del div che contiene l'immagine.
 * @param size dimensione da dare all'icona (0 - max)
 * @param opacity opacita' da dare all'icona (0 - 100).
 *
 * @return il &lt;div&gt; che contiene l'icona.
 */
function appbar_icoset(name,size,opacity) {
	debug("appbar_icoset("+name+")");
	div_obj = document.getElementById(name);
	if (div_obj == null) {
		alert("Non trovato elemento:"+name);
		return;
	}
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
	return div_obj;
}

/**
 * Questa funzione viene chiamata quando un'icona della appBar viene 
 * selezionata.
 *
 * <p>Accende il layer corrispondente alla funzione scelta, ma solo se
 * tale layer non e' gia' acceso.</p>
 */
function iconSelected(serviceName) {
	if (activeService != serviceName) {
		activeService = serviceName;
		refreshServicesLayer();
	}
}