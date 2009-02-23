/**
 * Copyright (C) 2008 ASCIA S.r.l.
 */
package it.ascia.ais;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Questa classe deve essere estesa da controllori per bus domotici.
 * 
 * @author arrigo
 */
public abstract class Controller {
	/**
	 * Connector registrati.
	 * 
	 * <p>I Connector qui dentro sono accessibili dal loro nome (stringa).</p>
	 */
	private Map connectors;
	
	/**
	 * Plugins attivi
	 */
	private Map plugins;
	
	protected Logger logger; 
	
	/**
	 * Rifa' String.split() per il GCJ che non ce l'ha.
	 */
	private static String[] splitString(String s, String separator) {
		String retval[], current = "";
		int i = 0, strings = 1, stringNo = 0, lastIndex = 0;
		i = s.indexOf(separator, i);
		while (i != -1) {
			strings++;
			i = s.indexOf(separator, i + 1);
		}
		retval = new String[strings];
		i = s.indexOf(separator, lastIndex);
		while (i != -1) {
			retval[stringNo] = s.substring(lastIndex, i);
			stringNo++;
			lastIndex = i + 1;
			i = s.indexOf(separator, lastIndex);
		}
		// Anche l'ultima
		retval[stringNo] = s.substring(lastIndex);
		return retval;
	}
	
	/**
	 * Aggiunge un Connector alla lista di quelli gestiti.
	 * 
	 * @param connector il connector da aggiungere.
	 */
	public void registerConnector(Connector connector) {
		connectors.put(connector.getName(), connector);
	}
	
	public void loadPlugin(String name) {
		String className = "it.ascia.ais."+name+"ControllerPlugin"; 
		loadPlugin(name, className);
	}
	
	public void loadPlugin(String name, String className) {
		ClassLoader pluginLoader = ControllerPlugin.class.getClassLoader();
		try {
			Class pluginClass = pluginLoader.loadClass(className);
			ControllerPlugin plugin = (ControllerPlugin) pluginClass.newInstance();
			plugin.setController(this);
			plugins.put(name, plugin);
			logger.info("Caricato plugin '"+name+"' da '"+className+"'");
			plugin.configure();
		} catch (ClassNotFoundException e) {
			logger.error("Fallito caricamento plugin '"+name+"': non trovata classe '"+className+"'");
		} catch (InstantiationException e) {
			logger.error("Fallito caricamento plugin '"+name+"': errore instanzazione classe '"+className+"'");
		} catch (IllegalAccessException e) {
			logger.error("Fallito caricamento plugin '"+name+"': accesso negato alla classe '"+className+"'");
		}
	}
	
	/**
	 * Effettua il parsing di un indirizzo nella forma "device:porta".
	 * 
	 * @return il nome del Device indicato nell'indirizzo (la parte prima dei 
	 * ":").
	 * 
	 * @throws un'eccezione se l'indirizzo non e' valido.
	 */
	protected String getDeviceFromAddress(String address) throws AISException {
		String temp[];
		// Prima cosa: trovare i ":" per dividere porte e device.
		temp = splitString(address, ":");
		if (temp.length != 2) {
			throw new AISException("L'indirizzo deve contenere uno e un solo " +
					"\":\"");
		}
		return temp[0];
	}
	
	/**
	 * Effettua il parsing di un indirizzo nella forma "device:porta".
	 * 
	 * @return il nome della porta indicata nell'indirizzo (la parte dopo i 
	 * ":").
	 * 
	 * @throws un'eccezione se l'indirizzo non e' valido.
	 */
	protected String getPortFromAddress(String address) throws AISException {
		String temp[];
		// Prima cosa: trovare i ":" per dividere porte e device.
		temp = splitString(address, ":");
		if (temp.length != 2) {
			throw new AISException("L'indirizzo deve contenere uno e un solo " +
					"\":\"");
		}
		return temp[1];
	}

	
	/**
	 * Cerca uno o piu' Device a partire da un indirizzo.
	 * 
	 * <p>Perche' i Device siano rintracciabili, il loro Connector deve essere
	 * stato preventivamente registrato usando registerConnector.
	 * 
	 * @param address indirizzo del/dei Device.
	 * 
	 * @return i Device rispondenti all'indirizzo indicato.
	 */
	protected Device[] findDevices(String address) throws AISException {
		String connectorName = null;
		String deviceAddress = null;
		Connector connector = null;
		Device retval[];
		Iterator it;
		if (address.equals("*")) {
			// "*" e' una scorciatoia per "*.*"
			address = "*.*";
		}
		retval = new Device[0];
		it = connectors.keySet().iterator();
		while (it.hasNext()) {
			connectorName = (String)it.next();
			if ((address.indexOf("*.") == 0) || 
					(address.indexOf(connectorName + ".") == 0)) {
				// Questo connector ci interessa!
				int deviceNameIndex;
				connector = (Connector)connectors.get(connectorName);
				// Dove inizia l'indirizzo del Device?
				if (address.indexOf("*") == 0) {
					 // L'indirizzo ha la forma "*.nome"
					deviceNameIndex = 2;
				} else {
					// L'indirizzo ha la forma "connector.nome"
					deviceNameIndex = connectorName.length() + 1;
				}
				try {
					Device temp[], temp2[];
					deviceAddress = address.substring(deviceNameIndex);
					temp = connector.getDevices(deviceAddress);
					// Concateniamo temp e retval
					temp2 = new Device[retval.length + temp.length];
					System.arraycopy(retval, 0, temp2, 0, 
							retval.length);
					System.arraycopy(temp, 0, temp2, retval.length, 
							temp.length);
					retval = temp2;
				} catch (StringIndexOutOfBoundsException e) {
					throw new AISException("Impossibile distinguere " +
							"connector e device nell'indirizzo \"" + 
							address + "\"");
				}
			} // Se il connector ci interessa
		} // Cicla sui connector
		if (connector == null) {
			// Nessun connector ci e' andato bene
			throw new AISException("Impossibile trovare il connector per " +
					"l'indirizzo \"" + address + "\".");
		}
		if (retval.length == 0) {
			throw new AISException("Il device " + address + " non " +
					"esiste");
		}
		return retval;
	}
	
	public Controller() {
		connectors = new HashMap();
		plugins = new HashMap();
		logger = Logger.getLogger(getClass());
	}
	
	/**
	 * Il cuore del controllore: riceve la richiesta e produce una risposta.
	 * 
	 * @param command comando
	 * @param name indirizzo del/dei device interessati
	 * @param value parametri del comando (puo' essere null)
	 * @param pin pin
	 */
	public abstract String receiveRequest(String command, String name, 
			String value, String pin);

	/**
	 * Comunica l'evento a tutti i plugin
	 * 
	 * @param event
	 */
	public void onDeviceEvent(DeviceEvent event) {
		Iterator i = plugins.keySet().iterator();
		while (i.hasNext()) {
			ControllerPlugin plugin = (ControllerPlugin) i.next();
			plugin.onDeviceEvent(event);
		}
	}
}

