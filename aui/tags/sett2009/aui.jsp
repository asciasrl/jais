<%
/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
 %>
<%@ page import="java.io.File, java.io.IOException, 
	java.util.Map, java.util.Iterator,
	javax.servlet.jsp.JspWriter,
	javax.xml.parsers.DocumentBuilder, 
	javax.xml.parsers.DocumentBuilderFactory,
	javax.xml.parsers.ParserConfigurationException, org.apache.log4j.Logger,
	org.w3c.dom.Document, org.w3c.dom.Element, org.w3c.dom.NodeList,
	org.xml.sax.SAXException, org.xml.sax.SAXParseException" %>
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
<%!
/**
 * Eccezione nel parsing.
 *
 * @author arrigo
 */
class ParseException extends Exception {
	public ParseException(String message) {
		super(message);
	}
}

%><%!
/**
 * Le informazioni contenute in un file di configurazione di AUI.
 *
 * <p>Questa classe carica il file di configurazione, ne effettua il parsing,
 * esegue un primo controllo di consistenza e mette a disposizione i nodi
 * del DOM XML che ha letto.</p>
 * 
 * @author arrigo
 */
class ConfigurationFile {
	/**
	 * Il nome del file di configurazione.
	 */
	private String fileName;
	/**
	 * Il risultato del parsing XML.
	 */
	private Document document;
	/**
	 * L'elemento <appbar>.
	 */
	private Element appBarElement;
	 /**
	  * Il primo elemento <map>.
	  */
	private Element firstMapElement;
	/**
	 * Altezza del cursore del dimmer richiesta.
	 */
	private int dimmerHeight; 
	/**
	 * Immagine che rappresenta la prima mappa.
	 */
	private String mapImage;
	/**
	 * La lista degli elementi <service>.
	 * 
	 * Gli elementi sono di tipo Element.
	 */
	private NodeList serviceElements;
	/**
	 * Il nostro logger.
	 */
	private Logger logger;
	
	/**
	 * Costruttore.
	 * @param fileName il nome del file di configurazione.
	 */
	public ConfigurationFile(String fileName) throws ParseException {
		this.fileName = fileName;
		logger = Logger.getLogger(getClass());
		parse();
	}
	
	/**
	 * Effettua una prima verifica del documento interpretato.
	 * 
	 * <p>Questo metodo viene chiamato solo dal costruttore.</p>
	 */
	private void parseDocument() throws ParseException {
		Element element;
		NodeList list;
		String temp;
		element = document.getDocumentElement();
		// Primo tag: <aui:configuration>
		if (!element.getTagName().equals("aui:configuration")) {
			throw new ParseException("Primo tag non riconosciuto: " +
					element.getTagName());
		}
		// Estrazione tag <appbar>
		list = element.getElementsByTagName("appbar");
		if (list.getLength() < 1) {
			throw new ParseException("Il file non contiene la configurazione " +
						"dell'appbar!");
		}
		appBarElement = (Element)list.item(0);
		// Estrazione primo tag <map>
		list = element.getElementsByTagName("map");
		if (list.getLength() < 1) {
			throw new ParseException("Il file non contiene mappe!");
		}
		firstMapElement = (Element)list.item(0);
	}
	
	/**
	 * Effettua il parsing XML del file di configurazione.
	 * 
	 * L'operazione di lettura dei dati e' affidata a parseDocument() e a
	 * createBMCs().
	 */
	private void parse() throws ParseException {
		try {
			DocumentBuilderFactory factory;
			DocumentBuilder builder;
			factory = DocumentBuilderFactory.newInstance();
			builder = factory.newDocumentBuilder();
			document = builder.parse(new File(fileName));
			parseDocument();
		} catch (SAXParseException e) {
			throw new ParseException("Errore durante il parsing, linea " +
					e.getLineNumber() + ", uri " + e.getSystemId() + ": " +
					e.getMessage());
		} catch (SAXException e) {
			throw new ParseException("Errore SAX durante il parsing: " +
					e.getMessage());
		} catch (ParserConfigurationException e) {
			throw new ParseException("Errore di configurazione del parser: " +
					e.getMessage());
		} catch (IOException e) {
			throw new ParseException("Errore di I/O durante il parsing: " +
					e.getMessage());
		}
	}
	
