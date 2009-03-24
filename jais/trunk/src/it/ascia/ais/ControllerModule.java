package it.ascia.ais;

import java.util.HashMap;

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

    protected Logger logger;
    
    protected HierarchicalConfiguration config;

    /**
     * Riferimento al controller che ha instanziato il modulo
     */
    protected Controller controller;

	public ControllerModule() {
		logger = Logger.getLogger(getClass());
	}

	public abstract void start();
	
	public abstract void stop();

	public void setController(Controller controller) {
		this.controller = controller;		
	}

	public void setConfiguration(HierarchicalConfiguration config) {
		this.config = config;
	}
		
}
