<!-- (C) Copyright 2009 Ascia S.r.l. -->
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="it.ascia.ais.Controller, it.ascia.aui.AUIControllerModule, org.apache.commons.configuration.HierarchicalConfiguration, java.util.*,org.apache.log4j.Logger, org.json.simple.JSONObject, org.json.simple.JSONArray" %>
<%@ page import="java.util.Random" %>
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
</head>
<body onload="AUI.Regt.init();">

<ul class="eds-regt-stagione">
	<li>Estate</li>
	<li>Inverno</li>
</ul>

<%

Random r = new Random();
String[] stagioni = {"Estate","Inverno"};
String[] giorni = {"Lun","Mar","Mer","Gio","Ven","Sab","Dom"};

for (int stagione = 0; stagione <= 1; stagione++) {
%>
<div id="regt-<%= stagione %>" class="eds-regt-season" >
<div><%= stagioni[stagione] %></div>
<%	
	for (int giorno = 0; giorno <= 6; giorno++) {
%>
	<div id="regt-<%= stagione %>-<%= giorno%>" class="eds-regt-day"
			onmousedown="AUI.Regt.onMouseDownDay(event,'<%= stagione + "-" + giorno %>');"
	>
	<div><%= giorni[giorno] %></div>
<%	
		for (int ora = 0; ora <= 23; ora++) {
			String id = stagione + "-" + giorno + "-" + ora;
%>
		<div class="eds-regt-hour"
		onmousedown="AUI.Regt.onMouseDownHour(event,'<%= id %>');"
		>
			<%= ora %>
			<div class="eds-regt-cursor" id="eds-regt-<%= id %>" 
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