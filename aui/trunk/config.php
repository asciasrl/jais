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
define("DIMMER_SLIDER_CORNER_HEIGHT", 7);

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
 * Icone varie.
 */
define("IMG_LIGHT_ON", "images/light-on.jpg");
define("IMG_LIGHT_OFF", "images/light-off2.jpg");
define("IMG_POWER_ON", "images/energia_on.png");
define("IMG_POWER_OFF", "images/energia_off.png");
define("IMG_THERMO_ON", "images/clima_on.png");
define("IMG_THERMO_OFF", "images/clima_off.png");

/**
 * Altezza della status bar [pixel].
 */
define("STATUS_BAR_HEIGHT", 22);

/**
 * Opacita' di default della status bar [0 .. 1].
 */
define("STATUS_BAR_OPACITY", 0.60);

?>