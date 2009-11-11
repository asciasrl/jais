<!-- (C) Copyright 2009 Ascia S.r.l. -->
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>AUI Configurazione</title>
<script type="text/javascript" src="JSCookMenu/JSCookMenu.js"></script>
<link rel="stylesheet" href="JSCookMenu/ThemeOffice2003/theme.css" type="text/css">
<script type="text/javascript" src="JSCookMenu/ThemeOffice2003/theme.js"></script>

<script language="javascript" src="classes/mootools-1.2.3-core-nc.js"></script>
<script language="javascript" src="classes/mootools-1.2.3.1-more.js"></script>
<script language="javascript" src="classes/jsonrpc.js"></script>
<script language="javascript" src="classes/AUI.js"></script>
<script language="javascript" src="classes/Logger.js"></script>
<script language="javascript" src="classes/Upload.js"></script>
<script language="javascript" src="classes/Config.js"></script>

<link href="skins/20090330/css/aui.css" rel="stylesheet" type="text/css"/>
<link href="skins/20090330/css/config.css" rel="stylesheet" type="text/css"/>

</head>
<body onload="AUI.Config.init();">

<div id="menubar">
	<div id="mainmenu"></div>
</div>

<div id="statusbar">Nessuna configurazione aperta.</div>

<div id="pages">Nessuna pagina</div>

<div id="controls">Nessun controllo</div>

<div id="page">Selezionare la pagina da modificare</div>

<div id="footer">
&copy; 2009 <a href="http://www.ascia.it/" target="_blank">ASCIA S.r.l. </a>
</div>

<div id="mask"></div>

<script type="text/javascript"><!--

function resize() {
	var wp = $("pages").getSize().x;
	var wc = $("controls").getSize().x;
	var h = (window.innerHeight - 21 - 20 - 1 - 20 - 20 - 1 - 17) + "px";
	var w = (window.innerWidth - wp - wc - 20 - 20) + "px";
	document.getElementById("pages").style.height = h;
	document.getElementById("page").style.height = h;
	document.getElementById("page").style.width = w;
	document.getElementById("controls").style.height = h;
}

resize();
window.onresize = resize;


