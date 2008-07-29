<%
/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
 %>
<%@ page import="java.io.File, java.io.IOException, 
	javax.xml.parsers.DocumentBuilder, 
	javax.xml.parsers.DocumentBuilderFactory,
	javax.xml.parsers.ParserConfigurationException, org.apache.log4j.Logger,
	org.w3c.dom.Document, org.w3c.dom.Element, org.w3c.dom.NodeList,
	org.xml.sax.SAXException, org.xml.sax.SAXParseException" %>
<%@ include file="config.jsp" %>
<%

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

// -------------------------------- Main --------------------------------------
ConfigurationFile configFile;

configFile = new ConfigurationFile("../aui/custom/impianto.xml");

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
			style="position: absolute; width: <?php echo ($pianiSize["w"]); ?>px; height: <?php echo($pianiSize["h"]); ?>px; overflow: hidden;"
			noappbar="noappbar">
			<img 
				header="ASCIA Building"
				title="AUI edificio - clicca su un appartamento" 
				style="position: absolute;"
				onclick="clicca1('piani-all','piano-01A<?php if ($mobile) echo("-big") ?>');"
				src="<?php echo($pianiFile); ?>" alt="" />
		</div>
<?php
foreach ($piani as $piano):
	if (!$mobile) {
 ?>
		<div id="<?php echo($piano["id"]); ?>" 
			style="position: absolute; width: <?php echo($piano["mapSize"]["w"]); ?>px; height: <?php echo($piano["mapSize"]["h"]); ?>px; overflow: hidden; display: none;"
			onclick="ingrandisci(event,'<?php echo($piano["id"]); ?>','<?php echo($piano["id"] . "-big"); ?>','piani-all');">
			<img
				header="<?php echo($piano["header"]); ?>"
				title="AUI mappa appartamento - clicca per ingrandire - doppio click per ritornare"
				style="position: absolute;"
				src="<?php echo($piano["mapFile"]); ?>" alt="" />
			<?php creaLayerServizi($piano, false, false); ?>
		</div>
		<div id="<?php echo($piano["id"] . "-big"); ?>"
			style="position: absolute;
				display: none; 
				width: <?php echo($piano["bigMapSize"]["w"]); ?>px;
				height: <?php echo($piano["bigMapSize"]["h"]); ?>px;"
			onclick="clicca('<?php echo($piano["id"] . "-big"); ?>','<?php echo($piano["id"] . "-big"); ?>','<?php echo($piano["id"]); ?>');">
			<img
				header="<?php echo($piano["header"]); ?>"
				title="AUI appartamento - doppio click per ritornare"
				style="position: absolute;"
				src="<?php echo($piano["bigMapFile"]); ?>" alt="" 
				width="<?php echo($piano["bigMapSize"]["w"]); ?>"
				height="<?php echo($piano["bigMapSize"]["h"]); ?>" />
			<?php creaLayerServizi($piano, true, true); ?>
		</div>
<?php
	} else { // iPod
?>
	<div id="<?php echo($piano["id"] . "-big"); ?>"
		style="position: absolute;
			display: none; 
			width: <%= IPOD_VIEWPORT_WIDTH %>px;
			height: <?php echo(IPOD_MAP_AREA_HEIGHT); ?>px;"
		onclick="clicca('<?php echo($piano["id"] . "-big"); ?>','<?php echo($piano["id"] . "-big"); ?>','piani-all');">
		<img
			header="<?php echo($piano["header"]); ?>"
			title="AUI appartamento - doppio click per ritornare"
			style="position: absolute;"
			src="<?php echo($piano["bigMapFile"]); ?>" alt="" 
			width="<?php echo($piano["mapSize"]["w"]); ?>"
			height="<?php echo($piano["mapSize"]["h"]); ?>" />
		<?php creaLayerServizi($piano, false, true); ?>
	</div>
<?php
	} // if iPod
endforeach; // $piani as $piano
 ?>
		<div id="dimmer"
			style="position: absolute; width: <%= IPOD_VIEWPORT_WIDTH %>px; height: <?php echo(IPOD_MAP_AREA_HEIGHT); ?>px; 
				overflow: hidden; display: none;" onclick="hideDimmer()">
