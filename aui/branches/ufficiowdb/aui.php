<?php
/**
 * Copyright (C) 2008 ASCIA S.r.l.
 */

require_once("config.php");
require_once("custom/config.php");

/**
 * Crea un layer per ciascun servizio.
 * 
 * @param $piano il piano.
 * @param $big true se stiamo facendo i layer della mappa grande.
 * @param $clickable true se i servizi devono reagire a click
 */
function creaLayerServizi($piano, $big, $clickable) {
	global $apps, $frameIlluminazione, $frameEnergia, $frameClima, 
		$frameSerramenti, $frameVideo, $frameSicurezza;
	global $idLuci, $idPrese, $idClimi, $idSerramenti, $idVideo, $idAllarmi;
	if ($big) {
		$scale = 1;
	} else {
		// Assumiamo che la mappa piccola e' uguale alla grande rimpicciolita
		$scale = $piano["mapSize"]["w"] / $piano["bigMapSize"]["w"];
	}
	$fontSize = ($scale * 100) . "%";
	foreach ($apps as $s):
		if ($clickable) {
			$idPiano = $piano["id"] . "-big-" . $s; 
		} else {
			$idPiano = $piano["id"] . "-" . $s;
		}
 ?>
			<div id="<?php echo($idPiano); ?>" style="position: absolute; display: none;">
<?php
		switch($s) {
		case "illuminazione":
			$temp = getimagesize(IMG_LIGHT_OFF);
			$imgWidth = $temp[0] * $scale;
			$imgHeight = $temp[1] * $scale;
			if (isset($frameIlluminazione[$piano["id"]])) {
				foreach ($frameIlluminazione[$piano["id"]] as $idLuce => $luce) {
					if ($clickable) {
						$id = "id=\"$idLuce\"";
						if ($luce["type"] == ILL_LUCE) {
							$lit = "lit=\"off\" onClick=\"lightClicked(event, this)\"";
							$text="OFF";
						} else {
							$lit = "lit=\"0\" onClick=\"dimmerClicked(event, this)\"";
							$text="0%";
						}
						$busaddress = "busaddress=\"" . $luce["address"] . "\"";
						$idLuci[] = $idLuce; 
					} else {
						$id = "";
						$lit = "";
						$busaddress = "";
					}
					echo("<div $id style=\"position:absolute; left: " .
						$luce["x"] * $scale . "px; top: " . $luce["y"] * $scale. "px; color: white;\" " .
						"$busaddress $lit name=\"" . $luce["label"] . "\"><div style=\"position: absolute;\"><img src=\"".IMG_LIGHT_OFF.
						"\" alt=\"" . $luce["label"] . "\" width=\"$imgWidth\"
						height=\"$imgHeight\" /></div><div style=\"position: absolute; font-size: $fontSize;\">$text</div></div>");
				} // foreach luce
			} else {
				echo("Non ci sono luci per questo piano!");
			}
			break;
		case "energia":
			$temp = getimagesize(IMG_POWER_OFF);
			$imgWidth = $temp[0] * $scale;
			$imgHeight = $temp[1] * $scale;
			if (isset($frameEnergia[$piano["id"]])) {
				foreach ($frameEnergia[$piano["id"]] as $idPresa => $presa) {
					if ($clickable) {
						$id = "id=\"$idPresa\"";
						$active = "power=\"off\" busaddress=\"" . $presa["address"] . 
							"\" onClick=\"powerClicked(event, this)\"";
						$idPrese[] = $idPresa;
						$text = "OFF";
					} else {
						$id = "";
						$active = "";
						$text = "";
					}
					echo("<div $id style=\"position:absolute; left: " .
						$presa["x"] * $scale. "px; top: " . 
						$presa["y"] * $scale. "px; color: white;\" $active name=\"" . 
							$presa["label"] . "\"><div style=\"position: absolute;\"><img src=\"".
							IMG_POWER_OFF."\" alt=\"" . $presa["label"] .
							"\" width=\"$imgWidth\" height=\"$imgHeight\" /></div><div style=\"position: absolute; font-size: $fontSize;\">$text</div></div>");
				} // foreach presa
			} else {
				echo("Non ci sono prese comandate su questo piano!");
			}
			break;
		case "clima":
			$temp = getimagesize(IMG_THERMO_OFF);
			$imgWidth = $temp[0] * $scale;
			$imgHeight = $temp[1] * $scale;
			if (isset($frameClima[$piano["id"]])) {
				foreach ($frameClima[$piano["id"]] as $idClima => $clima) {
					if ($clickable) {
						$id = "id=\"$idClima\"";
						$active = "power=\"off\" busaddress=\"" . 
							$clima["address"] . "\" onClick=\"thermoClicked(event, this)\"";
						$idClimi[] = $idClima;
						$text = "20&deg;C";
					} else {
						$id = "";
						$active = "";
						$text = "";
					}
					echo("<div $id style=\"position:absolute; left: " .
						$clima["x"] * $scale. "px; top: " . 
						$clima["y"] * $scale . "px; color: white;\" $active name=\"" . 
							$clima["label"] . "\"><div style=\"position: absolute;\"><img src=\"".
							IMG_THERMO_OFF."\" alt=\"" . $clima["label"] .
							"\" width=\"$imgWidth\" height=\"$imgHeight\"/></div><div style=\"position: absolute; font-size: $fontSize;\">$text</div></div>");
				} // foreach clima
			} else {
				echo("Non ci sono termostati su questo piano!");
			}
			break;
		case "serramenti":
			$temp = getimagesize(IMG_BLIND_STILL);
			$imgWidth = $temp[0] * $scale;
			$imgHeight = $temp[1] * $scale;
			if (isset($frameSerramenti[$piano["id"]])) {
				foreach ($frameSerramenti[$piano["id"]] as 
					$idSerramento => $schermo) {
					if ($clickable) {
						$id = "id=\"$idSerramento\"";
						$active = "status=\"still\" addressopen=\"" . 
							$schermo["addressopen"] . 
							"\" addressclose =\"" . $schermo["addressclose"].
							"\" onClick=\"blindClicked(event, this)\"";
						$idSerramenti[] = $idSerramento;
						$text = $schermo["label"];
					} else {
						$id = "";
						$active = "";
						$text = "";
					}
					echo("<div $id style=\"position:absolute; left: " .
						$schermo["x"] * $scale. "px; top: " . 
						$schermo["y"] * $scale . "px; color: white;\" $active name=\"" . 
							$schermo["label"] . "\"><div style=\"position: absolute;\"><img src=\"".
							IMG_BLIND_STILL."\" alt=\"".$schermo["label"]."\" width=\"$imgWidth\" height=\"$imgHeight\"/></div><div style=\"position: absolute; font-size: $fontSize;\"></div></div>");
				} // foreach schermo
			} else {
				echo("Non ci sono serramenti su questo piano!");
			}
			break;
		case "video":
			$temp = getimagesize(IMG_BLIND_STILL);
			$imgWidth = $temp[0] * $scale;
			$imgHeight = $temp[1] * $scale;
			if (isset($frameVideo[$piano["id"]])) {
				foreach ($frameVideo[$piano["id"]] as 
					$idSchermo => $schermo) {
					if ($clickable) {
						$id = "id=\"$idSchermo\"";
						$active = "status=\"still\" addressopen=\"" . 
							$schermo["addressopen"] . 
							"\" addressclose =\"" . $schermo["addressclose"].
							"\" onClick=\"blindClicked(event, this)\"";
						$idVideo[] = $idSchermo;
						$text = $schermo["label"];
					} else {
						$id = "";
						$active = "";
						$text = "";
					}
					echo("<div $id style=\"position:absolute; left: " .
						$schermo["x"] * $scale. "px; top: " . 
						$schermo["y"] * $scale . "px; color: white;\" $active name=\"" . 
							$schermo["label"] . "\"><div style=\"position: absolute;\"><img src=\"".
							IMG_BLIND_STILL."\" alt=\"".$schermo["label"]."\" width=\"$imgWidth\" height=\"$imgHeight\"/></div><div style=\"position: absolute; font-size: $fontSize;\"></div></div>");
				} // foreach schermo
			} else {
				echo("Non ci sono schermi su questo piano!");
			}
			break;
		case "sicurezza":
			$temp = getimagesize(IMG_LOCK_OPEN);
			$imgWidth = $temp[0] * $scale;
			$imgHeight = $temp[1] * $scale;
			if (isset($frameSicurezza[$piano["id"]])) {
				foreach ($frameSicurezza[$piano["id"]] as 
					$idAllarme => $allarme) {
					if ($clickable) {
						$id = "id=\"$idAllarme\"";
						$active = "status=\"open_ok\" onClick=\"alarmClicked(event, this)\"";
						$idAllarmi[] = $idAllarme;
						$text = $allarme["label"];
					} else {
						$id = "";
						$active = "";
						$text = "";
					}
					echo("<div $id style=\"position:absolute; left: " .
						$allarme["x"] * $scale. "px; top: " . 
						$allarme["y"] * $scale . "px; color: white;\" $active name=\"" . 
							$allarme["label"] . "\"><div style=\"position: absolute;\"><img src=\"".
							IMG_LOCK_OPEN."\" alt=\"".$allarme["label"]."\" width=\"$imgWidth\" height=\"$imgHeight\"/></div><div style=\"position: absolute; font-size: $fontSize;\"></div></div>");
				} // foreach allarme
			} else {
				echo("Non ci sono allarmi su questo piano!");
			}
			break;
		default:
		// echo("$s<br/>$s<br/>$s<br/>");
		}
?></div>
<?php
	endforeach;
}

