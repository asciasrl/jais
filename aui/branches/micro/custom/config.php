<?php
/**
 * Copyright (C) 2008 ASCIA S.r.l.
 *
 * <p>Questo file contiene le informazioni di configurazione di AUI
 * personalizzate per un cliente.</p>
 */

/**
 * Posizione iniziale della appBar [pixel].
 *
 * <p>Se la appBar non scorre, questo numero deve indicare la posizione della
 * icona da selezionare all'avvio di AUI.</p>
 */
define("APPBAR_START_POSITION", 360);

/**
 * True se la appbar non deve fare lo scrolling.
 */
define("APPBAR_SIMPLE", false);

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
$pianiFile = "custom/images/assonometria320x370.png"; // images/piani-all.png";
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
		"p1a-dimmer1" => Array(
			"type" => ILL_LUCE,
			"x" => 100,
			"y" => 340,
			"label" => "Luce 3",
			"address" => "0.2:Out3"),
		"p1a-dimmer2" => Array(
			"type" => ILL_LUCE,
			"x" => 300,
			"y" => 340,
			"label" => "Luce 4",
			"address" => "0.2:Out4")));

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
$frameEnergia = Array(
	"piano-01A" => Array(
		"p1a-presa1" => Array(
			"x" => 200,
			"y" => 200,
			"label" => "Presa 1",
			"address" => "0.3:Out3"),
		"p1a-presa2" => Array(
			"x" => 400,
			"y" => 200,
			"label" => "Presa pitosforo",
			"address" => "0.3:Out4"),
		"p1a-presa3" => Array(
			"x" => 200,
			"y" => 400,
			"label" => "Presa lavatrice",
			"address" => "0.5:Out5")));

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
$frameClima = Array(
	"piano-01A" => Array(
		"p1a-clima1" => Array(
			"x" => 300,
			"y" => 200,
			"label" => "Termostato a vapore",
			"address" => "0.3:Out6"),
		"p1a-clima2" => Array(
			"x" => 400,
			"y" => 400,
			"label" => "Bruciatore",
			"address" => "0.3:Out7")));


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
$frameSerramenti = Array(
	"piano-01A" => Array(
		"p1a-serr1" => Array(
			"x" => 460,
			"y" => 40,
			"label" => "Tapparella verde",
			"addressopen" => "0.3:Out1",
			"addressclose" => "0.3:Out2")));

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
$frameVideo = Array(
	"piano-01A" => Array(
		"p1a-schermo1" => Array(
			"x" => 295,
			"y" => 70,
			"label" => "Schermo",
			"addressopen" => "0.5:Out1",
			"addressclose" => "0.5:Out2")));


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
$frameSicurezza = Array(
	"piano-01A" => Array(
		"p1a-porta1" => Array(
			"type" => SIC_PORTA, 
			"x" => 525,
			"y" => 325,
			"label" => "Porta"),
		"p1a-allarme1" => Array(
			"type" => SIC_LUCCHETTO, 
			"x" => 365,
			"y" => 342,
			"label" => "Allarme")));

?>