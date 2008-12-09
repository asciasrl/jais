/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds;

import it.ascia.eds.device.BMC;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Le informazioni contenute in un file di configurazione EDS.
 * 
 * @author arrigo
 */
public class ConfigurationFile {
	/**
	 * Il nome del file di configurazione.
	 */
	private String fileName;
	/**
	 * Il risultato del parsing XML.
	 */
	private Document document;
	/**
	 * La lista degli elementi <dispositivo>.
	 * 
	 * Gli elementi sono di tipo Element.
	 */
	private NodeList dispositivoElements;
	/**
	 * Il nome del sistema domotico.
	 */
	private String systemName;
	/**
	 * Il nostro logger.
	 */
	private Logger logger;
	
	/**
	 * Costruttore.
	 * @param fileName the configuration file name.
	 */
	public ConfigurationFile(String fileName) throws EDSException {
		this.fileName = fileName;
		logger = Logger.getLogger(getClass());
		parse();
	}
	
	/**
	 * Ritorna il nome del sistema domotico.
	 */
	public String getSystemName() {
		return systemName;
	}
	
	/**
	 * Effettua una prima verifica del documento interpretato.
	 * 
	 * Questo metodo viene chiamato solo dal costruttore.
	 */
	private void parseDocument() throws EDSException {
		Element element;
		NodeList list;
		element = document.getDocumentElement();
		// Primo tag: <home>
		if (!element.getTagName().equals("home")) {
			throw new EDSException("Primo tag non riconosciuto: " +
					element.getTagName());
		}
		// Secondo tag: <edsnetwork nome="systemName">
		list = element.getElementsByTagName("edsnetwork");
		if (list.getLength() < 1) {
			throw new EDSException("Il tag <edsnetwork> e' vuoto!");
		}
		element = (Element)list.item(0);
		systemName = element.getAttribute("nome");
		// Tag <dispositivo> (sono tanti!)
		dispositivoElements = element.getElementsByTagName("dispositivo");
	}
	
	/**
	 * Effettua il parsing XML del file di configurazione.
	 * 
	 * L'operazione di lettura dei dati e' affidata a parseDocument() e a
	 * createBMCs().
	 */
	private void parse() throws EDSException {
		try {
			DocumentBuilderFactory factory;
			DocumentBuilder builder;
			factory = DocumentBuilderFactory.newInstance();
			builder = factory.newDocumentBuilder();
			document = builder.parse(new File(fileName));
			parseDocument();
		} catch (SAXParseException e) {
			throw new EDSException("Errore durante il parsing, linea " +
					e.getLineNumber() + ", uri " + e.getSystemId() + ": " +
					e.getMessage());
		} catch (SAXException e) {
			throw new EDSException("Errore SAX durante il parsing: " +
					e.getMessage());
		} catch (ParserConfigurationException e) {
			throw new EDSException("Errore di configurazione del parser: " +
					e.getMessage());
		} catch (IOException e) {
			throw new EDSException("Errore di I/O durante il parsing: " +
					e.getMessage());
		}
	}
	
	/**
	 * Legge il contenuto numerico (intero) di un tag XML.
	 * 
	 * In pratica, legge "10" da "<tag>10</tag>".
	 * 
	 * @param element l'elemento XML "padre" del tag.
	 * @param tag il tag che contiene il dato da leggere.
	 */
	private static int getIntegerTagContent(Element element, String tag) {
		Element tagElement = (Element) 
			element.getElementsByTagName(tag).item(0);
		return Integer.parseInt(tagElement.getFirstChild().getNodeValue());
	}
	
	/**
	 * Inserisce nel transport i BMC elencati nel file di configurazione.
	 * 
	 * Le informazioni inserite sono: nome, modello, indirizzo e nomi delle
	 * porte di input/output.
	 * 
	 * @param transport il transport a cui collegare i BMC.
	 */
	public void createBMCs(EDSConnector bus) {
		int i;
		for (i = 0; i < dispositivoElements.getLength(); i++) {
			Element dispositivoElement = (Element)dispositivoElements.item(i);
			try {
				BMC bmc;
				String name = dispositivoElement.getAttribute("nome");
				int address = getIntegerTagContent(dispositivoElement, 
						"indirizzo"); 
				int model = getIntegerTagContent(dispositivoElement, "modello");
				bmc = BMC.createBMC(address, model, name, bus, true);
				if (bmc != null) {
					int j;
					NodeList lista;
					// Ingressi
					lista =	dispositivoElement.getElementsByTagName("ingresso");
					for (j = 0; j < lista.getLength(); j++) {
						Element ingressoElement = (Element) lista.item(j);
						String portName = ingressoElement.getAttribute("nome");
						// gli ingressi partono da 1 
						// e non da 0 per _alcuni_ tipi di BMC!!
						int number = getIntegerTagContent(ingressoElement, 
								"indirizzo");
						number -= bmc.getFirstInputPortNumber();
						bmc.setInputName(number, portName);
					}
					// Uscite
					lista =	dispositivoElement.getElementsByTagName("uscita");
					for (j = 0; j < lista.getLength(); j++) {
						Element uscitaElement = (Element) lista.item(j);
						String portName = uscitaElement.getAttribute("nome");
						int number = getIntegerTagContent(uscitaElement, 
								"indirizzo");
						bmc.setOutputName(number, portName);
					}
				} // if bmc != null
			} catch (EDSException e) {
				logger.error("Durante la lettura del file di config.: " +
						e.getMessage());
			}
		} // for su tutti i dispositivi
	}
}
