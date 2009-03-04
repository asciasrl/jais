package it.ascia.ais;

import java.io.Writer;
import java.util.HashMap;

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

    protected Logger logger;
    
    protected HierarchicalConfiguration config;
    
    /**
     * Riferimento al controller che ha instanziato il modulo
     */
    protected Controller controller;

	public ControllerModule() {
		logger = Logger.getLogger(getClass());
	}
	
	/**
	 * Gestisce un evento generato da un device di campo.
	 * 
	 * E' il controller che notifica l'evento a tutti i moduli.
	 * 
	 * @param event
	 */
	public abstract void onDeviceEvent(DeviceEvent event);
	
	/**
	 * TODO Obbligatorio ?
	 */
	//public abstract String doCommand(String command, HashMap params);
	
	/**
	 * TODO aggiungere altri eventi tipo:
	 * - nuovo device
	 * - 
	 */
	
	public abstract void start();
	
	public abstract void stop();

	public void setController(Controller controller) {
		this.controller = controller;		
	}

	public void setConfiguration(HierarchicalConfiguration config) {
		this.config = config;
	}
		
}
