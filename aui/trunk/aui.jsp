<!-- (C) Copyright 2007,2009 Ascia S.r.l. -->
<%@ page import="it.ascia.ais.Controller, it.ascia.aui.AUIControllerModule, org.apache.commons.configuration.HierarchicalConfiguration, java.util.*,org.apache.log4j.Logger, org.json.simple.JSONObject" %>
<%
/**
 * Se riceviamo il parametro "nomobile" allora siamo sul fisso.
 */
boolean mobile;
{
	String fixedParameter = request.getParameter("nomobile");
	if ((fixedParameter == null) || (fixedParameter == "0")) {
		mobile = true;
	} else {
		mobile = false;
	}
}
%>
<%
Controller c = Controller.getController(); 
AUIControllerModule auiControllerModule = (AUIControllerModule) c.getModule("AUI");
HierarchicalConfiguration auiConfig = auiControllerModule.getConfig();
Logger logger = Logger.getLogger("AUI");
HashMap pagesMap = new HashMap();
%>
<?xml version="1.0" encoding="iso-8859-1"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>AUì</title>
<link href="aui.css" rel="stylesheet" type="text/css"/>
<meta name="viewport" content="width=device-width, maximum-scale=3" />
<meta name="apple-mobile-web-app-capable" content="yes" />
<link rel="apple-touch-icon" href="/images/apple-touch-icon.png" />
</head>
<body>
<div>
<div id="header-out">
	<div id="header-bk"><img src="images/barratesti.png" /></div>
	<div id="header"><b>barra di stato</b></div>
</div>
<%
List pages = auiConfig.configurationsAt("page");
for (Iterator it = pages.iterator(); it.hasNext();) {
	HierarchicalConfiguration pageConfig = (HierarchicalConfiguration) it.next();
	String pageType = pageConfig.getString("type");
	String pageId = pageConfig.getString("id");
	String pageDisplay = auiConfig.getString("startPage").equals(pageConfig.getString("id")) ? "block" : "none"; 	
%>
<div id="page-<%= pageId %>" class="<%= pageType %>" style="display: <%= pageDisplay %>">
<%
	if (pageType.equals("screensaver")) {
%>
<img onclick="changePage('page-<%= pageId %>','page-<%= pageConfig.getString("page") %>');" title="<%= pageConfig.getString("title") %>" src="<%= pageConfig.getString("src") %>" />
<%
	} // page type=screensaver 
	else if (pageType.equals("imgmap")) {
%>
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
	} // page type=imgmap 
	else if (pageType.equals("map")) {
		String mapId = pageConfig.getString("map");
		HashMap pageMap = new HashMap();
		pagesMap.put(mapId,pageMap);
		// ricerca, nell'elenco delle mappe, quella da visualizzare in questa pagina
		List maps = auiConfig.configurationsAt("map");
		for (Iterator im = maps.iterator(); im.hasNext(); ) {
			HierarchicalConfiguration mapConfig = (HierarchicalConfiguration) im.next();
			if (mapConfig.getString("id").equals(mapId)) {
%>
<img title="<%= mapConfig.getString("title") %>" src="<%= mapConfig.getString("src") %>" border="0" />
<%
				List controls = mapConfig.configurationsAt("control");
				for (Iterator ic = controls.iterator(); ic.hasNext(); ) {
					HierarchicalConfiguration controlConfig = (HierarchicalConfiguration) ic.next();
					String id = "control-" + mapId + "-" + controlConfig.getString("id");
					String type = controlConfig.getString("type");
					String onEvent = null;
					String eventType = "ontouchstart";
					if (type.equals("page")) {
						onEvent = "changePage('page-"+pageId+"','page-"+controlConfig.getString("page")+"')";
						eventType = "onclick";
					} else {
						onEvent = "touchControl(event, '"+id+"')";
					}
					if (!mobile) {
						eventType="onclick";						
					}
%>
<div id="<%= id %>" class="control control-type-<%= type %>" style="left: <%= controlConfig.getString("left") %>px; top: <%= controlConfig.getString("top") %>px;" <%= eventType %>="<%= onEvent %>">
	<img id="<%= id %>-img" src="<%= auiConfig.getString("controls."+type+".default") %>" title="<%= controlConfig.getString("title",type) %>" border="0" alt="<%= type %>"/>
	<span id="<%= id %>-label" ></span>
</div>
<%				
				} // controls
			} // mapConfig
		} // maps
	} // page type=map 
	else  {
		logger.error("Pagina di tipo '"+pageType+"' non prevista");
	}		
%>
</div>
<%
} // pages

%>
</div>

<script language="javascript">
var controls = <%= auiControllerModule.getControls() %>;
var addresses = <%= auiControllerModule.getAddresses() %>;
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

</body>
</html>