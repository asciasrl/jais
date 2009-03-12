<!-- (C) Copyright 2007,2009 Ascia S.r.l. -->
<%@ page import="it.ascia.ais.Controller, it.ascia.aui.AUIControllerModule, org.apache.commons.configuration.HierarchicalConfiguration, java.util.*,org.apache.log4j.Logger, org.json.simple.JSONObject" %>
<%@ include file="constants.jsp" %>
<%@ include file="custom/config.jsp" %>
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
initAUI();
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
<div 
  xongesturestart="gestureStart(event);"
  xongesturechange="gestureChange(event);"
  xongestureend="gestureEnd(event);"
  xontouchstart="touchStart(event);"
  xontouchmove="touchMove(event);"
>  
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

var startX = 0;
var startY = 0;

function touchStart(event,control) {
  event.preventDefault();
	startX = event.targetTouches[0].pageX;
	startY = event.targetTouches[0].pageY;	
}

function touchMove(event) {
    event.preventDefault();
    curX = event.targetTouches[0].pageX - startX;
    curY = event.targetTouches[0].pageY - startY;
    event.targetTouches[0].target.style.webkitTransform = 'translate(' + curX + 'px, ' + curY + 'px)';
		console.log("X="+curX+" Y="+curY);
}

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

<script type="" language="javascript">
	const IMG_LIGHT_ON = "<%= IMG_LIGHT_ON %>";
	const IMG_LIGHT_OFF = "<%= IMG_LIGHT_OFF %>";
	const IMG_POWER_ON = "<%= IMG_POWER_ON %>";
	const IMG_POWER_OFF = "<%= IMG_POWER_OFF %>";
	const IMG_THERMO_ON = "<%= IMG_THERMO_ON %>";
	const IMG_THERMO_OFF = "<%= IMG_THERMO_OFF %>";
	const IMG_BLIND_STILL = "<%= IMG_BLIND_STILL %>";
	const IMG_BLIND_OPENING = "<%= IMG_BLIND_OPENING %>";
	const IMG_BLIND_CLOSING = "<%= IMG_BLIND_CLOSING %>";
	const STATUS_BAR_HEIGHT = <%= STATUS_BAR_HEIGHT %>;
	const STATUS_BAR_OPACITY = "<%= STATUS_BAR_OPACITY %>";
	const APPBAR_START_POSITION = <%= APPBAR_START_POSITION %>;
	const DIMMER_SLIDER_HEIGHT = <%= DIMMER_SLIDER_HEIGHT %>;
	const DIMMER_TOP_MIN = <%= IMG_DIMMER_SLIDER_TOP_HEIGHT - DIMMER_SLIDER_CORNER_HEIGHT %>;
	const DIMMER_TOP_MAX = <%= IMG_DIMMER_SLIDER_TOP_HEIGHT - DIMMER_SLIDER_CORNER_HEIGHT  + DIMMER_SLIDER_HEIGHT - IMG_DIMMER_CURSOR_HEIGHT %>;
	const DIMMER_CURSOR_MIDDLE = <%= IMG_DIMMER_CURSOR_HEIGHT / 2 %>;
	const DIMMER_SLIDER_TOTAL_HEIGHT = <%= IMG_DIMMER_SLIDER_TOP_HEIGHT + DIMMER_SLIDER_HEIGHT - 2 * DIMMER_SLIDER_CORNER_HEIGHT + IMG_DIMMER_SLIDER_BOTTOM_HEIGHT %>;
	const DIMMER_SLIDER_WIDTH = <%= DIMMER_WIDTH %>;
	const IMG_BLIND_CONTROL = "<%= IMG_BLIND_CONTROL %>";
	const BLIND_UP_TOP = <%= BLIND_UP_TOP %>;
	const BLIND_LEFT = <%= BLIND_LEFT %>;
	const BLIND_UP_BOTTOM = <%= BLIND_UP_BOTTOM %>;
	const BLIND_RIGHT = <%= BLIND_RIGHT %>;
	const BLIND_DOWN_TOP = <%= BLIND_DOWN_TOP %>;
	const BLIND_DOWN_BOTTOM = <%= BLIND_DOWN_BOTTOM %>;
	const BLIND_CONTROL_HEIGHT = <%= IMG_BLIND_CONTROL_HEIGHT %>;
	const BLIND_CONTROL_WIDTH = <%= IMG_BLIND_CONTROL_WIDTH %>;
	const MAP_AREA_WIDTH = <%= IPOD_VIEWPORT_WIDTH %>;
	const MAP_AREA_HEIGHT = <%= IPOD_MAP_AREA_HEIGHT %>;
	const IMG_LOCK_OPEN = "<%= IMG_LOCK_OPEN %>";
	const IMG_LOCK_CLOSE = "<%= IMG_LOCK_CLOSE %>";
	const IMG_DOOR_OPEN = "<%= IMG_DOOR_OPEN %>";
	const IMG_DOOR_CLOSE = "<%= IMG_DOOR_CLOSE %>";
	const IMG_DOOR_OPEN_ALARM = "<%= IMG_DOOR_OPEN_ALARM %>";
	const IMG_DOOR_CLOSE_OK = "<%= IMG_DOOR_CLOSE_OK %>";
</script>

<%= includeScript("aui.js") %>
<%= includeScript("aui_old.js") %>
<%= includeScript("statusbar.js") %>
<%= includeScript("comm.js") %>
<%= includeScript("map.js") %>
<%= includeScript("appbar_common.js") %>
<%
if (auiConfig.getString("appbar","default").equals("simple")) {
	out.print(includeScript("appbar_simple.js"));
} else {
	out.print(includeScript("appbar.js"));
}
%>
<%= includeScript("services.js") %>
<%= includeScript("dimmer_slider.js") %>
<%= includeScript("blind.js") %>
<%= includeScript("keypad.js") %>
<%= includeScript("alarm.js") %>
</body>
</html>