<!-- (C) Copyright 2007,2009 Ascia S.r.l. -->
<%@ page import="it.ascia.ais.Controller, it.ascia.aui.AUIControllerModule, org.apache.commons.configuration.HierarchicalConfiguration, java.util.*,org.apache.log4j.Logger, org.json.simple.JSONObject" %>
<%
/**
 * Se riceviamo il parametro "nomobile" allora siamo sul fisso.
 */
boolean mobile = request.getParameter("nomobile") == null;
boolean debug = request.getParameter("debug") != null;
%>
<%
Controller c = Controller.getController(); 
AUIControllerModule auiControllerModule = (AUIControllerModule) c.getModule("AUI");
HierarchicalConfiguration auiConfig = auiControllerModule.getConfiguration();
Logger logger = Logger.getLogger("AUI");
String skin = auiConfig.getString("skin","");
%>
<?xml version="1.0" encoding="iso-8859-1"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>AUì</title>
<link href="<%= skin %>css/aui.css" rel="stylesheet" type="text/css"/>
<meta name="viewport" content="width=device-width, maximum-scale=3" />
<meta name="apple-mobile-web-app-capable" content="yes" />
<link rel="apple-touch-icon" href="<%= skin %>images/apple-touch-icon.png" />
</head>
<body>
<div>
<div id="header-out">
	<div id="header-bk"><img src="<%= skin %>images/barratesti.png" /></div>
	<div id="header"><b>barra di stato</b></div>
</div>
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
<area title="<%= areaConfig.getString("title","") %>" shape="<%= areaConfig.getString("shape","rect") %>" coords="<%= areaConfig.getString("coords") %>" href="javascript:changePage('page-<%= pageId %>','page-<%= areaConfig.getString("page") %>');" />    
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
		LinkedHashMap events = new LinkedHashMap();
		if (type.equals("page")) {
			events.put("onclick","changePage('page-"+pageId+"','page-"+controlConfig.getString("page")+"')");
		} else if (type.equals("dimmer")) {
			events.put(mobile ? "ontouchstart" : "onmousedown","touchControl(event, '"+id+"')");
			//events.put(mobile ? "ontouchmove" : "onmousemove","moveControl(event, '"+id+"')");
			events.put(mobile ? "ontouchend" : "onmouseup","endControl(event, '"+id+"')");
		} else {
			events.put(mobile ? "ontouchstart" : "onmousedown","touchControl(event, '"+id+"')");
		}
		
%>
<div id="<%= id %>" class="control control-<%= type %>" style="left: <%= controlConfig.getString("left") %>px; top: <%= controlConfig.getString("top") %>px;"<%  
		for (Iterator ie = events.keySet().iterator(); ie.hasNext(); ) {
			String eventType = (String) ie.next();
			String onEvent = (String) events.get(eventType);
%> <%= eventType %>="<%= onEvent %>"<%
		}
%>>
	<img id="<%= id %>-img" src="<%= skin + auiConfig.getString("controls."+type+".default") %>" title="<%= controlConfig.getString("title",type) %>" border="0" alt="<%= type %>"/>
	<span id="<%= id %>-label" ></span>
</div>
<%				
	} // controls
%>
</div>
<%
} // pages

%>
</div>

<script language="javascript">
var controls = <%= auiControllerModule.getControls() %>;
var addresses = <%= auiControllerModule.getAddresses() %>;
var skin = '<%= skin %>';
</script>
<%!
/**
 * Include uno script cambiandogli il nome, in modo da evitare il caching da
 * parte del browser.
 *
 * @return il codice HTML per includerlo.
 */
String includeScript(String scriptName) {
	return "<script type=\"\" language=\"javascript\" src=\"" + scriptName +
		"?" + System.currentTimeMillis() + "\"></script>\n";
}
%>

<%= includeScript("aui.js") %>
<% if (debug) { %>
<div id="debug">debug</div>
<% } %>
</body>
</html>