	/**
	 * Legge il contenuto numerico (intero) di un attributo di un tag XML.
	 * 
	 * <p>In pratica, legge "10" da "<tag value='10'>".</p>
	 *
	 * <p>Se potesse, questo metodo sarebbe statico.</p>
	 * 
	 * @param element il tag XML.
	 * @param attributeName l'attributo da leggere.
	 */
	public int getIntegerAttribute(Element element,
			String attributeName) {
		String temp = element.getAttribute(attributeName);
		return Integer.parseInt(temp);
	}
	
}

%><%!
/** Crea un layer per ciascun servizio.
 * 
 * @param piano il piano.
 * @param apps i servizi possibili.
 * @param big true se stiamo facendo i layer della mappa grande.
 * @param clickable true se i servizi devono reagire a click
 * @param out dove scrivere l'output.
 *
 * @throw IOException se out lo decide.
 */
void creaLayerServizi(Map piano, String[] apps, boolean big, boolean clickable,
		boolean mobile, JspWriter out) throws IOException {
	double scale;
	String onClick;
	String fontSize;
	if (big) {
		scale = 1;
	} else {%><%!
		// Assumiamo che la mappa piccola e' uguale alla grande rimpicciolita
		int mapWidth = ((int[])piano.get("mapSize"))[0];
		int bigMapWidth = ((int[])piano.get("bigMapSize"))[0];
		scale = (double)mapWidth / bigMapWidth;
	}
	if (mobile) {
		onClick = "ontouchstart";
	} else {
		onClick = "onclick";
	}
	fontSize = (int)(scale * 100) + "%";
	for (int i = 0; i < apps.length; i++) {
		String s = apps[i];
		String idPiano;
		String id, text, busAddress;
		Map frame;
		int imgWidth, imgHeight;
		if (clickable) {
			idPiano = (String)piano.get("id") + "-big-" + s; 
		} else {
			idPiano = (String)piano.get("id") + "-" + s;
		}
		out.println("<div id=\"" + idPiano + "\" style=\"position: absolute; display: none;\">");
		imgWidth = imgHeight = (int) (MAP_ICON_WIDTH * scale);
		if (s.equals("illuminazione")) { 
			frame = (Map)frameIlluminazione.get(piano.get("id"));
			if (frame != null) {
				Iterator it = frame.keySet().iterator();
				while (it.hasNext()) {
					String idLuce = (String)it.next();
					String lit;
					Map luce = (Map)frame.get(idLuce); 
					if (clickable) {
						id = "id=\"" + idLuce + "\"";
						if (luce.get("type").equals(ILL_LUCE)) {
							lit = "lit=\"off\" " + onClick + "=\"lightClicked(event, this)\"";
							text = "OFF";
						} else {
							lit = "lit=\"0\" "+ onClick +"=\"dimmerClicked(event, this)\"";
							text = "0%";
						}
						busAddress = "busaddress=\"" + (String)luce.get("address") + "\"";
						idLuci.add(idLuce); 
					} else {
						id = "";
						lit = "";
						busAddress = "";
						text = "";
					}
					out.print("<div " + id + "style=\"position:absolute; left: " +
						(int)((Integer)luce.get("x") * scale) + "px; top: " + (Integer)luce.get("y") * scale + "px; color: white;\" " +
						busAddress + " " + lit + " name=\"" + (String)luce.get("label") + "\"><div style=\"position: absolute;\"><img src=\"" + IMG_LIGHT_OFF +
						"\" alt=\"" + (String)luce.get("label") + "\" width=\"" + imgWidth + "\"" +
						"height=\"" + imgHeight + "\" /></div><div style=\"position: absolute; font-size: " + fontSize + ";\">" + text + "</div></div>");
				} // foreach luce
			} else {
				out.print("Non ci sono luci per questo piano!");
			}
		} else if (s.equals("energia")) {
			frame = (Map)frameEnergia.get(piano.get("id"));
			if (frame != null) {
				Iterator it = frame.keySet().iterator();
				while (it.hasNext()) {
					String active; 
					String idPresa = (String)it.next();
					Map presa = (Map) frame.get(idPresa);
					if (clickable) { 
						id = "id=\"" + idPresa + "\"";
						active = "power=\"off\" busaddress=\"" + (String)presa.get("address") + 
							"\" onClick=\"powerClicked(event, this)\"";
						idPrese.add(idPresa);
						text = "OFF";
					} else {
						id = "";
						active = "";
						text = "";
					}
					out.print("<div "+ id + " style=\"position:absolute; left: " +
						(int)((Integer)presa.get("x") * scale) + "px; top: " + 
						(int)((Integer)presa.get("y") * scale) + "px; color: white;\" " + active + " name=\"" +
						(String)presa.get("label") + "\"><div style=\"position: absolute;\"><img src=\"" +
						IMG_POWER_OFF + "\" alt=\"" + (String)presa.get("label") +
						"\" width=\"" + imgWidth + "\" height=\"" + imgHeight + "\" /></div><div style=\"position: absolute; font-size: " + fontSize + ";\">" + text + "</div></div>");
				} // foreach presa
			} else {
				out.print("Non ci sono prese comandate su questo piano!");
			}
		} else if (s.equals("clima")) {
			frame = (Map)frameClima.get(piano.get("id"));
			if (frame != null) {
				Iterator it = frame.keySet().iterator();
				while (it.hasNext()) {
					String idClima = (String)it.next();
					Map clima = (Map)frame.get(idClima);
					String active;  
					if (clickable) {
						id = "id=\"" + idClima + "\"";
						active = "power=\"off\" busaddress=\"" +
							(String)clima.get("address") + "\" onClick=\"thermoClicked(event, this)\"";
						idClimi.add(idClima);
						text = "20&deg;C";
					} else {
						id = "";
						active = "";
						text = "";
					}
					out.print("<div " + id + " style=\"position:absolute; left: " +
						(int)((Integer)clima.get("x") * scale) + "px; top: " +
						(int)((Integer)clima.get("y") * scale) + "px; color: white;\" " + active + " name=\"" + 
						(String)clima.get("label") + "\"><div style=\"position: absolute;\"><img src=\"" +
						IMG_THERMO_OFF + "\" alt=\"" + (String)clima.get("label") +
						"\" width=\"" + imgWidth + "\" height=\"" + imgHeight + "\"/></div><div style=\"position: absolute; font-size: " + fontSize + ";\">" + text + "</div></div>");
				} // foreach clima
			} else {
				out.print("Non ci sono termostati su questo piano!");
			} 
		} else if (s.equals("serramenti")) {
			frame = (Map) frameSerramenti.get(piano.get("id"));
			if (frame != null) {
				Iterator it = frame.keySet().iterator();
				while (it.hasNext()) {
					String idSerramento = (String)it.next();
					Map schermo = (Map)frame.get(idSerramento);
					String active;
					if (clickable) {
						id = "id=\"" + idSerramento + "\"";
						active = "status=\"still\" addressopen=\"" + 
							(String)schermo.get("addressopen") + 
							"\" addressclose =\"" + 
							(String)schermo.get("addressclose") +
							"\" onClick=\"blindClicked(event, this)\"";
						idSerramenti.add(idSerramento);
						text = (String)schermo.get("label");
					} else {
						id = "";
						active = "";
						text = "";
					}
					out.print("<div "+ id +" style=\"position:absolute; left: " +
						(int)((Integer)schermo.get("x") * scale) + "px; top: " + 
						(int)((Integer)schermo.get("y") * scale) + "px; color: white;\" " + active + " name=\"" + 
						(String)schermo.get("label") + "\"><div style=\"position: absolute;\"><img src=\"" +
						IMG_BLIND_STILL + "\" alt=\"" + (String)schermo.get("label") + "\" width=\"" + imgWidth + "\" height=\"" + imgHeight + "\"/></div><div style=\"position: absolute; font-size: " + fontSize + ";\"></div></div>");
				} // foreach schermo
			} else {
				out.print("Non ci sono serramenti su questo piano!");
			} 
		} else if (s.equals("video")) {
			frame = (Map)frameVideo.get(piano.get("id"));
			if (frame != null) { 
				Iterator it = frame.keySet().iterator();
				while (it.hasNext()) {
					String idSchermo = (String)it.next();
					Map schermo = (Map)frame.get(idSchermo);
					String active;
					if (clickable) { 
						id = "id=\"" + idSchermo + "\"";
						active = "status=\"still\" addressopen=\"" + 
							(String)schermo.get("addressopen") +
							"\" addressclose =\"" + (String)schermo.get("addressclose") +
							"\" onClick=\"blindClicked(event, this)\"";
						idVideo.add(idSchermo);
						text = (String)schermo.get("label");
					} else {
						id = "";
						active = "";
						text = "";
					}
					out.print("<div "+ id +" style=\"position:absolute; left: " +
						(int)((Integer)schermo.get("x") * scale) + "px; top: " + 
						(int)((Integer)schermo.get("y") * scale) + "px; color: white;\" " + active + " name=\"" + 
						(String)schermo.get("label") + "\"><div style=\"position: absolute;\"><img src=\"" +
						IMG_BLIND_STILL + "\" alt=\"" + (String)schermo.get("label") + "\" width=\"" + imgWidth + "\" height=\"" + imgHeight+ "\"/></div><div style=\"position: absolute; font-size: " + fontSize + ";\"></div></div>");
				} // foreach schermo
			} else {
				out.print("Non ci sono schermi su questo piano!");
			} 
		} else if (s.equals("sicurezza")) {
			frame = (Map)frameSicurezza.get(piano.get("id"));
			if (frame != null) {
				Iterator it = frame.keySet().iterator();
				while (it.hasNext()) {
					String idAllarme = (String)it.next();
					Map allarme = (Map)frame.get(idAllarme);
					String img, active;
					if (clickable) {
						id = "id=\"" + idAllarme + "\"";
						if (allarme.get("type").equals(SIC_PORTA)) {
							active = "status=\"open\" alarm=\"off\" onClick=\"doorClicked(event, this)\"";
							img = IMG_DOOR_OPEN;
						} else { //SIC_LUCCHETTO:
							active = "status=\"off\" onClick=\"lockClicked(event, this)\"";
							img = IMG_LOCK_OPEN;
						}
						idAllarmi.add(idAllarme);
						text = (String)allarme.get("label");
					} else {
						id = "";
						active = "";
						text = "";
						if (allarme.get("type").equals(SIC_PORTA)) {
							img = IMG_DOOR_OPEN;
						} else { // SIC_LUCCHETTO:
							img = IMG_LOCK_OPEN;
						}
					}
					out.print("<div " + id + " style=\"position:absolute; left: " +
						(int)((Integer)allarme.get("x") * scale) + "px; top: " + 
						(int)((Integer)allarme.get("y") * scale) + "px; color: white;\" " + active + " name=\"" + 
						(String)allarme.get("label") + "\"><div style=\"position: absolute;\"><img src=\"" +
						img + "\" alt=\"" + (String)allarme.get("label") + "\" width=\"" + imgWidth + "\" height=\"" + imgHeight + "\"/></div><div style=\"position: absolute; font-size: " + fontSize + ";\"></div></div>");
				} // foreach allarme
			} else {
				out.print("Non ci sono allarmi su questo piano!");
			}
		} else if (s.equals("scenari")) {
			// TODO: farlo vero
			// Riproduciamo l'immagine facendola grande quanto la mappa.
			imgWidth = (int)(((int [])piano.get("bigMapSize"))[0] * scale);
			imgHeight = (int)(((int [])piano.get("bigMapSize"))[1] * scale);
			out.print("<img src=\"" + IMG_SCENARIOS + "\" width=\"" + imgWidth + "\" height=\"" + imgHeight + "\" alt=\"\" />");
		}
	out.println("</div>");
	} // Cicla su tutte le "apps"
}
%>
<%!
/**
 * Ritorna il contenuto di un Vector di String sotto forma di dichiarazione di
 * array Javascript.
 */
