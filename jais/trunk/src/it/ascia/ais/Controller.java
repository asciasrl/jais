/**
 * Copyright (C) 2008 ASCIA S.r.l.
 */
package it.ascia.ais;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import javax.management.openmbean.KeyAlreadyExistsException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * Controllore del sistema di integrazione, esteso tramite moduli.
 * Il Controllore, di cui esiste una sola instanza nel sistema, gestisce il 
 * ciclo di vita delle istanze dei {@link ControllerModule}, che a loro volta gestiscono 
 * i differenti aspetti della applicazione ed in particolare dei {@link Connector}.
 * Gestisce il caricamento del file di configurazione di sistema e del sottosistema di logging.
 * Fornisce alcuni metodi per l'accesso alle instanze ai connettori registrati da moduli.
 * Riceve e distribuisce i {@link DevicePortChangeEvent} ai Listener che si registrano.     
 * 
 * @author arrigo
 * @author sergio
 */
public class Controller {
	
	public static final String CONFIGURATION_VERSION = "1.4";

	/**
	 * Connector registrati.
	 * 
	 * <p>I Connector qui dentro sono accessibili dal loro nome (stringa).</p>
	 */
	private LinkedHashMap<String, Connector> connectors = new LinkedHashMap<String,Connector>();

	/**
	 * Moduli del controllore
	 */
	private Map modules = new LinkedHashMap();
	
	private Logger logger;
		
	private XMLConfiguration config;

	private static Controller controller;

	/**
	 * Controller singleton
	 * @return Il Controller del sistema
	 * @throws AISException 
	 */
	public static Controller getController() {
		if (controller == null) {
			controller = new Controller();
		}
		return controller;
	}

	public XMLConfiguration getConfig() {
		return config;
	}
	
	/**
	 * Aggiunge un Connector alla lista di quelli gestiti.
	 * 
	 * @param connector il connector da aggiungere.
	 * @throws KeyAlreadyExistsException if a connector with the same name is already registered 
	 */
	public void registerConnector(Connector connector) throws KeyAlreadyExistsException {
		if (connectors.containsKey(connector.getName())) {
			throw(new KeyAlreadyExistsException("Connector name duplicated: "+connector.getName()));
		}
		connectors.put(connector.getName(), connector);
	}
	
	/**
	 * 
	 * @param name Name of the ControllerModule
	 * @return 
	 */
	public ControllerModule getModule(String name) {
		return (ControllerModule) modules.get(name);
	}
	
	
	/**
	 * Carica il modulo, lo instanzia, ne imposta la configurazione. 
	 * @param name Nome (unico) del modulo
	 * @param className Classe che implementa il modulo
	 * @param configName 
	 * @throws AISException 
	 * @deprecated Use module factory
	 */
	private void loadModule(String name, String className, String configName) throws AISException {
		if (modules.containsKey(name)) {
			throw(new AISException("Nome modulo duplicato: '"+name+"'"));
		}
		ClassLoader moduleLoader = ControllerModule.class.getClassLoader();
		ControllerModule module = null;
		try {
			logger.debug("Caricamento modulo '"+name+"' da '"+className+"'");
			Class moduleClass = moduleLoader.loadClass(className);
			module = (ControllerModule) moduleClass.newInstance();
			module.setName(name);
		    if (configName == null) {		    	
				module.setConfiguration(config);
		    } else {
				XMLConfiguration moduleConfig = new XMLConfiguration(configName);
				moduleConfig.setReloadingStrategy(new FileChangedReloadingStrategy());
				module.setConfiguration(moduleConfig);
				logger.info("Caricata configurazione modulo '"+name+"' da "+configName);		
		    }
		    // FIXME Use registerModule()
			modules.put(name,module);
			logger.info("Caricato modulo '"+name+"'");
		} catch (ClassNotFoundException e) {
			logger.fatal("Fallito caricamento modulo '"+name+"': non trovata classe '"+className+"'");
		} catch (InstantiationException e) {
			logger.fatal("Fallito caricamento modulo '"+name+"': errore instanzazione classe '"+className+"'");
		} catch (IllegalAccessException e) {
			logger.fatal("Fallito caricamento modulo '"+name+"': accesso negato alla classe '"+className+"'");
		} catch (ConfigurationException e) {
			logger.fatal("Fallito caricamento modulo '"+name+"': Errore nel file di configurazione:",e);
		}
	}
	
	/**
	 * 
	 * @param name Name of connector
	 * @return Connector
	 */
	public Connector getConnector(String name) {
		return connectors.get(name);
	}

	/**
	 * 
	 * @param address 
	 * @return Devices that match the address
	 */
	public Vector<Device> getDevices(Address address) {
		Vector<Device> res = new Vector<Device>();
		for (Connector connector : connectors.values()) {
			if (address.matchConnector(connector.getName())) {
				res.addAll(connector.getDevices(address));			
			}
		}
		return res;
	}
	
	public Vector<Device> getDevices(String address) {
		return getDevices(new Address(address));
	}
	
	
	/**
	 * Inizializza il logger con {@link BasicConfigurator}
	 */
	protected Controller() {
    	BasicConfigurator.configure();  // configurazione minimale di log4j
		logger = Logger.getLogger(getClass());	
		Logger.getRootLogger().setLevel(Level.INFO);
	}
	
	/**
	 * Configura il Controller usando il file di configurazione di default: conf/jais.xml 
	 */
	public void configure() {
		configure("conf/jais.xml");
	}

