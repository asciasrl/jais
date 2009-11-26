package it.ascia.ais;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;

import org.apache.commons.configuration.AbstractHierarchicalFileConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
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
	 * Stop module and reset running
	 * In subclasses super.stop() must be the first instruction
	 */
	public void stop() {
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
