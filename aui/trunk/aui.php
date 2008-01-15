<div id="screensaver" title="AUI screensaver - clicca per accedere" onclick="vai('login');"><img
	alt="aree schermo" src="images/aree-schermo2.jpg" /></div>
<div id="login" style="display: none;">
<table id="keypad" title="AUI login - immetti codice personale e premi OK" summary="keypad" cellpadding="0" cellspacing="0"
	border="0">
	<tr>
		<td colspan="4"><img alt="panel" width="240" height="70"
			border="0" src="images/key-panel.png" /></td>
	</tr>
	<tr>
		<td><img alt="1" width="60" height="48" border="0"
			src="images/key01.png" /></td>
		<td><img alt="2" width="60" height="48" border="0"
			src="images/key02.png" /></td>
		<td><img alt="3" width="60" height="48" border="0"
			src="images/key03.png" /></td>
		<td><img alt="qwerty" width="60" height="48" border="0"
			src="images/qwerty.png" /></td>
	</tr>
	<tr>
		<td><img alt="4" width="60" height="48" border="0"
			src="images/key04.png" /></td>
		<td><img alt="5" width="60" height="48" border="0"
			src="images/key05.png" /></td>
		<td><img alt="6" width="60" height="48" border="0"
			src="images/key06.png" /></td>
		<td><img onclick="vai('screensaver');" alt="annulla" width="60"
			height="48" border="0" src="images/annulla.png" /></td>
	</tr>
	<tr>
		<td><img alt="7" width="60" height="48" border="0"
			src="images/key07.png" /></td>
		<td><img alt="8" width="60" height="48" border="0"
			src="images/key08.png" /></td>
		<td><img alt="9" width="60" height="48" border="0"
			src="images/key09.png" /></td>
		<td><img alt="cancella" width="60" height="48" border="0"
			src="images/back.png" /></td>
	</tr>
	<tr>
		<td><img alt="*" width="60" height="48" border="0"
			src="images/key-asterisk.png" /></td>
		<td><img alt="0" width="60" height="48" border="0"
			src="images/key00.png" /></td>
		<td><img alt="#" width="60" height="48" border="0"
			src="images/key-sharp.png" /></td>
		<td><img onclick="vai('navigazione');" title="OK" alt="ok" width="60"
			height="48" border="0" src="images/ok.png" /></td>
	</tr>
	<tr>
		<td colspan="4"><img alt="footer" width="240" height="58"
			border="0" src="images/footer.png" /></td>
	</tr>
</table>
</div>
<div id="navigazione" style="display: none;">
<div style="height: 20px;">
<table id="top" summary="top" cellpadding="0" cellspacing="0" border="0">
	<tr>
		<td title="Menu"><img onclick="vai('login');" alt="menu" xwidth="40"
			xheight="60" border="0" src="images/infomenu-sx.png" /></td>
		<td title="Status bar" width="180">
		<div id="header" style="width: 180px; height: 20px; overflow: hidden; font-size: small; text-align: center;">ASCIA User Interface</div>
		</td>
		<td title="Exit"><img onclick="vai('screensaver');" alt="exit" xwidth="40"
			xheight="60" border="0" src="images/exit-dx.png" /></td>
	</tr>
</table>
</div>
<div style="width: 240px; height: 240px;">
<div id="mappa"
	style="position: absolute; width: 240px; height: 240px; overflow: hidden;">
<img 
	header="ASCIA Building"
	title="AUI edificio - clicca su un appartamento" 
	style="position: absolute;"
	onclick="clicca1('piani-all','piano-01A');"
	id="piani-all"	src="images/piani-all.png" alt="" />
<img
	header="Piano 1A"
	title="AUI mappa appartamento - clicca per ingrandire - doppio click per ritornare"
	style="position: absolute; display: none;"
	onclick="ingrandisci(event,'piano-01A','piano-01A-big','piani-all');"
	id="piano-01A" src="images/piano-01A.png" alt="" />
<img
	header="Piano 1A"
	title="AUI appartamento - doppio click per ritornare"
	style="position: absolute; display: none;"
	onclick="clicca('piano-01A-big','piano-01A-big','piano-01A');" id="piano-01A-big"
	src="images/piano-01A-big.png" alt="" /></div>
</div>
<div style="height: 60px;">
<table id="servizi" summary="servizi" title="AUI servizi - clicca per scegliere cosa gestire" cellpadding="0" cellspacing="0"
	border="0">
<?php
$funzioni = array('clima','serramenti','sicurezza','luci','energia');
$estensioni = array('clima' => 'jpg','serramenti'=>'jpg','sicurezza'=>'png','luci'=>'png','energia'=>'png');
foreach ($funzioni as $i => $f):
  $ext = $estensioni[$f];
?>	
	<tr id="<?php echo($f); ?>" <?php if ($i > 0): ?>style="display: none;"<?php endif; ?>>
	<?php for ($d = -2 ; $d <= 2 ; $d++):
		$j = $i + $d;
		if ($j < 0) {
		  $k = $j + sizeof($funzioni);
		} elseif ($j >= sizeof($funzioni)) {
		  $k = $j - sizeof($funzioni);
		} else {
		  $k = $j;
		}
		$to = $funzioni[$k];
		switch ($d) {
		  case -2:
		    $m = '-sx2';
		    $w = 40;
		    break;
		  case -1:
		    $m = '-sx1';
		    $w = 50;
		    break;
		  case 0:
		    $m = '';
		    $w = 60;
		    break;
		  case +1:
		    $m = '-dx1';
		    $w = 50;
		    break;
		  case +2:
		    $m = '-dx2';
		    $w = 40;
		    break;
		}
		$img = "images/" . $to . $m . '.' . $estensioni[$to]; 
	?>
		<td><img onclick="clicca1('<?php echo($f); ?>','<?php echo($funzioni[$k]); ?>');" alt="luci"
			width="<?php echo($w); ?>" height="60" border="0" src="<?php echo($img); ?>" /></td>
	<?php endfor; ?>
	</tr>
<?php endforeach; ?>
</table>
</div>
</div>

<script type="" language="javascript" src="aui.js"></script>
