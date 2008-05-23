<?php
/**
 * Copyright (C) 2008 ASCIA S.r.l.
 *
 * <p>Questo file contiene le informazioni di configurazione di AUI
 * personalizzate per un cliente.</p>
 */

/**
 * Periodo di auto-refresh [msec].
 *
 * <p>Se non impostato, l'auto-refresh viene disattivato.</p>
 */
$refreshInterval = 3000;

/**
 * Mostra lo screensaver.
 *
 * <p>Questo è ovviamente necessario se il server richiede un PIN!</p>
 */
$showScreenSaver = false;

/**
 * Mostra il keypad per l'inserimento del pin.
 *
 * <p>Questo è ovviamente necessario se il server richiede un PIN!</p>
 */
$showKeypad = false;

/**
 * Posizione iniziale della appBar [pixel].
 *
 * <p>Se la appBar non scorre, questo numero deve indicare la posizione della
 * icona da selezionare all'avvio di AUI.</p>
 */
define(APPBAR_START_POSITION, 360);

/**
 * True se la appbar non deve fare lo scrolling.
 */
define("APPBAR_SIMPLE", true);

/**
 * Lista dei servizi.
 */
$apps = array('illuminazione');

/**
 * Altezza della "parte utile" dello slider del dimmer [pixel].
 *
 * <p>Questa deve essere l'altezza dentro la quale si puo' spostare il cursore
 * del dimmer.</p>
 */
define("DIMMER_SLIDER_HEIGHT", 100);

/**
 * Immagine che mostra i piani.
 */
// $pianiFile = "custom/images/assonometria320x370.png"; // images/piani-all.png";
$pianiSize = Array("w" => IPOD_VIEWPORT_WIDTH, "h" => 370); // Array("w" => 240, "h" => 240);

/**
 * Lista dei piani.
 */
$piani = Array(
	Array(
		"id" => "piano-01A",
		"header" => "Piano 1A",
		"mapFile" => "custom/images/planimetria.png",
		"mapSize" => Array("w" => IPOD_VIEWPORT_WIDTH, "h" => 299),
		"bigMapFile" => "custom/images/planimetria-big.png",
		"bigMapSize" => Array("w" => 800, "h" => 748)));

/* Vecchi piani
$piani = Array(
	Array(
		"id" => "piano-01A",
		"header" => "Piano 1A",
		"mapFile" => "images/piano-01A.png",
		"bigMapFile" => "images/piano-01A-big.png",
		"bigMapSize" => Array("w" => 720, "h" => 720)));
*/

/**
 * Luci presenti nel sistema.
 * 
 * <p>Questa array di ID verra' popolata in fase di creazione delle luci.</p>
 */
$idLuci = Array();

/**
 * Frame: illuminazione.
 * 
 * <p>Gli indici sono gli ID dei piani.</p>
 */
$frameIlluminazione = Array(
	"piano-01A" => Array(
		"p1a-luce1" => Array(
			"type" => ILL_LUCE,
			"x" => 100,
			"y" => 100,
			"label" => "Luce 1",
			"address" => "0.2:Out1"),
		"p1a-luce2" => Array(
			"type" => ILL_LUCE,
			"x" => 300,
			"y" => 100,
			"label" => "Luce 2",
			"address" => "0.2:Out2"),
		"p1a-luce3" => Array(
			"type" => ILL_LUCE,
			"x" => 100,
			"y" => 340,
			"label" => "Luce 3",
			"address" => "0.2:Out3"),
		"p1a-luce4" => Array(
			"type" => ILL_LUCE,
			"x" => 300,
			"y" => 340,
			"label" => "Luce 4",
			"address" => "0.2:Out4"),
		"p1a-luce5" => Array(
			"type" => ILL_LUCE,
			"x" => 400,
			"y" => 340,
			"label" => "Luce 5",
			"address" => "0.2:Out5"),
		"p1a-luce6" => Array(
			"type" => ILL_LUCE,
			"x" => 340,
			"y" => 400,
			"label" => "Luce 6",
			"address" => "0.2:Out6"),
		"p1a-luce7" => Array(
			"type" => ILL_LUCE,
			"x" => 500,
			"y" => 340,
			"label" => "Luce 7",
			"address" => "0.2:Out7")));

/**
 * Prese comandate presenti nel sistema.
 * 
 * <p>Questa array di ID verra' popolata in fase di creazione delle prese
 * comandate.</p>
 */
$idPrese = Array();

/**
 * Frame: energia.
 * 
 * <p>Gli indici sono gli ID dei piani.</p>
 */
$frameEnergia = Array();

/**
 * Termostati presenti nel sistema.
 * 
 * <p>Questa array di ID verra' popolata in fase di creazione delle prese
 * comandate.</p>
 */
$idClimi = Array();

/**
 * Frame: clima.
 * 
 * <p>Gli indici sono gli ID dei piani.</p>
 */
$frameClima = Array();


/**
 * Tapparelle collegate al sistema.
 * 
 * <p>Questa array di ID verra' popolata in fase di creazione delle 
 * tapparelle.</p>
 */
$idSerramenti = Array();

/**
 * Frame: serramenti.
 * 
 * <p>Gli indici sono gli ID dei piani.</p>
 */
$frameSerramenti = Array();

/**
 * Schermi e altre cose "video" collegate al sistema.
 * 
 * <p>Questa array di ID verra' popolata in fase di creazione degli elementi 
 * del layer "video".</p>
 */
$idVideo = Array();

/**
 * Frame: video.
 * 
 * <p>Gli indici sono gli ID dei piani.</p>
 */
$frameVideo = Array();


/**
 * Allarmi gestiti dal sistema.
 * 
 * <p>Questa array di ID verra' popolata in fase di creazione degli elementi 
 * del layer "sicurezza".</p>
 */
$idAllarmi = Array();

/**
 * Frame: sicurezza.
 * 
 * <p>Gli indici sono gli ID dei piani.</p>
 */
$frameSicurezza = Array();

?>