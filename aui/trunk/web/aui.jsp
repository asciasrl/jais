<!-- (C) Copyright 2007,2009 Ascia S.r.l. -->
<%@ page import="it.ascia.ais.Controller, it.ascia.aui.AUIControllerModule, org.apache.commons.configuration.SubnodeConfiguration, org.apache.commons.configuration.HierarchicalConfiguration, java.util.*,org.apache.log4j.Logger, org.json.simple.JSONObject, org.json.simple.JSONArray" %>
<%
/**
 * Se riceviamo il parametro "nomobile" allora siamo sul fisso.
 */
boolean mobile = request.getParameter("nomobile") == null;

if (mobile) {
  String ua1 = request.getHeader( "User-Agent" );
  if (ua1 != null && 
  		(ua1.indexOf( "iPod" ) != -1 
  		|| ua1.indexOf( "iPhone" ) != -1
  		|| ua1.indexOf( "iPad" ) != -1
  		|| ua1.indexOf( "SymbianOS" ) != -1
  		|| ua1.indexOf( "Android" ) != -1
  	  )) {
	  mobile = true;
  } else {
	  mobile = false;
  }
}

int debugLevel = 0;
try {
	debugLevel = (new Integer(request.getParameter("debug"))).intValue();
} catch (Exception e) {	
}
boolean debug = request.getParameter("debug") != null;

%>
<%
Logger logger = Logger.getLogger("AUI");
Controller c = Controller.getController(); 
AUIControllerModule auiControllerModule = (AUIControllerModule) c.getModule("AUI");
if (auiControllerModule == null) {
	logger.fatal("Modulo AUI non caricato");
}
HierarchicalConfiguration auiConfig = auiControllerModule.getConfiguration();
String skin = auiConfig.getString("skin","");
HierarchicalConfiguration skinConfig = auiControllerModule.getSkinConfiguration();
%>
<?xml version="1.0" encoding="iso-8859-1"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>AUì</title>
<link href="<%= skin %>css/aui.css" rel="stylesheet" type="text/css"/>
<link href="<%= skin %>css/keypad.css" rel="stylesheet" type="text/css"/>
<!--  <meta name="viewport" content="width=device-width, maximum-scale=3" /> -->
<!--  <meta name="viewport" content="width=device-width, initial-scale = 1.0, user-scalable = no" />  -->
<meta name="viewport" content="width=device-width, initial-scale = 1.0, maximum-scale=2, minimum-scale=0.5" />
<meta name="apple-mobile-web-app-capable" content="yes" />
<meta name="apple-mobile-web-app-status-bar-style" content="black" />
<meta name="format-detection" content="telephone=no" />
<link rel="apple-touch-icon" href="<%= skin %>images/apple-touch-icon.png" />
</head>
<body onload="AUI.init();">

<!-- Barra visualizzazione stato -->
<div id="header-out">
	<div id="header"><b>barra di stato</b></div>
</div>

<div id="pages">

<!-- Pagine -->

