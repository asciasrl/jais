/**
 * Copyright (C) 2008 ASCIA S.r.l.
 */
package it.ascia.ais;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.log4j.BasicConfigurator;
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
	/**
	 * Connector registrati.
	 * 
	 * <p>I Connector qui dentro sono accessibili dal loro nome (stringa).</p>
	 */
	private Map connectors = new LinkedHashMap();

	/**
	 * Moduli del controllore
	 */
	private Map modules = new LinkedHashMap();
	
	private Logger logger;
		
	private XMLConfiguration config;

	private static Controller controller;

	protected final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

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
	 * Carica il modulo, lo instanzia, ne imposta la configurazione. 
	 * @param name Nome (unico) del modulo
	 * @param className Classe che implementa il modulo
	 * @param configName 
	 * @throws AISException 
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
			module.setController(this);
		    if (configName == null) {		    	
				module.setConfiguration(config);
		    } else {
				XMLConfiguration moduleConfig = new XMLConfiguration(configName);
				moduleConfig.setReloadingStrategy(new FileChangedReloadingStrategy());
				module.setConfiguration(moduleConfig);
				logger.info("Caricata configurazione modulo '"+name+"' da "+configName);		
		    }
			modules.put(name,module);
			logger.info("Caricato modulo '"+name+"'");
		} catch (ClassNotFoundException e) {
			logger.error("Fallito caricamento modulo '"+name+"': non trovata classe '"+className+"'");
		} catch (InstantiationException e) {
			logger.error("Fallito caricamento modulo '"+name+"': errore instanzazione classe '"+className+"'");
		} catch (IllegalAccessException e) {
			logger.error("Fallito caricamento modulo '"+name+"': accesso negato alla classe '"+className+"'");
		} catch (ConfigurationException e) {
			logger.fatal("Fallito caricamento modulo '"+name+"': Errore nel file di configurazione:",e);
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
	
	/**
	 * Inizializza il logger con {@link BasicConfigurator}
	 */
	protected Controller() {
    	BasicConfigurator.configure();  // configurazione minimale di log4j
		logger = Logger.getLogger(getClass());	
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
		logger.info("Caricata configurazione versione "+configurationVersion+" da "+configurationFileName);		
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
	
	public void addPropertyChangeListener( PropertyChangeListener listener )
    {
        this.pcs.addPropertyChangeListener( listener );
    }

    public void removePropertyChangeListener( PropertyChangeListener listener )
    {
        this.pcs.removePropertyChangeListener( listener );
    }
	
	/**
	 * Comunica l'evento a tutti i Listener registrati con addPropertyChangeListener(PropertyChangeListener)
	 * 
	 */
	public void fireDevicePortChangeEvent(DevicePortChangeEvent evt) {
		this.pcs.firePropertyChange(evt);
	}

	/**
	 * Avvia il controller
	 * @param args Come unico parametro accetta il nome del file di configurazione 
	 * @throws AISException 
	 */
	public static void main(String[] args) throws AISException {		
		Controller c = Controller.getController();
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
	public void start() {
		Iterator i = modules.keySet().iterator();
		logger.info("Avvio di "+modules.size()+" moduli");
		long start = System.currentTimeMillis();
		while (i.hasNext()) {
			long start1 = System.currentTimeMillis();
			String moduleName = (String) i.next();
			ControllerModule module = getModule(moduleName);
			logger.info("Avvio modulo "+moduleName);
			module.start();		
			logger.info("Avviato modulo "+moduleName+" in "+(System.currentTimeMillis()-start1)/1000.0+" secondi.");
		}
		logger.info("Avviati "+modules.size()+" moduli in "+(System.currentTimeMillis()-start)/1000.0+" secondi.");
	}

	/**
	 * Ferma tutti i moduli
	 */
	public void stop() {
		Iterator i = modules.keySet().iterator();
		while (i.hasNext()) {
			String moduleName = (String) i.next();
			ControllerModule module = getModule(moduleName);
			logger.trace("Doing "+moduleName+".stop()");
			module.stop();			
			logger.trace("Done "+moduleName+".stop()");
		}		
	}
	
	public void restart() {
		stop();
		try {
			logger.info("Waiting to restart");
			Thread.sleep(3000); // FIXME viene interrotto subito
		} catch (InterruptedException e) {
			logger.error("Controller:",e);
		}
		start();
	}

	/**
	 * Cerca esattamente un device
	 * @see findDevices
	 * @param fullAddress Indirizzo completo del device
	 * @return il Device trovato o null
	 * @throws AISException
	 */
	public Device getDevice(String fullAddress) throws AISException {
		Device[] devices = findDevices(fullAddress);
		if (devices.length == 1) {
			return devices[0];
		} else {
			return null;
		}
	}

	public DevicePort getDevicePort(String fullAddress) {
		String address = getDeviceFromAddress(fullAddress);
		Device device = getDevice(address);
		if (device == null) {
			return null;
		}
		String portId = getPortFromAddress(fullAddress);
		try {
			return device.getPort(portId);
		} catch (Exception e) {
			logger.warn(e);
			return null;
		}
	}

}

