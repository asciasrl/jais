<!-- (C) Copyright 2009 Ascia S.r.l. -->
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="it.ascia.ais.Controller, it.ascia.aui.AUIControllerModule, org.apache.commons.configuration.HierarchicalConfiguration, java.util.*,org.apache.log4j.Logger, org.json.simple.JSONObject, org.json.simple.JSONArray" %>
<%@ page import="java.util.Random" %>
<%@ page import="org.apache.commons.configuration.SubnodeConfiguration" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>RegT</title>
<link href="skins/20090330/css/aui.css" rel="stylesheet" type="text/css"/>
<link href="skins/20090330/css/eds.regt.css" rel="stylesheet" type="text/css"/>
<script language="javascript" src="classes/AUI.js"></script>
<script language="javascript" src="classes/Logger.js"></script>
<script language="javascript" src="classes/Http.js"></script>
<script language="javascript" src="classes/SetRequest.js"></script>
<script language="javascript" src="classes/Regt.js"></script>
<script language="javascript" src="classes/jsonrpc.js"></script>
<script language="javascript" src="classes/mootools-1.2.3-core-nc.js"></script>
<script language="javascript" src="classes/mootools-1.2.3.1-more.js"></script>
</head>
<%
String address = request.getParameter("address");
String pagina = request.getParameter("page");
String layer = request.getParameter("layer");
{
	int i = address.indexOf(":");
	if (i > 0) {
		address = address.substring(0,i);
	}
}

Controller c = Controller.getController(); 
AUIControllerModule auiControllerModule = (AUIControllerModule) c.getModule("AUI");
SubnodeConfiguration auiConfig = auiControllerModule.getConfiguration();
String skin = auiConfig.getString("skin","");

%>
<body onload="AUI.Regt.init('<%=address%>','<%=pagina%>','<%=layer%>');">

<div>
 <div id="">
	<div style="display: inline; float: left;">
		<a href="aui.jsp?page=<%=pagina%>&layer=<%=layer%>"><img style="border: 0;" src="<%=skin%>images/page_back.png"></a>
	</div>
	<div class="control control-thermo" style="display: inline; width: 120px; float: left; position: relative;">
	 	<div style="position: relative;"><img src="<%=skin%>images/thermo_display.png"></div>
		<div id="temp" class="thermo-display">--,-°C</div>
		<div class="caption">Temperatura misurata</div>
	</div>
	<div style="display: inline; float: left;">
	 	<img id="season-winter" src="<%=skin%>images/eds.regt.season-winter-off.png" onclick="AUI.Regt.setSeason('winter');" title="Attiva modo invernale">
	 	<img id="season-summer" src="<%=skin%>images/eds.regt.season-summer-off.png" onclick="AUI.Regt.setSeason('summer');" title="Attiva modo estivo">
	 	<img id="mode-off" src="<%=skin%>images/eds.regt.off.png" onclick="AUI.Regt.setMode('off');" title="Spegne il cronotermostato">
	 	<img id="mode-chrono" src="<%=skin%>images/eds.regt.chrono-off.png" onclick="AUI.Regt.setMode('chrono');" title="Attiva il modo cronotermostato">
	 	<img id="mode-manual" src="<%=skin%>images/eds.regt.manual-off.png" onclick="AUI.Regt.setMode('manual');" title="Attiva il modo manuale">
 	</div>
 	<!--
	<div class="control" style="display: inline; width: 60px; float: left; position: relative;" onclick="AUI.Regt.activatePreset('T0');">
	 	<div style="position: relative;"><img src="<%=skin%>images/eds.regt.T0-off.png"  title="Attiva il preset T0"></div>
		<div id="T0" class="caption"></div>
	</div>
	<div class="control" style="display: inline; width: 60px; float: left; position: relative;"  onclick="AUI.Regt.activatePreset('T1');">
	 	<div style="position: relative;"><img src="<%=skin%>images/eds.regt.T1-off.png"  title="Attiva il preset T1"></div>
		<div id="T1" class="caption"></div>
	</div>
	<div class="control" style="display: inline; width: 60px; float: left; position: relative;"  onclick="AUI.Regt.activatePreset('T2');">
	 	<div style="position: relative;"><img src="<%=skin%>images/eds.regt.T2-off.png"  title="Attiva il preset T2"></div>
		<div id="T2" class="caption"></div>
	</div>
	<div class="control" style="display: inline; width: 60px; float: left; position: relative;"  onclick="AUI.Regt.activatePreset('T3');">
	 	<div style="position: relative;"><img src="<%=skin%>images/eds.regt.T3-off.png"  title="Attiva il preset T3"></div>
		<div id="T3" class="caption"></div>
	</div>
	-->
	<div class="control" style="display: inline; position: relative; float: left;"><img src="<%=skin%>images/thermo_up.png" onclick="AUI.Regt.setPointUp();"></div>
	<div class="control control-thermo" style="display: inline; width: 120px; float: left; position: relative;">
	 	<div style="position: relative;"><img src="<%=skin%>images/thermo_display.png"></div>
		<div id="setPoint" class="thermo-display">--,-°C</div>
		<div class="caption">Set point attuale</div>
	</div>
	<div class="control" style="display: inline; position: relative; float: left;"><img src="<%=skin%>images/thermo_dn.png" onclick="AUI.Regt.setPointDown();"></div>
 </div>
</div>

<%

Random r = new Random();
String[] stagioni = {"Inverno","Estate"};
String[] giorni = {"Dom","Lun","Mar","Mer","Gio","Ven","Sab"};

for (int stagione = 0; stagione <= 1; stagione++) {
%>
<div id="regt-<%= stagione %>" class="eds-regt-<%= stagione %>-season" >
<h1><%= stagioni[stagione] %></h1>
<%	
	for (int giorno = 0; giorno <= 6; giorno++) {
%>
	<div id="regt-<%= stagione %>-<%= giorno%>" class="eds-regt-<%= stagione %>-day"
			onmousedown="AUI.Regt.onMouseDownDay(event,'<%=address%>'<%= stagione + "-" + giorno %>');"
	>
	<h2><%= giorni[giorno] %></h2>
<%	
		for (int ora = 0; ora <= 23; ora++) {
			String id = stagione + "-" + giorno + "-" + ora;
%>
		<div class="eds-regt-<%= stagione %>-hour"
		onmousedown="AUI.Regt.onMouseDownHour(event,'<%= id %>');"
		>
			<%= ora %>
			<div class="eds-regt-<%= stagione %>-cursor" id="eds-regt-<%= id %>" 
			onmousedown="AUI.Regt.onMouseDownCursor(event,'<%= id %>');"
			>--</div>
		</div>		
<%
		}
%>
	</div>
<%	
	}		
%>
</div>
<%	
}

%>

</body>
</html>