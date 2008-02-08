<?php

/**
 * Lista dei servizi.
 */
$apps = array('audio','clima','energia','illuminazione','serramenti','sicurezza','video'); 

/**
 * Lista dei piani.
 */
$piani = Array(
	Array(
		"id" => "piano-01A",
		"header" => "Piano 1A",
		"mapFile" => "images/piano-01A.png",
		"bigMapFile" => "images/piano-01A-big.png",
		"bigMapSize" => Array("w" => 720, "h" => 720)));

	define("ILL_LUCE", 0);
	define("ILL_DIMMER", 1);
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
			"address" => "0.3:1"),
		"p1a-luce2" => Array(
			"type" => ILL_LUCE,
			"x" => 300,
			"y" => 100,
			"label" => "Luce pitosforo",
			"address" => "0.3:2"),
		"p1a-dimmer1" => Array(
			"type" => ILL_DIMMER,
			"x" => 100,
			"y" => 300,
			"label" => "Luce dimmerizzata",
			"address" => "0.5:0")));
 ?>
<div style="position: absolute; z-index: 30; width: 320px; height: 40px; filter:alpha(opacity='60'); opacity: 0.60;">
<div style="position: absolute;"><img src="images/barratesti.png" /></div>
<div id="header" style="position: absolute; margin-top: 9px; height: 22px; width: 320px; text-align: center;"><b>barra di stato</b></div>
</div>
<div id="screensaver" title="AUI screensaver - clicca per accedere" onclick="vai('login');"><img
	alt="aree schermo" src="images/aree-schermo2.jpg" /></div>
<div id="login" style="display: none;">
<table id="keypad" title="AUI login - immetti codice personale e premi OK" summary="keypad" cellpadding="0" cellspacing="0"
	border="0">
	<tr>
		<td colspan="4" style="text-align: center;"><input 
		id="keypadScreen" type="text" size="8" disabled="disabled"><!-- <img alt="panel" width="240" height="70"
			border="0" src="images/key-panel.png" /> --></td>
	</tr>
	<tr>
		<td><img alt="1" width="60" height="48" border="0"
			src="images/key01.png" onclick="keypadButton(1)" /></td>
		<td><img alt="2" width="60" height="48" border="0"
			src="images/key02.png" onclick="keypadButton(2)" /></td>
		<td><img alt="3" width="60" height="48" border="0"
			src="images/key03.png" onclick="keypadButton(3)" /></td>
		<td><img alt="qwerty" width="60" height="48" border="0"
			src="images/qwerty.png" onclick="keypadButton('qwerty')"/></td>
	</tr>
	<tr>
		<td><img alt="4" width="60" height="48" border="0"
			src="images/key04.png" onclick="keypadButton(4)" /></td>
		<td><img alt="5" width="60" height="48" border="0"
			src="images/key05.png" onclick="keypadButton(5)" /></td>
		<td><img alt="6" width="60" height="48" border="0"
			src="images/key06.png" onclick="keypadButton(6)" /></td>
		<td><img onclick="keypadButton('x')" alt="annulla" width="60"
			height="48" border="0" src="images/annulla.png" 
			onclick="keypadButton('X')" /></td>
	</tr>
	<tr>
		<td><img alt="7" width="60" height="48" border="0"
			src="images/key07.png" onclick="keypadButton(7)" /></td>
		<td><img alt="8" width="60" height="48" border="0"
			src="images/key08.png" onclick="keypadButton(8)" /></td>
		<td><img alt="9" width="60" height="48" border="0"
			src="images/key09.png" onclick="keypadButton(9)" /></td>
		<td><img alt="cancella" width="60" height="48" border="0"
			src="images/back.png" onclick="keypadButton('back')" /></td>
	</tr>
	<tr>
		<td><img alt="*" width="60" height="48" border="0"
			src="images/key-asterisk.png" onclick="keypadButton('*')"/></td>
		<td><img alt="0" width="60" height="48" border="0"
			src="images/key00.png" onclick="keypadButton(0)" /></td>
		<td><img alt="#" width="60" height="48" border="0"
			src="images/key-sharp.png" onclick="keypadButton('#')" /></td>
		<td><img onclick="keypadButton('ok');" title="OK" alt="ok" width="60"
			height="48" border="0" src="images/ok.png" 
			onclick="keypadButton('ok')" /></td>
	</tr>
	<tr>
		<td colspan="4"><img alt="footer" width="240" height="58"
			border="0" src="images/footer.png" /></td>
	</tr>
</table>
</div>
<div id="navigazione" style="display: none;">
  <div id="mappa-out" style="width: 320px; height: 380px;">
	<div id="mappa"
		style="position: absolute; width: 240px; height: 240px; overflow: hidden;">
		<div id="piani-all" 
			style="position: absolute; width: 240px; height: 240px; overflow: hidden;">
			<img 
				header="ASCIA Building"
				title="AUI edificio - clicca su un appartamento" 
				style="position: absolute;"
				onclick="clicca1('piani-all','piano-01A');"
				src="images/piani-all.png" alt="" />
		</div>
<?php
foreach ($piani as $piano):
 ?>
		<div id="<?php echo($piano["id"]); ?>" 
			style="position: absolute; width: 240px; height: 240px; overflow: hidden; display: none;">
			<img
				header="<?php echo($piano["header"]); ?>"
				title="AUI mappa appartamento - clicca per ingrandire - doppio click per ritornare"
				style="position: absolute;"
				onclick="ingrandisci(event,'<?php echo($piano["id"]); ?>','<?php echo($piano["id"] . "-big"); ?>','piani-all');"
				src="<?php echo($piano["mapFile"]); ?>" alt="" />
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
<?php
	// Crea un layer per ciascun servizio
	foreach ($apps as $s):
 ?>
			<div id="<?php echo($piano["id"] . "-big-" . $s); ?>"
				style="position: absolute; display: none;">
<?php
		switch($s) {
		case "illuminazione":
			if (isset($frameIlluminazione[$piano["id"]])) {
				foreach ($frameIlluminazione[$piano["id"]] as $idLuce => $luce) {
					echo("<div id=\"$idLuce\" style=\"position:absolute; left: " .
						$luce["x"] . "px; top: " . $luce["y"] . "px;\" lit=\"no\" " .
						"busaddress=\"" . $luce["address"] . "\"");
					if ($luce["type"] == ILL_LUCE) {
						echo("onClick=\"lightClicked(this)\"");
					} else {
						echo("onClick=\"dimmerClicked(this)\"");
					}
					echo("><img src=\"images/luce_off.png\" alt=\"" . $luce["label"] .
						"\" /></div>");
				} // foreach luce
			} else {
				echo("Non ci sono luci per questo piano!");
			}
			break;
		default:
		echo("$s<br/>$s<br/>$s<br/>");
		}
?></div>
<?php
	endforeach;
 ?>
		</div> <!-- FIXME: bisogna dare le dimensioni al <div>-->
<?php
endforeach; // $piani as $piano
 ?>
	</div>
	<!-- fine mappa -->
  </div>
  <div id="appbar-out">
<?php include('appbar.php'); ?>
  </div>
</div>
<script type="" language="javascript" src="aui.js"></script>
<script type="" language="javascript" src="comm.js"></script>
<script type="" language="javascript" src="appbar.js"></script>
<script type="" language="javascript" src="keypad.js"></script>
