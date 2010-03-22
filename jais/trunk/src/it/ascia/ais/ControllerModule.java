package it.ascia.ais;

import java.util.Vector;

import org.apache.commons.configuration.AbstractHierarchicalFileConfiguration;
import org.apache.commons.configuration.HierarchicalConfiguration;
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
    
    private HierarchicalConfiguration configuration;

	private Vector<Connector> myConnectors = new Vector<Connector>();
	
	private String configurationFilename;

	private boolean running;

    /**
     * Riferimento al controller che ha instanziato il modulo
     */
    protected Controller controller;

	public ControllerModule() {
		logger = Logger.getLogger(getClass());
		controller = Controller.getController();
	}
	
	/**
	 * Start module and set running
	 * In subclasses super.start() must be last instruction
	 */
	public void start() {
		running = true;
	};
	
	/**
	 * Add a connector to this module
	 * Also call Connector.setModule(this)
	 * @param connector
	 */
	protected void addConnector(Connector connector) {
		connector.setModule(this);
		myConnectors.add(connector);
	}
	
	protected Vector<Connector> getConnectors() {
		return myConnectors;
	}
	
	/**
	 * Stop module and reset running
	 * Close all connectors
	 * In subclasses super.stop() must be the first instruction
	 */
	public void stop() {
		for (Connector connector : myConnectors) {
			logger.debug("Closing connector: "+connector.getName());
			connector.close();
			logger.trace("Closed connector: "+connector.getName());
		}
		running = false;		
	};

	/**
	 * @return module sub configuration
	 */
	public HierarchicalConfiguration getConfiguration() {
		return configuration.configurationAt(name);
	}

	/**
	 * Set module configuration
	 * @param config
	 */
	public void setConfiguration(HierarchicalConfiguration config) {
		this.configuration = config;
	}
	
	public void setConfiguration(AbstractHierarchicalFileConfiguration config) {
		this.configurationFilename = config.getFileName();
		setConfiguration((HierarchicalConfiguration)config);
	}
	
	public String getConfigurationFilename() {
		return configurationFilename;
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

	/**
	 * @return true if module has been started without problems
	 */
	public boolean isRunning() {
		return running;
	}

}