static String arrayJavascript(Vector v) {
	String retval = "[";
	Iterator it = v.iterator();
	while (it.hasNext()) {
		retval += "\"" + (String)it.next() + "\", ";
	}
	retval += "]";
	return retval;
}
%>
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
<%
// -------------------------------- Main --------------------------------------
// ConfigurationFile configFile;
// configFile = new ConfigurationFile("../aui/custom/impianto.xml");
%>

<div id="header-out" style="display: none; position: absolute; z-index: 30; width: <%= IPOD_VIEWPORT_WIDTH %>px; height: 40px; filter:alpha(opacity='60'); opacity: <%= STATUS_BAR_OPACITY %>;">
<div style="position: absolute;"><img src="images/barratesti.png" /></div>
<div id="header" style="position: absolute; margin-top: 9px; height: <%= STATUS_BAR_HEIGHT %>px; width: <%= IPOD_VIEWPORT_WIDTH %>px; text-align: center;"><b>barra di stato</b></div>
</div>
<div id="screensaver" title="AUI screensaver - clicca per accedere" onclick="vai('login');"><img
	alt="AUI" src="images/ascia_logo_home.png" /></div>
<div id="login" style="display: none; background: URL(images/groundcalc.png); width: <%= IPOD_VIEWPORT_WIDTH %>px; height: <%= IPOD_VIEWPORT_HEIGHT %>px;">
	<div id="keypadScreen" style="position: absolute; top: 24px; left: 28px; width: 260px; height: 60px; font-size: 60px; overflow: hidden; text-align: center;">&nbsp;</div> 
	<div style="position: absolute; top: 108px; left: 30px;">
		<table id="keypad" title="AUI login - immetti codice personale e premi OK" summary="keypad" cellpadding="0" cellspacing="0"
			border="0">
			<tr>
				<td><img alt="1" width="68" height="60" border="0"
					src="images/key_1.png" onclick="keypadButton(1)" /></td>
				<td><img alt="2" width="68" height="60" border="0"
					src="images/key_2.png" onclick="keypadButton(2)" /></td>
				<td><img alt="3" width="68" height="60" border="0"
					src="images/key_3.png" onclick="keypadButton(3)" /></td>
				<td><img alt="on" width="68" height="60" border="0"
					src="images/key_on.png" onclick="keypadButton('on')"/></td>
			</tr>
			<tr>
				<td><img alt="4" width="68" height="60" border="0"
					src="images/key_4.png" onclick="keypadButton(4)" /></td>
				<td><img alt="5" width="68" height="60" border="0"
					src="images/key_5.png" onclick="keypadButton(5)" /></td>
				<td><img alt="6" width="68" height="60" border="0"
					src="images/key_6.png" onclick="keypadButton(6)" /></td>
				<td><img onclick="keypadButton('x')" alt="annulla" width="68"
					height="60" border="0" src="images/key_x.png" 
					onclick="keypadButton('X')" /></td>
			</tr>
			<tr>
				<td><img alt="7" width="68" height="60" border="0"
					src="images/key_7.png" onclick="keypadButton(7)" /></td>
				<td><img alt="8" width="68" height="60" border="0"
					src="images/key_8.png" onclick="keypadButton(8)" /></td>
				<td><img alt="9" width="68" height="60" border="0"
					src="images/key_9.png" onclick="keypadButton(9)" /></td>
				<td><img alt="cancella" width="68" height="60" border="0"
					src="images/key_back.png" onclick="keypadButton('back')" /></td>
			</tr>
			<tr>
				<td><img alt="*" width="68" height="60" border="0"
					src="images/key_asterisco.png" onclick="keypadButton('*')"/></td>
				<td><img alt="0" width="68" height="60" border="0"
					src="images/key_0.png" onclick="keypadButton(0)" /></td>
				<td><img alt="#" width="68" height="60" border="0"
					src="images/key_cancelletto.png" onclick="keypadButton('#')" /></td>
				<td><img onclick="keypadButton('ok');" title="OK" alt="ok" width="68"
					height="60" border="0" src="images/key_ok.png" 
					onclick="keypadButton('ok')" /></td>
			</tr>
		</table>
	</div>