<%
List pages = auiConfig.configurationsAt("pages.page");
String startPage = auiConfig.getString("startPage",null); 
String pagina = request.getParameter("page");
if (pagina != null && !pagina.equals("null")) {
	startPage = pagina;
}
for (Iterator it = pages.iterator(); it.hasNext();) {
	HierarchicalConfiguration pageConfig = (HierarchicalConfiguration) it.next();
	String pageId = pageConfig.getString("[@id]");
	if (startPage == null) {
		startPage = pageId;
	}
%>
<div id="page-<%= pageId %>" class="page" onmousedown="AUI.Pages.onMouseDown('<%= pageId %>',event);">
<img usemap="#imgmap-<%= pageId %>" title="<%= pageConfig.getString("title") %>" src="<%= pageConfig.getString("src") %>" border="0" />
<map name="imgmap-<%= pageId %>">
<%
	List areas = pageConfig.configurationsAt("area");
	for (Iterator ia = areas.iterator(); ia.hasNext();) {
		HierarchicalConfiguration areaConfig = (HierarchicalConfiguration) ia.next();
%>
<area title="<%= areaConfig.getString("title","") %>" shape="<%= areaConfig.getString("shape","rect") %>" coords="<%= areaConfig.getString("coords") %>" href="javascript:AUI.Pages.change('page-<%= pageId %>','page-<%= areaConfig.getString("page") %>');" />    
<%		
	} // areas
%>	
</map>
<%
	List controls = pageConfig.configurationsAt("control");
	for (Iterator ic = controls.iterator(); ic.hasNext(); ) {
		HierarchicalConfiguration controlConfig = (HierarchicalConfiguration) ic.next();
		String id = "control-" + pageId + "-" + controlConfig.getString("[@id]");
		String type = controlConfig.getString("type");
		String eventType = "onclick";
		String eventHandler = "void(0)";
		if (type.equals("page")) {
			eventType = "onclick";
			eventHandler = "AUI.Pages.change('page-"+pageId+"','page-"+controlConfig.getString("page")+"')"; 
		} else {
			if (mobile) {
				eventType = "ontouchstart";
				eventHandler = "AUI.Controls.onTouchStart('"+id+"',event)";
			} else {
				eventType = "onmousedown";
				eventHandler = "AUI.Controls.onMouseDown('"+id+"',event)";
			}
		}
		
%>
<div id="<%= id %>" class="control control-<%= type %>" style="left: <%= controlConfig.getString("left") %>px; top: <%= controlConfig.getString("top") %>px;">
	<div class="control-icon" <%= eventType %>="<%= eventHandler %>">
		<img id="<%= id %>-img" src="<%= skin + controlConfig.getString("default",skinConfig.getString("controls."+type+".default")) %>" title="<%= controlConfig.getString("title",controlConfig.getString("address")) %>" border="0" alt="<%= type %>"/>
	</div>
<%
		if (type.equals("dimmer")) {
%>	
	<div class="reddot">
	  <table cellpadding="0" cellspacing="0">
	    <tr>
	    	<td class="reddot-sx"></td>
	    	<td class="reddot-ce"><div class="reddot-text" id="<%= id %>-label">-%</div></td>
	    	<td class="reddot-dx"></td>
	    </tr>
	  </table>
	</div>
<% 
		} else if (type.equals("blind")) {
%>	
	<div id="<%= id %>-open" class="control-button control-button-open control-<%= type %>-open">
		<img id="<%= id %>-open-img" src="<%= skin + controlConfig.getString("open",skinConfig.getString("controls."+type+".open")) %>" title="Open <%= controlConfig.getString("title",controlConfig.getString("address")) %>" border="0" alt="Open <%= type %>"/>
	</div>
	<div id="<%= id %>-close" class="control-button control-button-close control-<%= type %>-close">
		<img id="<%= id %>-close-img" src="<%= skin + controlConfig.getString("close",skinConfig.getString("controls."+type+".close")) %>" title="Close <%= controlConfig.getString("title",controlConfig.getString("address")) %>" border="0" alt="Close <%= type %>"/>
	</div>
<% 
		} else if (type.equals("thermo")) {
%>	
	<div class="thermo-display" id="<%= id %>-label"  <%= eventType %>="<%= eventHandler %>">-,-°C</div>
<%
		}
%>
	<div id="<%= id %>-caption" class="caption"><%= controlConfig.getString("title",controlConfig.getString("address")) %></div>
</div>
<%				
	} // controls
%>

</div>
<%
} // pages

%>
</div>


