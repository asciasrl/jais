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
					echo("<div $id style=\"left: " .
						$luce["x"] * $scale . "px; top: " . $luce["y"] * $scale. "px; color: white;\" " .
						"$busaddress $lit name=\"" . $luce["label"] . "\"><div style=\"\"><img src=\"".IMG_LIGHT_OFF.
						"\" alt=\"" . $luce["label"] . "\" width=\"$imgWidth\"
						height=\"$imgHeight\" /></div><div style=\" font-size: $fontSize;\">$text</div></div>");
				} // foreach luce
			} else {
				echo("Non ci sono luci per questo piano!");
			}
			break;
		default:
		// echo("$s<br/>$s<br/>$s<br/>");
		}
?>
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

 


<div id="header" style="margin-top: 9px; height: <?php echo(STATUS_BAR_HEIGHT); ?>px; width: <?php echo (IPOD_VIEWPORT_WIDTH); ?>px; text-align: center;"><b>barra di stato</b></div>

<?php
creaLayerServizi($piani[0], true, true);
?>
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
	const IMG_BLIND_CONTROL = "<?php echo(IMG_BLIND_CONTROL); ?>";
	const BLIND_UP_TOP = <?php echo(BLIND_UP_TOP); ?>;
	const BLIND_LEFT = <?php echo(BLIND_LEFT); ?>;
	const BLIND_UP_BOTTOM = <?php echo(BLIND_UP_BOTTOM); ?>;
	const BLIND_RIGHT = <?php echo(BLIND_RIGHT); ?>;
	const BLIND_DOWN_TOP = <?php echo(BLIND_DOWN_TOP); ?>;
	const BLIND_DOWN_BOTTOM = <?php echo(BLIND_DOWN_BOTTOM); ?>;
	const MAP_AREA_WIDTH = <?php echo(IPOD_VIEWPORT_WIDTH); ?>;
	const MAP_AREA_HEIGHT = <?php echo(IPOD_MAP_AREA_HEIGHT); ?>;
	const IMG_LOCK_OPEN = "<?php echo(IMG_LOCK_OPEN); ?>";
	const IMG_LOCK_CLOSE = "<?php echo(IMG_LOCK_CLOSE); ?>";
	const IMG_DOOR_OPEN = "<?php echo(IMG_DOOR_OPEN); ?>";
	const IMG_DOOR_CLOSE = "<?php echo(IMG_DOOR_CLOSE); ?>";
	const IMG_DOOR_OPEN_ALARM = "<?php echo(IMG_DOOR_OPEN_ALARM); ?>";
	const IMG_DOOR_CLOSE_OK = "<?php echo(IMG_DOOR_CLOSE_OK); ?>";
	pin = "";
</script>
<script type="" language="javascript" src="statusbar.js"></script>
<script type="" language="javascript" src="aui.js"></script>
<script type="" language="javascript" src="comm.js"></script>
<script type="" language="javascript" src="map.js"></script>
<script type="" language="javascript" src="services.js"></script>
<script type="" language="javascript" src="alarm.js"></script>
<script type="" language="javascript">
setInterval("refreshEverything()", 3000);
</script>
