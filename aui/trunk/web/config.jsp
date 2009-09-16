<!-- (C) Copyright 2009 Ascia S.r.l. -->
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ page import="it.ascia.ais.Controller, it.ascia.aui.AUIControllerModule, org.apache.commons.configuration.HierarchicalConfiguration, java.util.*,org.apache.log4j.Logger, org.json.simple.JSONObject, org.json.simple.JSONArray" %>
<%@ page import="java.util.Random" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>AUI Configurazione</title>
<link href="skins/20090330/css/aui.css" rel="stylesheet" type="text/css"/>
<link href="skins/20090330/css/config.css" rel="stylesheet" type="text/css"/>
<script language="javascript" src="classes/AUI.js"></script>
<script language="javascript" src="classes/Logger.js"></script>
<script language="javascript" src="classes/Http.js"></script>
<script language="javascript" src="classes/Config.js"></script>
<script type="text/javascript" src="JSCookMenu/JSCookMenu.js"></script>
<link rel="stylesheet" href="JSCookMenu/ThemeOffice2003/theme.css" type="text/css">
<script type="text/javascript" src="JSCookMenu/ThemeOffice2003/theme.js"></script>
</head>
<body onload="AUI.Regt.init();">

<div id="mainMenu" class="menubar"></div>

<table id="main" cellpadding="0" cellspacing="0" border="0">
	<tr>
		<td scope="col" class="header">Impianto</td>	
		<td scope="col" class="header">Pagina</td>
		<td scope="col" class="header">Pagine</td>	
	</tr>
	<tr>
		<td scope="row" id="impianto">
<% 
for (int i=0; i < 30; i++) { 
%>
<div id="control-<%= i %>" class="control">cc
</div>
<%
}
%>
</td>	
		<td scope="row" id="pagina">Pagina</td>
		<td scope="row" id="pagine">Pagine</td>	
	</tr>
</table>

<script type="text/javascript"><!--
var myMenu =
[
		 ['&nbsp;', 'File', null, null, null,
		  ['<img src="JSCookMenu/ThemeOffice/new.gif"/>', 'Nuovo', '?cmd=New', '', 'Nuovo progetto'],  // a menu item
		  ['<img class="seq1" src="JSCookMenu/ThemeOffice/open.gif" /><img class="seq2" src="JSCookMenu/ThemeOffice/openshadow.gif" />', 'Apri ...', null, null, null],
		  _cmSplit,
		  ['<img class="seq1" src="JSCookMenu/ThemeOffice/save.gif" /><img class="seq2" src="JSCookMenu/ThemeOffice/saveshadow.gif" />', 'Salva', null, null, null],
		  ['<img class="seq1" src="JSCookMenu/ThemeOffice/save.gif" /><img class="seq2" src="JSCookMenu/ThemeOffice/saveshadow.gif" />', 'Salva con nome ...', null, null, null],
		  _cmSplit,
		  [null, 'Anteprima', '/aui.jsp?nomobile=1', '_blank', 'Apre anteprima in una nuova finestra'],
		  _cmSplit,
		  [null, 'Esci', '/', null, null]
		],
    [null, 'Impianto', null, null, null,
     	[null, 'Nuova connessione ...', '?do=NewConnector', null, null]
    ],
    ['icon', 'title', 'url', 'target', 'description'],  // a menu item
    _cmSplit,
    ['', 'Strumenti', '', '', '',
        [null, 'Amministrazione', null, null, null,
            [null, 'Cambio password', '?do=ChangePassword', null, null],
            [null, 'title', 'url', 'target', 'description']
        ],
        [null, 'Aggiornamenti', null, null, null,
         		[null, 'Cambio password', '?do=ChangePassword', null, null],
         		[null, 'title', 'url', 'target', 'description']
		    ],
        [null, 'Connessioni', null, null, null,
         		[null, 'Rete senza fili', '?do=ConfigWifi', null, null],
          	[null, 'Rete locale', '?do=ConfigLan', null, null]
        ],           	
        [null, 'Server',null,null,null,
         		[null, 'Imposta data e ora', '?do=DateTime', null, null],
         		[null, 'Password accesso remoto', '?do=Passwd', null, null]
        ]
    ]
];
cmDraw ('mainMenu', myMenu, 'hbr', cmThemeOffice2003);
--></script>

</body>
</html>