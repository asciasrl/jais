/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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
	 * Il nome del sistema domotico.
	 */
	private String systemName;
	/**
	 * Ritorna il nome del sistema domotico.
	 */
	public String getSystemName() {
		return systemName;
	}
	/**
	 * Estrae le informazioni di configurazione dal Document interpretato.
	 */
	private void parseDocument() throws EDSException {
		Element element, elem2;
		NodeList list;
		int i;
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
		list = element.getElementsByTagName("dispositivo");
		for (i = 0; i < list.getLength(); i++) {
			element = (Element)list.item(i);
			// TODO:
			// - crea oggetti per ciascun elemento del file di cfg
			// - implementa un metodo di controllo se due oggetti BMC sono
			//   operativamente uguali (indirizzo, modello)
			// - gestisci la possibilità di aggiungere device al bus sia da cfg,
			//   sia da discovery
		}
	}
	/**
	 * Effettua il parsing XML del file di configurazione.
	 * 
	 * L'operazione di lettura dei dati e' affidata a parseDocument().
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
	 * @param fileName the configuration file name.
	 */
	public ConfigurationFile(String fileName) throws EDSException {
		this.fileName = fileName;
		parse();
	}
}
