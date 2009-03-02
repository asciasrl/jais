<%!
/**
 * Copyright (C 2008 ASCIA S.r.l.
 *
 * <p>Questo file contiene le informazioni di configurazione di default per 
 * AUI.</p>
 *
 * <p>Queste informazioni devono essere integrate (eventualmente anche
 * sovrascritte dal file custom/config.php.</p>
 */

/**
 * Larghezza della "viewport" dell'iPod Touch. [pixel]
 */
final int IPOD_VIEWPORT_WIDTH =  320;

/**
 * Altezza della "viewport" dell'iPod Touch. [pixel]
 */
final int IPOD_VIEWPORT_HEIGHT =  356;

/**
 * Altezza dell'area in cui si visualizza la mappa. [pixel]
 */
final int IPOD_MAP_AREA_HEIGHT =  276; 

/**
 * Altezza della "parte stondata" degli estremi dello slider del dimmer [pixel].
 *
 * <p>Gli estremi del dimmer hanno gli angoli arrotondati. Questa e' l'altezza
 * di tali angoli = che stanno disegnati dentro l'immagine degli estremi dello
 * slider.</p>
 */
final int DIMMER_SLIDER_CORNER_HEIGHT =  5;

/**
 * Larghezza del bordo dello slider del dimmer.
 *
 * <p>Lo slider del dimmer ha un bordo che deve essere simmetrico rispetto
 * all'asse verticale. La sua larghezza in pixel e' questa.</p>
 */
final int DIMMER_SLIDER_BORDER_WIDTH =  6;

/**
 * Immagine che contiene la parte superiore dello slider del dimmer.
 */
final String IMG_DIMMER_SLIDER_TOP =  "images/dimmer-top.png";
/**
 * Larghezza di IMG_DIMMER_SLIDER_TOP (e quindi di tutto il dimmer) [pixel].
 */
final int DIMMER_WIDTH = 40;
/**
 * Altezza di IMG_DIMMER_SLIDER_TOP [pixel].
 */
final int IMG_DIMMER_SLIDER_TOP_HEIGHT = 17;

/**
 * Immagine che contiene la parte inferiore dello slider del dimmer.
 */
final String IMG_DIMMER_SLIDER_BOTTOM =  "images/dimmer-bottom.png";
/**
 * Altezza di IMG_DIMMER_SLIDER_BOTTOM [pixel].
 */
final int IMG_DIMMER_SLIDER_BOTTOM_HEIGHT = 17;

/**
 * Immagine che contiene un segmento della parte centrale dello slider del 
 * dimmer.
 *
 * <p>Lo "sfondo" dello slider del dimmer sara' questa immagine ripetuta.</p>
 */
final String IMG_DIMMER_SLIDER_MIDDLE =  "images/dimmer-sfondo.png";

/**
 * Cursore del dimmer.
 */
final String IMG_DIMMER_CURSOR =  "images/dimmer-tasto.png";
/**
 * Larghezza del cursore del dimmer [pixel].
 */
final int IMG_DIMMER_CURSOR_WIDTH = 28;
/**
 * Altezza del cursore del dimmer [pixel].
 */
final int IMG_DIMMER_CURSOR_HEIGHT = 30;

/**
 * Controllo delle tapparelle.
 */
final String IMG_BLIND_CONTROL =  "images/serrande.png";
/**
 * Larghezza di IMG_BLIND_CONTROL [pixel].
 */
final int IMG_BLIND_CONTROL_WIDTH = 40;
/**
 * Altezza di IMG_BLIND_CONTROL [pixel].
 */
final int IMG_BLIND_CONTROL_HEIGHT = 90;

/**
 * Coordinata y del bordo superiore del bottone "up".
 */
final int BLIND_UP_TOP =  12;

/**
 * Coordinata x del bordo sinistro dei bottoni "up".
 */
final int BLIND_LEFT =  6;

/**
 * Coordinata y del bordo inferiore del bottone "up".
 */
final int BLIND_UP_BOTTOM =  42;

/**
 * Coordinata x del bordo destro dei bottoni.
 */
final int BLIND_RIGHT =  34;

/**
 * Coordinata y del bordo inferiore del bottone "down".
 */
final int BLIND_DOWN_TOP =  48;

/**
 * Coordinata y del bordo inferiore del bottone "down".
 */
final int BLIND_DOWN_BOTTOM =  78;

// ---------------------------------------------------------
// Costanti per uso interno

final int ILL_LUCE =  0;
final int ILL_DIMMER =  1;
final int SIC_LUCCHETTO =  0;
final int SIC_PORTA =  1;


/**
 * Icone varie.
 *
 * <p>Queste icone devono essere tutte quadrate, con lato MAP_ICON_WIDTH.</p>
 */
final String IMG_LIGHT_ON =  "images/luce_on.png";
final String IMG_LIGHT_OFF =  "images/luce_off.png";
final String IMG_POWER_ON =  "images/energia_on.png";
final String IMG_POWER_OFF =  "images/energia_off.png";
final String IMG_THERMO_ON =  "images/clima_on.png";
final String IMG_THERMO_OFF =  "images/clima_off.png";
final String IMG_BLIND_STILL =  "images/serranda_ferma.png";
final String IMG_BLIND_OPENING =  "images/serranda_sale.png";
final String IMG_BLIND_CLOSING =  "images/serranda_scende.png";
final String IMG_LOCK_OPEN =  "images/allarm_off.png";
final String IMG_LOCK_CLOSE =  "images/allarm_on.png";
final String IMG_DOOR_OPEN =  "images/porta_aperta.png";
final String IMG_DOOR_CLOSE =  "images/porta_chiusa.png";
final String IMG_DOOR_OPEN_ALARM =  "images/porta_aperta_red.png";
final String IMG_DOOR_CLOSE_OK =  "images/porta_chiusa_green.png";
final String IMG_SCENARIOS =  "images/scenari_all.png";
final int MAP_ICON_WIDTH = 60;

/**
 * Altezza della status bar [pixel].
 */
final int STATUS_BAR_HEIGHT =  40;

/**
 * Opacita' di default della status bar [0 .. 1].
 */
final double STATUS_BAR_OPACITY =  0.60;


%>