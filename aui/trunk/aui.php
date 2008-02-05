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
</div>
<script type="" language="javascript" src="aui.js"></script>
<script type="" language="javascript" src="keypad.js"></script>
<?php include('appbar.php'); ?>	