</div>
<div id="navigazione" style="display: none;">
  <div id="mappa-out" style="width: <%= IPOD_VIEWPORT_WIDTH %>px; height: <%= IPOD_VIEWPORT_HEIGHT - 80 %>px;">
	<div id="mappa"
		style="position: absolute; width: <%= IPOD_VIEWPORT_WIDTH %>px; height: <%= IPOD_VIEWPORT_HEIGHT %>px; overflow: hidden;">
		<div id="piani-all" 
			style="position: absolute; width: <%= pianiSize[0] %>px; height: <%= pianiSize[1] %>px; overflow: hidden;"
			noappbar="noappbar">
			<img 
				header="ASCIA Building"
				title="AUI edificio - clicca su un appartamento" 
				style="position: absolute;"
				onclick="clicca1('piani-all','piano-01A<% if (mobile) out.print("-big"); %>');"
				src="<%= pianiFile %>" alt="" />
		</div>
<%
Iterator it = piani.iterator();
while (it.hasNext()) {
	Map piano = (Map)it.next();
	if (!mobile) {
%>
		<div id="<%= (String)piano.get("id") %>" 
			style="position: absolute; width: <%= ((int [])piano.get("mapSize"))[0] %>px; height: <%= ((int [])piano.get("mapSize"))[1] %>px; overflow: hidden; display: none;"
			onclick="ingrandisci(event,'<%= (String)piano.get("id") %>','<%= (String)piano.get("id") + "-big" %>','piani-all');">
			<img
				header="<%= (String)piano.get("header") %>"
				title="AUI mappa appartamento - clicca per ingrandire - doppio click per ritornare"
				style="position: absolute;"
				src="<%= (String)piano.get("mapFile") %>" alt="" />
			<% creaLayerServizi(piano, apps, false, false, mobile, out); %>
		</div>
	<div id="<%= (String)piano.get("id") + "-big" %>"
			style="position: absolute;
				display: none; 
				width: <%= ((int [])piano.get("bigMapSize"))[0] %>px;
				height: <%= ((int [])piano.get("bigMapSize"))[1] %>px;"
			onclick="clicca('<%= (String)piano.get("id") + "-big" %>','<%= (String)piano.get("id") + "-big" %>','<%= (String)piano.get("id") %>');">
			<img
				header="<%= (String)piano.get("header") %>"
				title="AUI appartamento - doppio click per ritornare"
				style="position: absolute;"
				src="<%= (String) piano.get("bigMapFile") %>" alt="" 
				width="<%= ((int [])piano.get("bigMapSize"))[0] %>"
				height="<%= ((int [])piano.get("bigMapSize"))[1] %>" />
			<% creaLayerServizi(piano, apps, true, true, mobile, out); %>
		</div>
<%	} else { // iPod
%>
	<div id="<%= (String)piano.get("id") + "-big" %>"
		style="position: absolute;
			display: none; 
			width: <%= IPOD_VIEWPORT_WIDTH %>px;
			height: <%= IPOD_MAP_AREA_HEIGHT %>px;"
		onclick="clicca('<%= (String)piano.get("id") + "-big" %>','<%= (String)piano.get("id") + "-big" %>','piani-all');">
		<img
			header="<%= (String)piano.get("header") %>"
			title="AUI appartamento - doppio click per ritornare"
			style="position: absolute;"
			src="<%= (String)piano.get("bigMapFile") %>" alt="" 
			width="<%= ((int [])piano.get("mapSize"))[0] %>"
			height="<%= ((int [])piano.get("mapSize"))[1] %>" />
		<%  creaLayerServizi(piano, apps, false, true, mobile, out); %>
	</div>
<%
	} // if iPod
} // Cicla sui piani
 %>
		<div id="dimmer"
			style="position: absolute; width: <%= IPOD_VIEWPORT_WIDTH %>px; height: <%= IPOD_MAP_AREA_HEIGHT %>px; 
				overflow: hidden; display: none;" onclick="hideDimmer()">