/**
 * Scrive gli elementi di un'array PHP sotto forma di array Javascript.
 * 
 * @param arr array PHP.
 */
function arrayJavascript($arr) {
	echo("[");
	foreach ($arr as $elem) {
		echo("\"$elem\", ");	
	}
	echo("]");
}
 ?>

 

<div id="header-out" style="display: none; position: absolute; z-index: 30; width: <?php echo (IPOD_VIEWPORT_WIDTH); ?>px; height: 40px; filter:alpha(opacity='60'); opacity: <?php echo(STATUS_BAR_OPACITY); ?>;">
<div style="position: absolute;"><img src="images/barratesti.png" /></div>
<div id="header" style="position: absolute; margin-top: 9px; height: <?php echo(STATUS_BAR_HEIGHT); ?>px; width: <?php echo (IPOD_VIEWPORT_WIDTH); ?>px; text-align: center;"><b>barra di stato</b></div>
</div>
<div id="screensaver" title="AUI screensaver - clicca per accedere" onclick="vai('login');"><img
	alt="AUI" src="images/ascia_logo_home.png" /></div>
<div id="login" style="display: none; background: URL(images/groundcalc.png); width: <?php echo (IPOD_VIEWPORT_WIDTH); ?>px; height: <?php echo (IPOD_VIEWPORT_HEIGHT); ?>px;">
	<div id="keypadScreen" style="position: absolute; top: 48px; left: 36px; width: 260px; height: 47px; font-size: 60px; overflow: hidden; text-align: center;">&nbsp;</div> 
	<div style="position: absolute; top: 108px; left: 30px;">
		<table id="keypad" title="AUI login - immetti codice personale e premi OK" summary="keypad" cellpadding="0" cellspacing="0"
			border="0">
			<tr>
				<td><img alt="1" width="68" height="60" border="0"
					src="images/key_1.png" onclick="keypadButton(1)" /></td>
				<td><img alt="2" width="68" height="60" border="0"
					src="images/key_2.png" onclick="keypadButton(2)" /></td>
				<td><img alt="3" width="68" height="60" border="0"
					src="images/key_3.png" onclick="keypadButton(3)" /></td>
				<td><img alt="on" width="68" height="60" border="0"
					src="images/key_on.png" onclick="keypadButton('on')"/></td>
			</tr>
			<tr>
				<td><img alt="4" width="68" height="60" border="0"
					src="images/key_4.png" onclick="keypadButton(4)" /></td>
				<td><img alt="5" width="68" height="60" border="0"
					src="images/key_5.png" onclick="keypadButton(5)" /></td>
				<td><img alt="6" width="68" height="60" border="0"
					src="images/key_6.png" onclick="keypadButton(6)" /></td>
				<td><img onclick="keypadButton('x')" alt="annulla" width="68"
					height="60" border="0" src="images/key_x.png" 
					onclick="keypadButton('X')" /></td>
			</tr>
			<tr>
				<td><img alt="7" width="68" height="60" border="0"
					src="images/key_7.png" onclick="keypadButton(7)" /></td>
				<td><img alt="8" width="68" height="60" border="0"
					src="images/key_8.png" onclick="keypadButton(8)" /></td>
				<td><img alt="9" width="68" height="60" border="0"
					src="images/key_9.png" onclick="keypadButton(9)" /></td>
				<td><img alt="cancella" width="68" height="60" border="0"
					src="images/key_back.png" onclick="keypadButton('back')" /></td>
			</tr>
			<tr>
				<td><img alt="*" width="68" height="60" border="0"
					src="images/key_asterisco.png" onclick="keypadButton('*')"/></td>
				<td><img alt="0" width="68" height="60" border="0"
					src="images/key_0.png" onclick="keypadButton(0)" /></td>
				<td><img alt="#" width="68" height="60" border="0"
					src="images/key_cancelletto.png" onclick="keypadButton('#')" /></td>
				<td><img onclick="keypadButton('ok');" title="OK" alt="ok" width="68"
					height="60" border="0" src="images/key_ok.png" 
					onclick="keypadButton('ok')" /></td>
			</tr>
		</table>
	</div>