var skinImages = "skins/20090330/config.images/";
var themeImages = "JSCookMenu/ThemeOffice/";
var myMenu =
[
	['<img src="'+skinImages+'aui_icon.png" width="18" height="16" alt="AUI" title="Ascia User Interface" />', '', null, null, null,
		[null, 'Informazioni su Aui', '', null, null]
	],		
	[null, 'File', null, null, null,
		['<img class="seq1" src="'+themeImages+'open.gif" /><img class="seq2" src="'+themeImages+'openshadow.gif" />', 'Apri configurazione', 'javascript:AUI.Config.cmdOpen();', null, null],
		[null, 'Chiudi', 'javascript: AUI.Config.cmdClose();',null,null],
		_cmSplit,
		['<img class="seq1" src="'+themeImages+'save.gif" /><img class="seq2" src="'+themeImages+'saveshadow.gif" />', 'Salva', 'javascript:AUI.Config.cmdSave();', null, null],
		['<img class="seq1" src="'+themeImages+'save.gif" /><img class="seq2" src="'+themeImages+'saveshadow.gif" />', 'Salva con nome...', 'javascript:AUI.Config.cmdSaveAs();', null, null],
		_cmSplit,
		[null, 'Anteprima', '/index.jsp', '_blank', 'Apre anteprima in una nuova finestra'],
		[null, 'Anteprima a pieno schermo', '/aui.jsp?nomobile=1', '_blank', 'Apre anteprima in una nuova finestra'],
		_cmSplit,
		[null, 'Esci', 'javascript: AUI.Config.cmdExit();',null,null]
	],
	[null, 'Modifica', null, null, null,
		['<img src="'+themeImages+'new.gif" />','Nuova pagina...', 'javascript: AUI.Config.cmdNewPage();', null, null],
		[null,'Carica nuovo sfondo...', 'javascript: AUI.Config.cmdUploadBackground();', null, null],
		[null,'Cambia sfondo...', 'javascript: AUI.Config.cmdChangeBackground();', null, null],
		[null,'Rinomina pagina...', 'javascript: AUI.Config.cmdRenamePage();', null, null],
		[null,'Elimina pagina', 'javascript: AUI.Config.cmdDeletePage();', null, null],
		_cmSplit,
		['<img src="'+themeImages+'new.gif" />','Nuovo controllo', null, null, null,
			[null,'Cambio pagina...','javascript: AUI.Config.cmdNewControl("Page");',null,null],
			[null,'Scenario...','javascript: AUI.Config.cmdNewControl("Scene");',null,null],
			[null,'Luce...','javascript: AUI.Config.cmdNewControl("Light");',null,null],
			[null,'Dimmer...','javascript: AUI.Config.cmdNewControl("Dimmer");',null,null],
			[null,'Telecamera...','javascript: AUI.Config.cmdNewControl("Camera");',null,null],
			[null,'Speaker...','javascript: AUI.Config.cmdNewControl("Speaker");',null,null],
			[null,'Termostato...','javascript: AUI.Config.cmdNewControl("Temperature");',null,null],
			[null,'Presa...','javascript: AUI.Config.cmdNewControl("Power");',null,null]
		],
		[null,'Cambia layer...', null, null, null,
			[null,'Ogni layer','javascript: AUI.Config.cmdChangeControlLayer(null);',null,null],
			_cmSplit,
			[null,'Scenari','javascript: AUI.Config.cmdChangeControlLayer("scene");',null,null],
			[null,'Luci','javascript: AUI.Config.cmdChangeControlLayer("light");',null,null],
			[null,'Serramenti','javascript: AUI.Config.cmdChangeControlLayer("blind");',null,null],
			[null,'Sicurezza','javascript: AUI.Config.cmdChangeControlLayer("security");',null,null],
			[null,'Video','javascript: AUI.Config.cmdChangeControlLayer("video");',null,null],
			[null,'Audio','javascript: AUI.Config.cmdChangeControlLayer("audio");',null,null],
			[null,'Termoregolazione','javascript: AUI.Config.cmdChangeControlLayer("thermo");',null,null],
			[null,'Controllo carichi','javascript: AUI.Config.cmdChangeControlLayer("power");',null,null]
		],
		[null,'Cambia tipo...', 'javascript: AUI.Config.cmdChangeControlType();', null, null],
		[null,'Cambia icona...', 'javascript: AUI.Config.cmdChangeControlIcon();', null, null],
		[null,'Rinomina controllo...', 'javascript: AUI.Config.cmdRenameControl();', null, null],
		[null,'Elimina controllo', 'javascript: AUI.Config.cmdDeleteControl();', null, null]
	], 
	[null,'Visualizza', null, null, null,
		['<div id="checkLayer-all"><img src="'+skinImages+'tick.png"></div>','Mostra tutto','javascript: AUI.Config.cmdShowAllLayers();',null,null],
	  _cmSplit,
		['<div id="checkLayer-scene"></div>','Scenari','javascript: AUI.Config.cmdShowLayerToggle("scene");',null,null],
		['<div id="checkLayer-light"></div>','Luci','javascript: AUI.Config.cmdShowLayerToggle("light");',null,null],
		['<div id="checkLayer-blind"></div>','Serramenti','javascript: AUI.Config.cmdShowLayerToggle("blind");',null,null],
		['<div id="checkLayer-security"></div>','Sicurezza','javascript: AUI.Config.cmdShowLayerToggle("security");',null,null],
		['<div id="checkLayer-video"></div>','Video','javascript: AUI.Config.cmdShowLayerToggle("video");',null,null],
		['<div id="checkLayer-audio"></div>','Audio','javascript: AUI.Config.cmdShowLayerToggle("audio");',null,null],
		['<div id="checkLayer-thermo"></div>','Termoregolazione','javascript: AUI.Config.cmdShowLayerToggle("thermo");',null,null],
		['<div id="checkLayer-power"></div>','Controllo carichi','javascript: AUI.Config.cmdShowLayerToggle("power");',null,null]
	],
  [null, 'Strumenti', null, null, null,
  	[null, 'Cambia password configurazione...', 'javascript: AUI.Config.cmdChangePassword();', null, null],
  	_cmSplit,
		[null, 'Impostazioni rete senza fili', null, null, null],
    [null, 'Impostazioni rete locale', null, null, null],
  	_cmSplit,
    [null, 'Modifica data/ora', null, null, null],
    [null, 'Modifica password accesso remoto', null, null, null],
   	_cmSplit,
    [null, 'Opzioni', null, null, null],
   ],
   [null, 'Aiuto', null, null, null,
  	[null, 'Contenuti della guida', '/guida/', '_blank', null],
  	[null, 'Guida in linea...', 'http://www.ascia.it/aui/guida/', '_blank', null],
	  _cmSplit,
    [null, 'Visita il sito di ASCIA...', 'http://www.ascia.it/', '_blank', null]
   ]
   
];
cmDraw ('mainmenu', myMenu, 'hbr', cmThemeOffice2003);
--></script>

<div id="login" class="popup">
	<form action="http://null/" onsubmit="return false;">
		<h3>Login Configurazione di AU&igrave;</h3>
		<div id="loginError" class="error"></div> 
		<p>Utilizzare nome utente e password validi per accedere alla Configurazione.</p>
		<label for="username">Nome utente:</label><input type="text" name="username">
		<label for="password">Password:</label><input type="password" name="password">
		<button type="submit" onclick="AUI.Config.loginSubmit(this.form);">Login</button>
	</form>
</div>

<div id="changeBackground" class="popup">
		<h3>Cambia immagine</h3>
		<p>Selezionare il file che si vuole utilizzare come sfondo per la pagina corrente.</p>
		<div id="images"></div>
</div>

<div id="upload" class="popup">
	<form action="/aui/upload" method="post" onsubmit="return AUI.Upload.submit(this, {'onComplete' : AUI.Config.completeUploadCallback});" enctype="multipart/form-data">
		<h3>Carica nuova immagine di sfondo</h3>
		<div id="uploadError" class="error"></div> 
		<p>Selezionare il file da caricare sul server e da utilizzare come sfondo per la pagina corrente.</p>
		<label for="username">File:</label><input type="file" size="50" name="file">
		<button type="submit">Carica</button>
	</form>
</div>

<div id="renamePage" class="popup">
	<form action="http://null/" onsubmit="return false;">
		<h3>Rinomina pagina</h3>
		<p>Immettere il titolo della pagina corrente:</p>
		<input type="text" size="100" name="title">
		<button type="submit" onclick="AUI.Config.renamePageSubmit(this.form);">Aggiorna</button>
	</form>
</div>

</body>
</html>