<?php
$temp = getimagesize(IMG_DIMMER_SLIDER_TOP);
$dimmerWidth = $temp[0] ;
$dimmerTopHeight = $temp[1] ;
$temp = getImageSize(IMG_DIMMER_SLIDER_BOTTOM);
$dimmerBottomHeight = $temp[1] ;
$temp = getImageSize(IMG_DIMMER_CURSOR);
$dimmerCursorWidth = $temp[0] ;
$dimmerCursorHeight = $temp[1] ;
// In pixel
$dimmerCursorTextSize = $dimmerCursorHeight / 3;
$dimmerCursorTextTopMargin = $dimmerCursorHeight / 3;
?>
			<div style="position: absolute; width: 100%; height: 100%; background-color: black; filter:alpha(opacity='80'); opacity: 0.8;">&nbsp;</div>
			<div id="dimmer-slider" style="position: absolute; width: <?php echo($dimmerWidth); ?>px;"
					onclick="dimmerSliderClicked(event)" >
				<div style="position: absolute; height: <?php echo ($dimmerTopHeight); ?>px;"><img src="<?php echo(IMG_DIMMER_SLIDER_TOP); ?>" alt="" width="<?php echo($dimmerWidth); ?>" height="<?php echo($dimmerTopHeight); ?>" /></div>
				<div style="position: absolute; top: <?php echo($dimmerTopHeight); ?>px; width: <?php echo ($dimmerWidth); ?>px; height: <?php echo (DIMMER_SLIDER_HEIGHT - 2 * DIMMER_SLIDER_CORNER_HEIGHT ); ?>px; background-image: URL(<?php echo(IMG_DIMMER_SLIDER_MIDDLE); ?>);"></div>
				<div style="position: absolute; top: <?php echo($dimmerTopHeight + DIMMER_SLIDER_HEIGHT - 2 * DIMMER_SLIDER_CORNER_HEIGHT ); ?>px;"><img src="<?php echo(IMG_DIMMER_SLIDER_BOTTOM); ?>" width="<?php echo($dimmerWidth); ?>" height="<?php echo($dimmerTopHeight); ?>" /></div>
				<div id="dimmer-tasto" style="position: absolute; 
					margin-left: <?php echo(DIMMER_SLIDER_BORDER_WIDTH ); ?>px;">
					<div style="position: absolute;"><img  src="<?php echo(IMG_DIMMER_CURSOR); ?> " width="<?php echo($dimmerCursorWidth); ?>" height="<?php echo($dimmerCursorHeight); ?>" /></div>
					<div id="dimmer-tasto-testo" style="position: absolute; text-align: center; width: <?php echo($dimmerCursorWidth); ?>px; top: <?php echo($dimmerCursorTextTopMargin); ?>px; bottom: auto; font-size: <?php echo($dimmerCursorTextSize); ?>px;"></div>
					</div>
			</div> <!--  dimmer-sfondo -->
		</div><!--  dimmer -->
		<div id="blind"
			style="position: absolute; width: <%= IPOD_VIEWPORT_WIDTH %>px; height: <?php echo(IPOD_MAP_AREA_HEIGHT); ?>px; 
				overflow: hidden; display: none;" onclick="blindBackgroundClicked()">
<?php
$temp = getimagesize(IMG_BLIND_CONTROL);
$blindControlWidth = $temp[0];
$blindControlHeight = $temp[1];
?>
			<div style="position: absolute; width: 100%; height: 100%; background-color: black; filter:alpha(opacity='80'); opacity: 0.8;">&nbsp;</div>
			<div id="blind-control" style="position: absolute; width: <?php echo($blindControlWidth); ?>px; height: <?php echo($blindControlHeight); ?>px; background-image: URL(<?php echo(IMG_BLIND_CONTROL); ?>);"
				onclick="blindControlClicked(event)"></div>
		</div><!--  blind -->
	</div> 
	<!-- fine mappa -->
	</div>
	<div id="appbar-out" style="display: none;">
<?php include('appbar.php'); ?>
	</div>
