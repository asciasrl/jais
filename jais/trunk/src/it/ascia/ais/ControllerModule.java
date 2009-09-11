package it.ascia.ais;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Logger;

/**
 * I moduli del controller sono elementi di codice caricati dinamicamente
 * in base alle impostazioni presenti nel file di configurazione del sistema
 * 
 * @author sergio
 *
 */
public abstract class ControllerModule {

	private String name;
	
    protected Logger logger;
    
    protected XMLConfiguration configuration;

	protected boolean running;

    /**
     * Riferimento al controller che ha instanziato il modulo
     */
    protected Controller controller;

	public ControllerModule() {
		logger = Logger.getLogger(getClass());
	}

	public void start() {
		running = true;
	};
	
	public void stop() {
		running = false;		
	};

	public void setController(Controller controller) {
		this.controller = controller;		
	}

	public HierarchicalConfiguration getConfiguration() {
		return configuration.configurationAt(name);
	}

	public void setConfiguration(XMLConfiguration config) {
		this.configuration = config;
	}
	
	public void addNode(String key, Collection node) {
		ArrayList nodes = new ArrayList();
		nodes.add(node);
		configuration.addNodes(key, nodes);
	}
	
	public boolean saveConfiguration() {
		try {
			configuration.save();
		} catch (ConfigurationException e) {
			logger.error("Error saving configuration file "+configuration.getFileName()+":",e);
			return false;
		}
		return true;
	}	

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	public void fireDevicePortChangeEvent(DevicePortChangeEvent evt) {
		controller.fireDevicePortChangeEvent( evt );
	}

}
