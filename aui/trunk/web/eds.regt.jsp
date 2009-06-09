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
<link href="eds.regt.css" rel="stylesheet" type="text/css"/>
<script language="javascript" src="classes/Regt.js"></script>
</head>
<body>

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
<div id="regt-<%= stagione %>" class="eds-regt-stagione" >
<div><%= stagioni[stagione] %></div>
<%	
	for (int giorno = 0; giorno <= 6; giorno++) {
%>
	<div id="regt-<%= stagione %>-<%= giorno%>" class="eds-regt-giorno">
	<div><%= giorni[giorno] %></div>
<%	
		for (int ora = 0; ora <= 23; ora++) {
%>
		<div id="regt-<%= stagione %>-<%= giorno%>-<%= ora %>" class="eds-regt-ora" onmouseover="Regt.onMouseOver(event);">
			<div id="regt-<%= stagione %>-<%= giorno%>-<%= ora %>-bar" class="eds-regt-bar" style="height: <%= r.nextInt(100) %>px;">
			</div>
			<%= ora %>
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