</div>
<div id="navigazione" style="display: none;">
  <div id="mappa-out" style="width: <?php echo(IPOD_VIEWPORT_WIDTH); ?>px; height: <?php echo(IPOD_VIEWPORT_HEIGHT - 80); ?>px;">
	<div id="mappa"
		style="position: absolute; width: <?php echo(IPOD_VIEWPORT_WIDTH); ?>px; height: <?php echo(IPOD_VIEWPORT_HEIGHT); ?>px; overflow: hidden;">
		<div id="piani-all" 
			style="position: absolute; width: <?php echo ($pianiSize["w"]); ?>px; height: <?php echo($pianiSize["h"]); ?>px; overflow: hidden;"
			noappbar="noappbar">
			<img 
				header="ASCIA Building"
				title="AUI edificio - clicca su un appartamento" 
				style="position: absolute;"
				onclick="clicca1('piani-all','piano-01A<?php if ($mobile) echo("-big") ?>');"
				src="<?php echo($pianiFile); ?>" alt="" />
		</div>
<?php
foreach ($piani as $piano):
	if (!$mobile) {
 ?>
		<div id="<?php echo($piano["id"]); ?>" 
			style="position: absolute; width: <?php echo($piano["mapSize"]["w"]); ?>px; height: <?php echo($piano["mapSize"]["h"]); ?>px; overflow: hidden; display: none;"
			onclick="ingrandisci(event,'<?php echo($piano["id"]); ?>','<?php echo($piano["id"] . "-big"); ?>','piani-all');">
			<img
				header="<?php echo($piano["header"]); ?>"
				title="AUI mappa appartamento - clicca per ingrandire - doppio click per ritornare"
				style="position: absolute;"
				src="<?php echo($piano["mapFile"]); ?>" alt="" />
			<?php creaLayerServizi($piano, false, false); ?>
		</div>
		<div id="<?php echo($piano["id"] . "-big"); ?>"
			style="position: absolute;
				display: none; 
				width: <?php echo($piano["bigMapSize"]["w"]); ?>px;
				height: <?php echo($piano["bigMapSize"]["h"]); ?>px;"
			onclick="clicca('<?php echo($piano["id"] . "-big"); ?>','<?php echo($piano["id"] . "-big"); ?>','<?php echo($piano["id"]); ?>');">
			<img
				header="<?php echo($piano["header"]); ?>"
				title="AUI appartamento - doppio click per ritornare"
				style="position: absolute;"
				src="<?php echo($piano["bigMapFile"]); ?>" alt="" 
				width="<?php echo($piano["bigMapSize"]["w"]); ?>"
				height="<?php echo($piano["bigMapSize"]["h"]); ?>" />
			<?php creaLayerServizi($piano, true, true); ?>
		</div>
<?php
	} else { // iPod
?>
	<div id="<?php echo($piano["id"] . "-big"); ?>"
		style="position: absolute;
			display: none; 
			width: <?php echo(IPOD_VIEWPORT_WIDTH); ?>px;
			height: <?php echo(IPOD_MAP_AREA_HEIGHT); ?>px;"
		onclick="clicca('<?php echo($piano["id"] . "-big"); ?>','<?php echo($piano["id"] . "-big"); ?>','piani-all');">
		<img
			header="<?php echo($piano["header"]); ?>"
			title="AUI appartamento - doppio click per ritornare"
			style="position: absolute;"
			src="<?php echo($piano["bigMapFile"]); ?>" alt="" 
			width="<?php echo($piano["mapSize"]["w"]); ?>"
			height="<?php echo($piano["mapSize"]["h"]); ?>" />
		<?php creaLayerServizi($piano, false, true); ?>
	</div>
<?php
	} // if iPod
