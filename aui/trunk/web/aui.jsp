<!-- (C) Copyright 2007,2009 Ascia S.r.l. -->
<%@ page import="it.ascia.ais.Controller, it.ascia.aui.AUIControllerModule, org.apache.commons.configuration.HierarchicalConfiguration, java.util.*,org.apache.log4j.Logger, org.json.simple.JSONObject, org.json.simple.JSONArray" %>
<%
/**
 * Se riceviamo il parametro "nomobile" allora siamo sul fisso.
 */
boolean mobile = request.getParameter("nomobile") == null;
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
%>
<?xml version="1.0" encoding="iso-8859-1"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>AUì</title>
<link href="<%= skin %>css/aui.css" rel="stylesheet" type="text/css"/>
<!--  <meta name="viewport" content="width=device-width, maximum-scale=3" /> -->
<!--  <meta name="viewport" content="width=device-width, initial-scale = 1.0, user-scalable = no" />  -->
<meta name="viewport" content="width=device-width, initial-scale = 1.0, maximum-scale=2, minimum-scale=0.5" />
<meta name="apple-mobile-web-app-capable" content="yes" />
<meta name="apple-mobile-web-app-status-bar-style" content="black" />
<meta name="format-detection" content="telephone=no" />
<link rel="apple-touch-icon" href="<%= skin %>images/apple-touch-icon.png" />
</head>
<body>

<!-- Barra visualizzazione stato -->
<div id="header-out">
	<div id="header"><b>barra di stato</b></div>
</div>

<div id="pages">

<!-- Pagine -->

<%
List pages = auiConfig.configurationsAt("pages.page");
String startPage = auiConfig.getString("startPage",null); 	
for (Iterator it = pages.iterator(); it.hasNext();) {
	HierarchicalConfiguration pageConfig = (HierarchicalConfiguration) it.next();
	String pageId = pageConfig.getString("[@id]");
	if (startPage == null) {
		startPage = pageId;
	}
	String pageDisplay = startPage.equals(pageId) ? "block" : "none"; 	
%>
<div id="page-<%= pageId %>" class="page" style="display: <%= pageDisplay %>">
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
<div id="<%= id %>" class="control control-<%= type %>" style="left: <%= controlConfig.getString("left") %>px; top: <%= controlConfig.getString("top") %>px;" <%= eventType %>="<%= eventHandler %>">
	<img id="<%= id %>-img" src="<%= skin + auiConfig.getString("controls."+type+".default") %>" title="<%= controlConfig.getString("title",controlConfig.getString("address")) %>" border="0" alt="<%= type %>"/>
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
		} else if (type.equals("thermo")) {
%>	
	<div class="thermo-display" id="<%= id %>-label">-,-°C</div>
<%
		}
%>
</div>
<%				
	} // controls
%>

</div>
<%
} // pages

%>
</div>


<!-- Maschera e cursore del dimmer  -->
<div id="mask"></div>
<div id="slider">
	<img id="slider-cursor" src="<%= skin %>/images/slider-cursor.png" width="128" height="130" />
</div>

<!-- Barra selezione layer -->
<div id="layers">
<div id="scroller">
<%
List layers = auiConfig.configurationsAt("layers.layer");
JSONArray jLayers = new JSONArray();
int nLayers = layers.size();
for (int iLayer = 0;  iLayer < (5 + nLayers); iLayer++) {
	int j = iLayer;
	if (iLayer >= nLayers) {
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

<%!
/**
 * Include uno script cambiandogli il nome, in modo da evitare il caching da
 * parte del browser.
 *
 * @return il codice HTML per includerlo.
 */
String includeScript(String scriptName) {
	return "<script type=\"text/javascript\" language=\"javascript\" src=\"" + scriptName +
		"?" + System.currentTimeMillis() + "\"></script>\n";
}
%>

<%= includeScript("classes/AUI.js") %>
<%= includeScript("classes/Logger.js") %>
<%= includeScript("classes/Header.js") %>
<%= includeScript("classes/Pages.js") %>
<%= includeScript("classes/Http.js") %>
<%= includeScript("classes/SetRequest.js") %>
<%= includeScript("classes/StreamRequest.js") %>
<%= includeScript("classes/Controls.js") %>
<%= includeScript("classes/Layers.js") %>
<%= includeScript("classes/Device.js") %>
<%= includeScript("classes/Light.js") %>
<%= includeScript("classes/Blind.js") %>
<%= includeScript("classes/Dimmer.js") %>
<%= includeScript("classes/Power.js") %>
<%= includeScript("classes/Thermo.js") %>
<%= includeScript("classes/Webcam.js") %>

<script language="javascript">
AUI.Controls.controls = <%= auiControllerModule.getControls() %>;
AUI.Controls.addresses = <%= auiControllerModule.getAddresses() %>;
AUI.Pages.pageLayers = <%= auiControllerModule.getPageLayerControls() %>;
AUI.Layers.layers = <%= jLayers.toString() %>;
var skin = '<%= skin %>';
</script>

<% if (debug) { %>
<script language="javascript">
AUI.Logger.setLevel(<%= debugLevel %>);
var debug = true;
</script>
<% } %>

<script language="javascript">
AUI.Pages.setCurrentPageId("page-<%= startPage %>");
setTimeout('AUI.StreamRequest.start();', 1000);
setTimeout('AUI.Layers.init();', 1000);
</script>

</body>
</html>