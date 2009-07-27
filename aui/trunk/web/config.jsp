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
</head>
<body onload="AUI.Regt.init();">

<div class="menubar">
	<div class="menuitem"></div>
	<div class="menuitem">File</div>
	<div class="menuitem">Impianto</div>
	<div class="menuitem">Pagina</div>
</div>

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

</body>
</html>