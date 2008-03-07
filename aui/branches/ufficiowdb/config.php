<?php
/**
 * Copyright (C) 2008 ASCIA S.r.l.
 *
 * <p>Questo file contiene le informazioni di configurazione di default per 
 * AUI.</p>
 *
 * <p>Queste informazioni devono essere integrate (eventualmente anche
 * sovrascritte) dal file custom/config.php.</p>
 */

/**
 * Larghezza della "viewport" dell'iPod Touch. [pixel]
 */
define("IPOD_VIEWPORT_WIDTH", 320);

/**
 * Altezza della "viewport" dell'iPod Touch. [pixel]
 */
define("IPOD_VIEWPORT_HEIGHT", 356);

/**
 * Altezza dell'area in cui si visualizza la mappa. [pixel]
 */
define("IPOD_MAP_AREA_HEIGHT", 276); 

/**
 * Altezza della "parte stondata" degli estremi dello slider del dimmer [pixel].
 *
 * <p>Gli estremi del dimmer hanno gli angoli arrotondati. Questa e' l'altezza
 * di tali angoli, che stanno disegnati dentro l'immagine degli estremi dello
 * slider.</p>
 */
define("DIMMER_SLIDER_CORNER_HEIGHT", 5);

/**
 * Larghezza del bordo dello slider del dimmer.
 *
 * <p>Lo slider del dimmer ha un bordo che deve essere simmetrico rispetto
 * all'asse verticale. La sua larghezza in pixel e' questa.</p>
 */
define("DIMMER_SLIDER_BORDER_WIDTH", 6);

/**
 * Immagine che contiene la parte superiore dello slider del dimmer.
 */
define("IMG_DIMMER_SLIDER_TOP", "images/dimmer-top.png");

/**
 * Immagine che contiene la parte inferiore dello slider del dimmer.
 */
define("IMG_DIMMER_SLIDER_BOTTOM", "images/dimmer-bottom.png");

/**
 * Immagine che contiene un segmento della parte centrale dello slider del 
 * dimmer.
 *
 * <p>Lo "sfondo" dello slider del dimmer sara' questa immagine ripetuta.</p>
 */
define("IMG_DIMMER_SLIDER_MIDDLE", "images/dimmer-sfondo.png");

/**
 * Cursore del dimmer.
 */
define("IMG_DIMMER_CURSOR", "images/dimmer-tasto.png");

/**
 * Controllo delle tapparelle.
 */
define("IMG_BLIND_CONTROL", "images/serrande.png");

/**
 * Coordinata y del bordo superiore del bottone "up".
 */
define("BLIND_UP_TOP", 12);

/**
 * Coordinata x del bordo sinistro dei bottoni "up".
 */
define("BLIND_LEFT", 6);

/**
 * Coordinata y del bordo inferiore del bottone "up".
 */
define("BLIND_UP_BOTTOM", 42);

/**
 * Coordinata x del bordo destro dei bottoni.
 */
define("BLIND_RIGHT", 34);

/**
 * Coordinata y del bordo inferiore del bottone "down".
 */
define("BLIND_DOWN_TOP", 48);

/**
 * Coordinata y del bordo inferiore del bottone "down".
 */
define("BLIND_DOWN_BOTTOM", 78);

// ---------------------------------------------------------
// Costanti per uso interno

define("ILL_LUCE", 0);
define("ILL_DIMMER", 1);


/**
 * Icone varie.
 */
define("IMG_LIGHT_ON", "images/luce_on.png");
define("IMG_LIGHT_OFF", "images/luce_off.png");
define("IMG_POWER_ON", "images/energia_on.png");
define("IMG_POWER_OFF", "images/energia_off.png");
define("IMG_THERMO_ON", "images/clima_on.png");
define("IMG_THERMO_OFF", "images/clima_off.png");
define("IMG_BLIND_STILL", "images/serranda_ferma.png");
define("IMG_BLIND_OPENING", "images/serranda_sale.png");
define("IMG_BLIND_CLOSING", "images/serranda_scende.png");

/**
 * Altezza della status bar [pixel].
 */
define("STATUS_BAR_HEIGHT", 40);

/**
 * Opacita' di default della status bar [0 .. 1].
 */
define("STATUS_BAR_OPACITY", 0.60);


?>