<%
// In pixel
int dimmerCursorTextSize = IMG_DIMMER_CURSOR_HEIGHT / 3;
int dimmerCursorTextTopMargin = IMG_DIMMER_CURSOR_HEIGHT / 3;
%>
			<div style="position: absolute; width: 100%; height: 100%; background-color: black; filter:alpha(opacity='80'); opacity: 0.8;">&nbsp;</div>
			<div id="dimmer-slider" style="position: absolute; width: <%= DIMMER_WIDTH %>px;"
					onclick="dimmerSliderClicked(event)" >
				<div style="position: absolute; height: <%= IMG_DIMMER_SLIDER_TOP_HEIGHT %>px;"><img src="<%= IMG_DIMMER_SLIDER_TOP %>" alt="" width="<%= DIMMER_WIDTH %>" height="<%= IMG_DIMMER_SLIDER_TOP_HEIGHT %>" /></div>
				<div style="position: absolute; top: <%= IMG_DIMMER_SLIDER_TOP_HEIGHT %>px; width: <%=  DIMMER_WIDTH %>px; height: <%=  DIMMER_SLIDER_HEIGHT - 2 * DIMMER_SLIDER_CORNER_HEIGHT %>px; background-image: URL(<%=  IMG_DIMMER_SLIDER_MIDDLE %>);"></div>
				<div style="position: absolute; top: <%= IMG_DIMMER_SLIDER_TOP_HEIGHT + DIMMER_SLIDER_HEIGHT - 2 * DIMMER_SLIDER_CORNER_HEIGHT %>px;"><img src="<%= IMG_DIMMER_SLIDER_BOTTOM %>" width="<%= DIMMER_WIDTH %>" height="<%= IMG_DIMMER_SLIDER_TOP_HEIGHT %>" /></div>
				<div id="dimmer-tasto" style="position: absolute; 
					margin-left: <%= DIMMER_SLIDER_BORDER_WIDTH %>px;">
					<div style="position: absolute;"><img  src="<%= IMG_DIMMER_CURSOR %> " width="<%= IMG_DIMMER_CURSOR_WIDTH %>" height="<%= IMG_DIMMER_CURSOR_HEIGHT %>" /></div>
					<div id="dimmer-tasto-testo" style="position: absolute; text-align: center; width: <%= IMG_DIMMER_CURSOR_WIDTH %>px; top: <%= dimmerCursorTextTopMargin %>px; bottom: auto; font-size: <%= dimmerCursorTextSize %>px;"></div>
					</div>
			</div> <!--  dimmer-sfondo -->
		</div><!--  dimmer -->
		<div id="blind"
			style="position: absolute; width: <%= IPOD_VIEWPORT_WIDTH %>px; height: <%= IPOD_MAP_AREA_HEIGHT %>px; 
				overflow: hidden; display: none;" onclick="blindBackgroundClicked()">
			<div style="position: absolute; width: 100%; height: 100%; background-color: black; filter:alpha(opacity='80'); opacity: 0.8;">&nbsp;</div>
			<div id="blind-control" style="position: absolute; width: <%= IMG_BLIND_CONTROL_WIDTH %>px; height: <%= IMG_BLIND_CONTROL_HEIGHT %>px; background-image: URL(<%= IMG_BLIND_CONTROL %>);"
				onclick="blindControlClicked(event)"></div>
		</div><!--  blind -->
	</div> 
	<!-- fine mappa -->
	</div>
	<div id="appbar-out" style="display: none;">
	<%@ include file="appbar.jsp" %>
	</div>
</div>
<script type="" language="javascript">
	const MOBILE = <%= mobile %>;
	const ID_LUCI = <%= arrayJavascript(idLuci) %>;
	const ID_PRESE = <%= arrayJavascript(idPrese) %>;
	const ID_CLIMI = <%= arrayJavascript(idClimi) %>;
	const ID_SERRAMENTI = <%= arrayJavascript(idSerramenti) %>;
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

<%= includeScript("statusbar.js") %>
<%= includeScript("aui.js") %>
<%= includeScript("comm.js") %>
<%= includeScript("map.js") %>
<%= includeScript("appbar_common.js") %>
<%
if (APPBAR_SIMPLE) {
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

<script type="" language="javascript">
startMasterTimer();
</script>