<!-- Barra selezione layer -->
<div id="layers">
<div id="scroller">
<%
List layers = skinConfig.configurationsAt("layers.layer");
JSONArray jLayers = new JSONArray();
int nLayers = layers.size();
for (int iLayer = 0;  iLayer < (5 + nLayers); iLayer++) {
	int j = iLayer;
	while (j >= nLayers) {
		j -= nLayers;
	}
	HierarchicalConfiguration layerConfig = (HierarchicalConfiguration) layers.get(j);
	String layerId = layerConfig.getString("[@id]");
	HashMap h = new HashMap();
	h.put("layer",layerId);
	jLayers.add(h);
	String eventType = "onclick";
	String eventHandler = "void(0)";
	if (mobile) {
		eventType = "ontouchstart";
		eventHandler = "AUI.Layers.onTouchStart("+iLayer+",event)";
	} else {
		eventType = "onmousedown";
		eventHandler = "AUI.Layers.onMouseDown("+iLayer+",event)";
	}
	
%>
  <div id="layer-<%= iLayer %>" class="layer" <%= eventType %>="<%= eventHandler %>">
      <img id="layer-<%= iLayer %>-img" title="<%= layerConfig.getString("title") %>" alt="<%= layerId %>"	width="80" height="80" border="0" src="<%= skin %><%= layerConfig.getString("icon") %>" />
  </div>
<%
}
%>
</div>
</div>

<!-- Maschera e cursore del dimmer  -->
<div id="mask"></div>
<div id="slider">
	<img id="slider-cursor" src="<%= skin %>images/slider-cursor.png" width="128" height="130" />
</div>

<script type="text/javascript" language="javascript" src="classes/mootools-1.2.3-core-nc.js"></script>
<script type="text/javascript" language="javascript" src="classes/mootools-1.2.3.1-more.js"></script>

<script type="text/javascript" language="javascript" src="classes/jsonrpc.js"></script>

<script type="text/javascript" language="javascript" src="classes/AUI.js"></script>
<script type="text/javascript" language="javascript" src="classes/Logger.js"></script>
<script type="text/javascript" language="javascript" src="classes/Header.js"></script>
<script type="text/javascript" language="javascript" src="classes/Pages.js"></script>
<script type="text/javascript" language="javascript" src="classes/Http.js"></script>
<script type="text/javascript" language="javascript" src="classes/SetRequest.js"></script>
<script type="text/javascript" language="javascript" src="classes/StreamWebSocket.js"></script>
<script type="text/javascript" language="javascript" src="classes/StreamRequest.js"></script>
<script type="text/javascript" language="javascript" src="classes/Controls.js"></script>
<script type="text/javascript" language="javascript" src="classes/Layers.js"></script>
<script type="text/javascript" language="javascript" src="classes/Device.js"></script>
<script type="text/javascript" language="javascript" src="classes/Light.js"></script>
<script type="text/javascript" language="javascript" src="classes/Pushbutton.js"></script>
<script type="text/javascript" language="javascript" src="classes/Blind.js"></script>
<script type="text/javascript" language="javascript" src="classes/Dimmer.js"></script>
<script type="text/javascript" language="javascript" src="classes/Power.js"></script>
<script type="text/javascript" language="javascript" src="classes/Scene.js"></script>
<script type="text/javascript" language="javascript" src="classes/Group.js"></script>
<script type="text/javascript" language="javascript" src="classes/Thermo.js"></script>
<script type="text/javascript" language="javascript" src="classes/Webcam.js"></script>
<script type="text/javascript" language="javascript" src="classes/Digitalinput.js"></script>
<script type="text/javascript" language="javascript" src="classes/Alarm.js"></script>
<script type="text/javascript" language="javascript" src="classes/Keypad.js"></script>
<script type="text/javascript" language="javascript" src="classes/Prealarm.js"></script>
<script type="text/javascript" language="javascript" src="classes/Alarmzone.js"></script>

<script language="javascript" type="text/javascript">
AUI.Layers.layers = <%= jLayers.toString() %>;
var skin = '<%= skin %>';
</script>

<% if (debug) { %>
<script language="javascript" type="text/javascript">
AUI.Logger.setLevel(<%= debugLevel %>);
var debug = true;
</script>
<% } %>

<%
String startLayer = request.getParameter("layer");
%>
<script language="javascript" type="text/javascript">
AUI.Pages.setCurrentPageId("page-<%= startPage %>");
AUI.Pages.setCurrentLayer("<%= startLayer %>");
</script>

</body>
</html>