/**
 * Copyright (C) 2008 ASCIA S.r.l.
 */
package it.ascia.ais;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.log4j.Logger;

/**
 * Controllore del sistema di integrazione, esteso tramite moduli.
 * 
 * @author arrigo
 */
public class Controller {
	/**
	 * Connector registrati.
	 * 
	 * <p>I Connector qui dentro sono accessibili dal loro nome (stringa).</p>
	 */
	private Map connectors;
	
	/**
	 * Moduli del controllore
	 */
	private Map modules;
	
	private Logger logger;
	
	private XMLConfiguration config;

	private static Controller controller;
		
	public static Controller getController() {
		return controller;
	}

	private static void setController(Controller c) {
		controller = c;
	}
	
	public XMLConfiguration getConfig() {
		return config;
	}
	
	/**
	 * Rifa' String.split() per il GCJ che non ce l'ha.
	 */
	private static String[] splitString(String s, String separator) {
		String retval[];
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
	
	public ControllerModule getModule(String name) {
		return (ControllerModule) modules.get(name);
	}
	
	
	/**
	 * Carica il modulo, lo instanzia, ne effettua la configurazione e quindi lo avvia. 
	 * @param name Nome (unico) del modulo
	 * @param className Classe che implementa il modulo
	 * @throws AISException 
	 */
	private void loadModule(String name, String className) throws AISException {
		if (modules.containsKey(name)) {
			throw(new AISException("Nome modulo duplicato: '"+name+"'"));
		}
		ClassLoader moduleLoader = ControllerModule.class.getClassLoader();
		ControllerModule module = null;
		try {
			logger.debug("Caricamento modulo '"+name+"' da '"+className+"'");
			Class moduleClass = moduleLoader.loadClass(className);
			module = (ControllerModule) moduleClass.newInstance();
			module.setController(this);
			module.setConfiguration(config);
			modules.put(name,module);
			logger.info("Caricato modulo '"+name+"'");
		} catch (ClassNotFoundException e) {
			logger.error("Fallito caricamento modulo '"+name+"': non trovata classe '"+className+"'");
		} catch (InstantiationException e) {
			logger.error("Fallito caricamento modulo '"+name+"': errore instanzazione classe '"+className+"'");
		} catch (IllegalAccessException e) {
			logger.error("Fallito caricamento modulo '"+name+"': accesso negato alla classe '"+className+"'");
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
	public String getDeviceFromAddress(String address) throws AISException {
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
	public String getPortFromAddress(String address) throws AISException {
		String temp[];
		// Prima cosa: trovare i ":" per dividere porte e device.
		temp = splitString(address, ":");
		if (temp.length != 2) {
			throw new AISException("L'indirizzo deve contenere uno e un solo " +
					"\":\"");
		}
		return temp[1];
	}

	public Connector getConnector(String name) {
		return (Connector) connectors.get(name);
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
	public Device[] findDevices(String address) throws AISException {
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
		this("conf/jais.xml");
	}
	
	/**
	 * 
	 * @param configurationFileName 
	 * @throws ConfigurationException 
	 */
	public Controller(String configurationFileName) {
		connectors = new HashMap();
		modules = new HashMap();
		logger = Logger.getLogger(getClass());	
		try {
			XMLConfiguration.setDefaultListDelimiter(';');
			config = new XMLConfiguration(configurationFileName);
			config.setReloadingStrategy(new FileChangedReloadingStrategy());
		} catch (ConfigurationException e) {
			logger.fatal(e);
		}
		Controller.setController(this);
		logger.info("Inizializzato controller.");
	}

	public void configure() {
		List modules = config.configurationsAt("modules.module");
		for(Iterator it = modules.iterator(); it.hasNext();)
		{
		    HierarchicalConfiguration sub = (HierarchicalConfiguration) it.next();
		    String name = sub.getString("name");
		    String className = sub.getString("class");
		    try {
				loadModule(name, className);
			} catch (AISException e) {
				logger.fatal(e);
			}
		}		
	}
	
	/**
	 * Comunica l'evento a tutti i moduli
	 * 
	 * @param event
	 */
	public void onDeviceEvent(DeviceEvent event) {
		logger.info("Ricevuto evento: "+event.getInfo());
		Iterator i = modules.keySet().iterator();
		while (i.hasNext()) {
			String moduleName = (String) i.next();
			ControllerModule module = getModule(moduleName);
			module.onDeviceEvent(event);
		}
	}

	/**
	 * Avvia tutti i moduli
	 */
	public void start() {
		Iterator i = modules.keySet().iterator();
		while (i.hasNext()) {
			ControllerModule module = getModule((String) i.next());
			module.start();		
		}
	}

	/**
	 * Ferma tutti i moduli
	 */
	public void stop() {
		Iterator i = modules.keySet().iterator();
		while (i.hasNext()) {
			ControllerModule module = getModule((String) i.next());
			module.stop();			
		}		
	}
}

