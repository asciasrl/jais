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
$apps = array('illuminazione','clima','sicurezza','video');

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
		"p1a-luce1" => Array(
			"type" => ILL_LUCE,
			"x" => 100,
			"y" => 100,
			"label" => "Applique",
			"address" => "0.3:Out1"),
		"p1a-luce2" => Array(
			"type" => ILL_LUCE,
			"x" => 300,
			"y" => 100,
			"label" => "Luce pitosforo",
			"address" => "0.3:Out2"),
		"p1a-dimmer1" => Array(
			"type" => ILL_DIMMER,
			"x" => 100,
			"y" => 340,
			"label" => "Dimmer allarme",
			"address" => "0.5:Out1"),
		"p1a-dimmer2" => Array(
			"type" => ILL_DIMMER,
			"x" => 300,
			"y" => 340,
			"label" => "Dimmer BMC virtuale",
			"address" => "0.5:Out2")));

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


?>