</div>
<script type="" language="javascript">
	const MOBILE = <?php if($mobile) echo "true"; else echo "false"; ?>;
	const ID_LUCI = <?php arrayJavascript($idLuci); ?>;
	const ID_PRESE = <?php arrayJavascript($idPrese); ?>;
	const ID_CLIMI = <?php arrayJavascript($idClimi); ?>;
	const ID_SERRAMENTI = <?php arrayJavascript($idSerramenti); ?>;
	const IMG_LIGHT_ON = "<?php echo(IMG_LIGHT_ON); ?>";
	const IMG_LIGHT_OFF = "<?php echo(IMG_LIGHT_OFF); ?>";
	const IMG_POWER_ON = "<?php echo(IMG_POWER_ON); ?>";
	const IMG_POWER_OFF = "<?php echo(IMG_POWER_OFF); ?>";
	const IMG_THERMO_ON = "<?php echo(IMG_THERMO_ON); ?>";
	const IMG_THERMO_OFF = "<?php echo(IMG_THERMO_OFF); ?>";
	const IMG_BLIND_STILL = "<?php echo(IMG_BLIND_STILL); ?>";
	const IMG_BLIND_OPENING = "<?php echo(IMG_BLIND_OPENING); ?>";
	const IMG_BLIND_CLOSING = "<?php echo(IMG_BLIND_CLOSING); ?>";
	const STATUS_BAR_HEIGHT = <?php echo(STATUS_BAR_HEIGHT); ?>;
	const STATUS_BAR_OPACITY = "<?php echo(STATUS_BAR_OPACITY); ?>";
	const APPBAR_START_POSITION = <?php echo(APPBAR_START_POSITION); ?>;
	const DIMMER_SLIDER_HEIGHT = <?php echo(DIMMER_SLIDER_HEIGHT); ?>;
	const DIMMER_TOP_MIN = <?php echo($dimmerTopHeight - DIMMER_SLIDER_CORNER_HEIGHT ); ?>;
	const DIMMER_TOP_MAX = <?php echo($dimmerTopHeight - DIMMER_SLIDER_CORNER_HEIGHT  + DIMMER_SLIDER_HEIGHT - $dimmerCursorHeight); ?>;
	const DIMMER_CURSOR_MIDDLE = <?php echo($dimmerCursorHeight / 2); ?>;
	const DIMMER_SLIDER_TOTAL_HEIGHT = <?php echo ($dimmerTopHeight + DIMMER_SLIDER_HEIGHT - 2 * DIMMER_SLIDER_CORNER_HEIGHT + $dimmerBottomHeight); ?>;
	const DIMMER_SLIDER_WIDTH = <?php echo($dimmerWidth); ?>;
	const IMG_BLIND_CONTROL = "<?php echo(IMG_BLIND_CONTROL); ?>";
	const BLIND_UP_TOP = <?php echo(BLIND_UP_TOP); ?>;
	const BLIND_LEFT = <?php echo(BLIND_LEFT); ?>;
	const BLIND_UP_BOTTOM = <?php echo(BLIND_UP_BOTTOM); ?>;
	const BLIND_RIGHT = <?php echo(BLIND_RIGHT); ?>;
	const BLIND_DOWN_TOP = <?php echo(BLIND_DOWN_TOP); ?>;
	const BLIND_DOWN_BOTTOM = <?php echo(BLIND_DOWN_BOTTOM); ?>;
	const BLIND_CONTROL_HEIGHT = <?php echo($blindControlHeight); ?>;
	const BLIND_CONTROL_WIDTH = <?php echo($blindControlWidth); ?>;
	const MAP_AREA_WIDTH = <%= IPOD_VIEWPORT_WIDTH %>;
	const MAP_AREA_HEIGHT = <?php echo(IPOD_MAP_AREA_HEIGHT); ?>;
	const IMG_LOCK_OPEN = "<?php echo(IMG_LOCK_OPEN); ?>";
	const IMG_LOCK_CLOSE = "<?php echo(IMG_LOCK_CLOSE); ?>";
	const IMG_DOOR_OPEN = "<?php echo(IMG_DOOR_OPEN); ?>";
	const IMG_DOOR_CLOSE = "<?php echo(IMG_DOOR_CLOSE); ?>";
	const IMG_DOOR_OPEN_ALARM = "<?php echo(IMG_DOOR_OPEN_ALARM); ?>";
	const IMG_DOOR_CLOSE_OK = "<?php echo(IMG_DOOR_CLOSE_OK); ?>";
</script>

<?php
includeScript("statusbar.js");
includeScript("aui.js");
includeScript("comm.js");
includeScript("map.js");
includeScript("appbar_common.js");
if (APPBAR_SIMPLE) {
	includeScript("appbar_simple.js");
} else {
	includeScript("appbar.js");
}
includeScript("services.js");
includeScript("dimmer_slider.js");
includeScript("blind.js");
includeScript("keypad.js");
includeScript("alarm.js");
?>
<script type="" language="javascript">
startMasterTimer();
</script>