endforeach; // $piani as $piano
 ?>
		<div id="dimmer"
			style="position: absolute; width: <?php echo(IPOD_VIEWPORT_WIDTH); ?>px; height: <?php echo(IPOD_MAP_AREA_HEIGHT); ?>px; 
				overflow: hidden; display: none;" onclick="hideDimmer()">
<?php
$temp = getimagesize(IMG_DIMMER_SLIDER_TOP);
$dimmerWidth = $temp[0] ;
$dimmerTopHeight = $temp[1] ;
$temp = getImageSize(IMG_DIMMER_SLIDER_BOTTOM);
$dimmerBottomHeight = $temp[1] ;
$temp = getImageSize(IMG_DIMMER_CURSOR);
$dimmerCursorWidth = $temp[0] ;
$dimmerCursorHeight = $temp[1] ;
// In pixel
$dimmerCursorTextSize = $dimmerCursorHeight / 3;
$dimmerCursorTextTopMargin = $dimmerCursorHeight / 3;
?>
			<div style="position: absolute; width: 100%; height: 100%; background-color: black; filter:alpha(opacity='80'); opacity: 0.8;">&nbsp;</div>
			<div id="dimmer-slider" style="position: absolute; width: <?php echo($dimmerWidth); ?>px;"
					onclick="dimmerSliderClicked(event)" >
				<div style="position: absolute; height: <?php echo ($dimmerTopHeight); ?>px;"><img src="<?php echo(IMG_DIMMER_SLIDER_TOP); ?>" alt="" width="<?php echo($dimmerWidth); ?>" height="<?php echo($dimmerTopHeight); ?>" /></div>
				<div style="position: absolute; top: <?php echo($dimmerTopHeight); ?>px; width: <?php echo ($dimmerWidth); ?>px; height: <?php echo (DIMMER_SLIDER_HEIGHT - 2 * DIMMER_SLIDER_CORNER_HEIGHT ); ?>px; background-image: URL(<?php echo(IMG_DIMMER_SLIDER_MIDDLE); ?>);"></div>
				<div style="position: absolute; top: <?php echo($dimmerTopHeight + DIMMER_SLIDER_HEIGHT - 2 * DIMMER_SLIDER_CORNER_HEIGHT ); ?>px;"><img src="<?php echo(IMG_DIMMER_SLIDER_BOTTOM); ?>" width="<?php echo($dimmerWidth); ?>" height="<?php echo($dimmerTopHeight); ?>" /></div>
				<div id="dimmer-tasto" style="position: absolute; 
					margin-left: <?php echo(DIMMER_SLIDER_BORDER_WIDTH ); ?>px;"><div style="position: absolute;"><img  src="<?php echo(IMG_DIMMER_CURSOR); ?> " width="<?php echo($dimmerCursorWidth); ?>" height="<?php echo($dimmerCursorHeight); ?>" /></div><div id="dimmer-tasto-testo" style="position: absolute; text-align: center; width: <?php echo($dimmerCursorWidth); ?>; top: <?php echo($dimmerCursorTextTopMargin); ?>px; bottom: auto; font-size: <?php echo($dimmerCursorTextSize); ?>px;"></div>
					</div>
			</div> <!--  dimmer-sfondo -->
		</div><!--  dimmer -->
		<div id="blind"
			style="position: absolute; width: <?php echo(IPOD_VIEWPORT_WIDTH); ?>px; height: <?php echo(IPOD_MAP_AREA_HEIGHT); ?>px; 
				overflow: hidden; display: none;" onclick="blindBackgroundClicked()">