	/**
	 * Configura il controller in base a quanto specificato nel file di configurazione.
	 * In particolare:
	 * <ol>
	 * <li>Inizializza il Logger</li>
	 * <li>Imposta la lingua Locale</li>
	 * <li>Carica i moduli</li>
	 * </ol>
	 * 
	 * @see LoadModule
	 * 
	 * @param configurationFileName
	 */
	public void configure(String configurationFileName) {
		try {
			XMLConfiguration.setDefaultListDelimiter(';');
			config = new XMLConfiguration(configurationFileName);
		} catch (ConfigurationException e) {
			logger.fatal("Errore nel file di configurazione "+configurationFileName,e);
			return;
		}
		config.setReloadingStrategy(new FileChangedReloadingStrategy());
		String configurationVersion = config.getString("[@version]");
		// Inizializzazione logger
	    String loggerConfigFileName = config.getString("logger[@file]","conf/log4j.xml");
	    String loggerConfiguratorName = config.getString("logger[@configurator]","DOMConfigurator"); 
	    if (loggerConfiguratorName.equals("DOMConfigurator")) {
	    	DOMConfigurator.configure(loggerConfigFileName);  
	    } else if (loggerConfiguratorName.equals("PropertyConfigurator")) {
	    	PropertyConfigurator.configure(loggerConfigFileName);
	    } else {
	    	logger.fatal("Configuratore file di log sconosciuto:"+loggerConfiguratorName);
	    }
		logger = Logger.getLogger(getClass());	
		logger.info("Caricata configurazione versione "+configurationVersion+" da "+configurationFileName);		

		// Impostazione locale
		Locale.setDefault(new Locale(config.getString("locale[@language]","it"),config.getString("locale[@country]","IT")));
		logger.info("Default locale: " + Locale.getDefault());
		
		// caricamento moduli
		List modules = config.configurationsAt("modules.module");
		for(Iterator it = modules.iterator(); it.hasNext();)
		{
		    HierarchicalConfiguration sub = (HierarchicalConfiguration) it.next();
		    String name = (String) sub.getProperty("[@name]");
		    String className = sub.getString("class");
		    String configName = sub.getString("config",null);
		    try {
				loadModule(name, className, configName);
			} catch (AISException e) {
				logger.fatal("Errore caricamento modulo:",e);
			}
		}		
		logger.info("Configurato controller.");
	}
	
	/**
	 * Avvia il controller
	 * @param args Come unico parametro accetta il nome del file di configurazione 
	 * @throws AISException 
	 */
	public static void main(String[] args) throws AISException {		
		Controller c = Controller.getController();
		Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
            	this.setName("ShutdownHook");
            	Logger logger = Logger.getLogger(getClass());
            	logger.info("Shutdown initiated");
            	Controller.getController().stop();
            }
        });
		if (args.length > 0) {
			c.configure(args[0]);			
		} else {
			c.configure();
		}
		c.start();
	}

	/**
	 * Avvia tutti i moduli
	 * 
	 * @see ControllerModule.start
	 */
	public void start() throws AISException {
		Iterator i = modules.keySet().iterator();
		logger.info("Avvio di "+modules.size()+" moduli");
		long start = System.currentTimeMillis();
		try {
			while (i.hasNext()) {
				long start1 = System.currentTimeMillis();
				String moduleName = (String) i.next();
				ControllerModule module = getModule(moduleName);
				logger.info("Avvio modulo "+moduleName);
				module.start();
				if (module.isRunning()) {
					logger.debug("Started module "+moduleName+" in "+(System.currentTimeMillis()-start1)/1000.0+" seconds.");
				} else {
					logger.fatal("Unable to start module: " + moduleName);
				}
			}
			logger.debug("Avviati "+modules.size()+" moduli in "+(System.currentTimeMillis()-start)/1000.0+" secondi.");			
		} catch (Exception e) {
			logger.fatal("Start exception:",e);
			stop();
			throw(new AISException("Cannot start all modules"));
		}
	}

	/**
	 * Ferma tutti i moduli
	 */
	public void stop() {
		Iterator i = modules.keySet().iterator();
		logger.info("Arresto di "+modules.size()+" moduli");
		while (i.hasNext()) {
			String moduleName = (String) i.next();
			ControllerModule module = getModule(moduleName);
			if (module.isRunning()) {
				logger.info("Arresto modulo "+moduleName);
				try {
					module.stop();								
					logger.debug("Arrestato modulo "+moduleName);
				} catch (Exception e) {
					logger.fatal("Stop exception:",e);					
				}
			}
		}		
		logger.info("Arresto completato.");
	}
	
	public void restart() {
		stop();
		start();
	}

	/**
	 * Cerca esattamente un device
	 * @param address Indirizzo del device
	 * @return il Device trovato o null
	 */
	public Device getDevice(Address address) throws AISException {
		Vector<Device> devices = getDevices(address);
		if (devices.size() == 1) {
			return devices.firstElement();
		} else {
			return null;
		}
	}
	
	/**
	 * Ritorna il riferimento ad una porta in base all'indirizzo 
	 * @param fullAddress
	 * @return null se la porta non esiste
	 */
	public DevicePort getDevicePort(Address address) {
		try {
			Vector<Device> devices = getDevices(address);
			if (devices.size() == 1) {
				Device device = devices.firstElement();
				String portId = address.getPortId();
				return device.getPort(portId);
			} else {
				logger.warn("Port not found: "+address);
				return null;
			}
		} catch (Exception e) {
			logger.error("Getting port '"+address+"': ",e);
			return null;
		}
	}

}

