/**
 * Copyright (C) 2008 ASCIA S.r.l.
 */
package it.ascia.ais;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
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
	
	public static final String CONFIGURATION_VERSION = "1.5";

	private static final int DEFAULT_WATCHDOG_INTERVAL = 100;

	/**
	 * Connector registrati.
	 * 
	 * <p>I Connector qui dentro sono accessibili dal loro nome (stringa).</p>
	 */
	private HashMap<String, ConnectorInterface> connectors = new LinkedHashMap<String,ConnectorInterface>();

	/**
	 * Moduli del controllore
	 */
	private Map<String, ControllerModule> modules = new LinkedHashMap<String, ControllerModule>();

	private Set<NewDevicePortListener> ndpl = new HashSet<NewDevicePortListener>();
	
	private Logger logger;
		
	private XMLConfiguration config;
	
	private boolean running = false;

	/**
	 * Tempo intervallo watchdog in secondi
	 */
	private int watchdogInterval = DEFAULT_WATCHDOG_INTERVAL;

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
	public void addConnector(ConnectorInterface connector) throws KeyAlreadyExistsException {
		if (connectors.containsKey(connector.getName())) {
			throw(new KeyAlreadyExistsException("Connector name duplicated: "+connector.getName()));
		}
		connectors.put(connector.getName(), connector);
		connector.start();
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
	 * 
	 * @param name Name of connector
	 * @return Connector
	 */
	public ConnectorInterface getConnector(String name) {
		return connectors.get(name);
	}
	
	public Collection<ConnectorInterface> getConnectors() {
		return connectors.values();
	}

	/**
	 * 
	 * @param address 
	 * @return Devices that match the address
	 */
	public Vector<Device> getDevices(Address address) {
		Vector<Device> res = new Vector<Device>();
		for (ConnectorInterface connector : connectors.values()) {
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
		List<HierarchicalConfiguration> modulesConfig = config.configurationsAt("modules.module");
		for(Iterator<HierarchicalConfiguration> it = modulesConfig.iterator(); it.hasNext();)
		{
		    HierarchicalConfiguration sub = (HierarchicalConfiguration) it.next();
		    String name = (String) sub.getProperty("[@name]");
		    Boolean disabled = sub.getBoolean("[@disabled]",false);
		    if (disabled) {
				logger.info("Modulo '"+name+"' disabilitato, non viene caricato.");
				continue;
		    }
		    String className = sub.getString("class");
		    String configName = sub.getString("config",null);
		    try {
				if (modules.containsKey(name)) {
					throw(new AISException("Nome modulo duplicato: '"+name+"'"));
				}
				ClassLoader moduleLoader = ControllerModule.class.getClassLoader();
				ControllerModule module = null;
				logger.debug("Caricamento modulo '"+name+"' da '"+className+"'");
				Class<?> moduleClass = moduleLoader.loadClass(className);
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
			} catch (Exception e) {
				logger.fatal("Fallito caricamento modulo '"+name+"':",e);
			}
		}		
		
		watchdogInterval = config.getInt("watchdog[@interval]",DEFAULT_WATCHDOG_INTERVAL);
		
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
		if (running) {
			logger.warn("Controller already running.");
			return;
		}
		running = true;
		Iterator<String> i = modules.keySet().iterator();
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
					throw(new AISException("Unable to start module: " + moduleName));
				}
			}
			logger.debug("Avviati "+modules.size()+" moduli in "+(System.currentTimeMillis()-start)/1000.0+" secondi.");			
		} catch (Exception e) {
			logger.fatal("Start exception:",e);
			stop();
			throw(new AISException("Cannot start all modules"));
		}
		WatchDog watchdog = new WatchDog(watchdogInterval);
		watchdog.setName("WatchDog");
		watchdog.start();
	}

	/**
	 * Ferma tutti i moduli
	 */
	public void stop() {
		if (!running) {
			logger.warn("Controller already stopped.");
			return;
		}
		running = false;
		Iterator<String> i = modules.keySet().iterator();
		logger.info("Stopping "+modules.size()+" modules.");
		while (i.hasNext()) {
			String moduleName = (String) i.next();
			ControllerModule module = getModule(moduleName);
			if (module.isRunning()) {
				logger.info("Stopping module "+moduleName);
				try {
					module.stop();								
					logger.debug("Stopped module "+moduleName);
				} catch (Exception e) {
					logger.fatal("Stop exception:",e);					
				}
			} else {
				logger.trace("Module "+module.getName()+" already stopped.");
			}
		}		
		logger.trace("Closing "+connectors.size()+" connectors");
		for (ConnectorInterface connector: connectors.values()) {
			if (connector.isRunning()) {
				// I connettori dovrebbero essere stati tutti chiusi dal modulo relativo
				logger.error("Closing connettor "+connector.getName());
				try {
					connector.close();								
					logger.debug("Closed connettore "+connector.getName());
				} catch (Exception e) {
					logger.fatal("Stop exception:",e);					
				}
			} else {
				logger.trace("Connector "+connector.getName()+" already closed.");
			}
		}		
		logger.info("Arresto completato.");
	}
	
	public void restart() {
		stop();
		start();
	}
	
	private class WatchDog extends Thread {
					
		long interval = DEFAULT_WATCHDOG_INTERVAL;
		
		public WatchDog(long i) {
			super();
			interval = i;
		}

		@Override
		public void run() {
			super.run();
	   		while (true) {
				try {
					logger.debug("Sleeping "+interval+" seconds.");
					sleep(interval*1000); 
					int ok = 0;
					int tot = 0;
					/*
					for (ControllerModule module: modules.values()) {
						if (module.isRunning()) {
							tot++;
							if (module.isAlive()) {
								logger.trace("Module "+module.getName()+" is alive.");
								ok++;
							} else {
								logger.fatal("Module "+module.getName()+" is NOT alive!");
							}
						} else {
							logger.trace("Module "+module.getName()+" is not running.");
						}
					}
					*/		
					for (ConnectorInterface connector: connectors.values()) {
						if (connector.isRunning()) {
							tot++;
							if (connector.isAlive()) {
								logger.trace("Connector "+connector.getName()+" is alive.");
								ok++;
							} else {
								logger.fatal("Connector "+connector.getName()+" is NOT alive!");
							}
						} else {
							logger.trace("Connector "+connector.getName()+" is not running.");
						}
					}
					Runtime.getRuntime().gc();
					logger.info("Ok "+ok+"/"+tot+" Memory (free/tot): "+Runtime.getRuntime().freeMemory()/1024+"/"+Runtime.getRuntime().totalMemory()/1024+" KBytes");
					if (ok < tot) {
						logger.error("Not all running modules and connector are alive, exiting!");
						System.exit(1);
					}
					if (Runtime.getRuntime().freeMemory() * 100 < Runtime.getRuntime().totalMemory()) {
						logger.error("Low free memory, exiting!");
						System.exit(1);						
					}
				} catch (InterruptedException e) {
					logger.debug("Interrotto.");
				} catch (Exception e) {
					logger.error("Errore:",e);
				}
    		}			
		}		
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

	/**
	 * Fired when a port is added to a device, dispatch to method newDevicePort of all listner bounded with addNewDevicePortListener()   
	 * @param evt
	 */
	void fireNewDevicePortEvent(NewDevicePortEvent evt) {
		for (NewDevicePortListener el : ndpl) {
			el.newDevicePort(evt);
		}
	}

	
	/**
	 * Add to the list of object listening for new Device Port.
	 * When a new Device Port event fires, the method newDevicePort of the listener is invoked  
	 * @param listener
	 */
	public synchronized void addNewDevicePortListener(NewDevicePortListener listener) {
		ndpl.add(listener);
	}

	public synchronized void removeNewDevicePortListener(NewDevicePortListener listener) {
		ndpl.remove(listener);
	}

}

