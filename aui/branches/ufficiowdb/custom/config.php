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
define(APPBAR_START_POSITION, 40);

/**
 * True se la appbar non deve fare lo scrolling.
 */
define("APPBAR_SIMPLE", true);

/**
 * Lista dei servizi.
 */
$apps = array('illuminazione','serramenti','sicurezza','video');

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
$pianiFile = "custom/images/piani.png"; // images/piani-all.png";
$pianiSize = Array("w" => IPOD_VIEWPORT_WIDTH, "h" => IPOD_VIEWPORT_HEIGHT);

/**
 * Lista dei piani.
 */
$piani = Array(
	Array(
		"id" => "piano-01A",
		"header" => "Piano 1A",
		"mapFile" => "custom/images/pianta-small.png",
		"mapSize" => Array("w" => IPOD_VIEWPORT_WIDTH, "h" => 276),
		"bigMapFile" => "custom/images/pianta.png",
		"bigMapSize" => Array("w" => 640, "h" => 552)));


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
		"p1a-dimmercorsi1" => Array(
			"type" => ILL_DIMMER,
			"x" => 245,
			"y" => 235,
			"label" => "Dimmer aula corsi 1",
			"address" => "0.30:Out1"),
		"p1a-dimmercorsi2" => Array(
			"type" => ILL_DIMMER,
			"x" => 350,
			"y" => 235,
			"label" => "Dimmer aula corsi 2",
			"address" => "0.30:Out2"),
		"p1a-farettocorsi1" => Array(
			"type" => ILL_LUCE,
			"x" => 270,
			"y" => 190,
			"label" => "Faretto 1",
			"address" => "0.6:Out1"),
		"p1a-farettocorsi2" => Array(
			"type" => ILL_LUCE,
			"x" => 322,
			"y" => 190,
			"label" => "Faretto 2",
			"address" => "0.6:Out2"),
		"p1a-farettocorsi3" => Array(
			"type" => ILL_LUCE,
			"x" => 270,
			"y" => 140,
			"label" => "Faretto 3",
			"address" => "0.6:Out3"),
		"p1a-farettocorsi4" => Array(
			"type" => ILL_LUCE,
			"x" => 322,
			"y" => 140,
			"label" => "Faretto 6",
			"address" => "0.6:Out7"),
		"p1a-farettocorsi5" => Array(
			"type" => ILL_LUCE,
			"x" => 270,
			"y" => 90,
			"label" => "Faretto 5",
			"address" => "0.6:Out5"),
		"p1a-farettocorsi6" => Array(
			"type" => ILL_LUCE,
			"x" => 322,
			"y" => 90,
			"label" => "Faretto 6",
			"address" => "0.6:Out6")));


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
$frameEnergia = Array( /*
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
			"address" => "0.5:Out5"))*/);

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
$frameClima =  Array(
	"piano-01A" => Array(/*
		"p1a-clima1" => Array(
			"x" => 300,
			"y" => 200,
			"label" => "Termostato a vapore",
			"address" => "0.3:Out6"),
		"p1a-clima2" => Array(
			"x" => 400,
			"y" => 400,
			"label" => "Bruciatore",
			"address" => "0.3:Out7")*/));

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
			"x" => 250,
			"y" => 40,
			"label" => "Tapparella 1",
			"addressopen" => "0.1:Out1",
			"addressclose" => "0.1:Out2"),
	"p1a-serr2" => Array(
			"x" => 330,
			"y" => 40,
			"label" => "Tapparella 2",
			"addressopen" => "0.1:Out3",
			"addressclose" => "0.1:Out4"),
	"p1a-serr3" => Array(
			"x" => 250,
			"y" => 80,
			"label" => "Tapparella 3",
			"addressopen" => "0.1:Out5",
			"addressclose" => "0.1:Out6"),
	"p1a-serr4" => Array(
			"x" => 330,
			"y" => 80,
			"label" => "Tapparella 4",
			"addressopen" => "0.1:Out5",
			"addressclose" => "0.1:Out6")));

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

?>