<?php
$temp = getimagesize(IMG_BLIND_CONTROL);
$blindControlWidth = $temp[0];
$blindControlHeight = $temp[1];
?>
			<div style="position: absolute; width: 100%; height: 100%; background-color: black; filter:alpha(opacity='80'); opacity: 0.8;">&nbsp;</div>
			<div id="blind-control" style="position: absolute; width: <?php echo($blindControlWidth); ?>px; height: <?php echo($blindControlHeight); ?>px; background-image: URL(<?php echo(IMG_BLIND_CONTROL); ?>);"
				onclick="blindControlClicked(event)"></div>
		</div><!--  blind -->
	</div> 
	<!-- fine mappa -->
	</div>
	<div id="appbar-out" style="display: none;">
<?php include('appbar.php'); ?>
	</div>
</div>
<script type="" language="javascript">
	const MOBILE = <?php if($mobile) echo "true"; else echo "false"; ?>;
	const ID_LUCI = <?php arrayJavascript($idLuci); ?>;
	const ID_PRESE = <?php arrayJavascript($idPrese); ?>;
	const ID_CLIMI = <?php arrayJavascript($idClimi); ?>;
	const ID_SERRAMENTI = <?php arrayJavascript($idSerramenti); ?>;
	const IMG_LIGHT_ON = "<?php echo(IMG_LIGHT_ON); ?>";
	const IMG_LIGHT_OFF = "<?php echo(IMG_LIGHT_OFF); ?>";
	const IMG_POWER_ON = "<?php echo(IMG_POWER_ON); ?>";
	const IMG_POWER_OFF = "<?php echo(IMG_POWER_OFF); ?>";
	const IMG_THERMO_ON = "<?php echo(IMG_THERMO_ON); ?>";
	const IMG_THERMO_OFF = "<?php echo(IMG_THERMO_OFF); ?>";
	const IMG_BLIND_STILL = "<?php echo(IMG_BLIND_STILL); ?>";
	const IMG_BLIND_OPENING = "<?php echo(IMG_BLIND_OPENING); ?>";
	const IMG_BLIND_CLOSING = "<?php echo(IMG_BLIND_CLOSING); ?>";
	const STATUS_BAR_HEIGHT = <?php echo(STATUS_BAR_HEIGHT); ?>;
	const STATUS_BAR_OPACITY = "<?php echo(STATUS_BAR_OPACITY); ?>";
	const APPBAR_START_POSITION = <?php echo(APPBAR_START_POSITION); ?>;
	const DIMMER_SLIDER_HEIGHT = <?php echo(DIMMER_SLIDER_HEIGHT); ?>;
	const DIMMER_TOP_MIN = <?php echo($dimmerTopHeight - DIMMER_SLIDER_CORNER_HEIGHT ); ?>;
	const DIMMER_TOP_MAX = <?php echo($dimmerTopHeight - DIMMER_SLIDER_CORNER_HEIGHT  + DIMMER_SLIDER_HEIGHT - $dimmerCursorHeight); ?>;
	const DIMMER_CURSOR_MIDDLE = <?php echo($dimmerCursorHeight / 2); ?>;
	const DIMMER_SLIDER_TOTAL_HEIGHT = <?php echo ($dimmerTopHeight + DIMMER_SLIDER_HEIGHT - 2 * DIMMER_SLIDER_CORNER_HEIGHT + $dimmerBottomHeight); ?>;
	const DIMMER_SLIDER_WIDTH = <?php echo($dimmerWidth); ?>;
	const IMG_BLIND_CONTROL = "<?php echo(IMG_BLIND_CONTROL); ?>";
	const BLIND_UP_TOP = <?php echo(BLIND_UP_TOP); ?>;
	const BLIND_LEFT = <?php echo(BLIND_LEFT); ?>;
	const BLIND_UP_BOTTOM = <?php echo(BLIND_UP_BOTTOM); ?>;
	const BLIND_RIGHT = <?php echo(BLIND_RIGHT); ?>;
	const BLIND_DOWN_TOP = <?php echo(BLIND_DOWN_TOP); ?>;
	const BLIND_DOWN_BOTTOM = <?php echo(BLIND_DOWN_BOTTOM); ?>;
	const BLIND_CONTROL_HEIGHT = <?php echo($blindControlHeight); ?>;
	const BLIND_CONTROL_WIDTH = <?php echo($blindControlWidth); ?>;
	const MAP_AREA_WIDTH = <?php echo(IPOD_VIEWPORT_WIDTH); ?>;
	const MAP_AREA_HEIGHT = <?php echo(IPOD_MAP_AREA_HEIGHT); ?>;
	const IMG_LOCK_OPEN = "<?php echo(IMG_LOCK_OPEN); ?>";
	const IMG_LOCK_CLOSE_OK = "<?php echo(IMG_LOCK_CLOSE_OK); ?>";
	const IMG_LOCK_OPEN_ALARM = "<?php echo(IMG_LOCK_OPEN_ALARM); ?>";
</script>
<script type="" language="javascript" src="statusbar.js"></script>
<script type="" language="javascript" src="aui.js"></script>
<script type="" language="javascript" src="comm.js"></script>
<script type="" language="javascript" src="map.js"></script>
<script type="" language="javascript" src="appbar_common.js"></script>
<script type="" language="javascript" src="appbar<?php if (APPBAR_SIMPLE) echo("_simple"); ?>.js"></script>
<script type="" language="javascript" src="services.js"></script>
<script type="" language="javascript" src="dimmer_slider.js"></script>
<script type="" language="javascript" src="blind.js"></script>
<script type="" language="javascript" src="keypad.js"></script>
<script type="" language="javascript" src="alarm.js"></script>
<script type="" language="javascript">
setInterval("refreshEverything()", 3000);
</script>
