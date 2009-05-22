package it.ascia.ais;

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

	public void setConfiguration(HierarchicalConfiguration config) {
		this.configuration = config;
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
		
}
