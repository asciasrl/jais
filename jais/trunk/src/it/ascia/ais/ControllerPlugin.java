package it.ascia.ais;

import org.apache.log4j.Logger;

/**
 * I plugin del controller sono elementi di codice caricati dinamicamente
 * in base alle impostazioni presenti nel file di configurazione del sistema
 * 
 * @author sergio
 *
 */
public abstract class ControllerPlugin {

    protected Logger logger;
    
    /**
     * Riferimento al controller che ha instanziato il plugin
     */
    protected Controller controller;

	public ControllerPlugin() {
		logger = Logger.getLogger(getClass());
	}
	
	/**
	 * Gestisce un evento generato da un device di campo.
	 * 
	 * E' il controller che notifica l'evento a tutti i plugin.
	 * 
	 * @param event
	 */
	public abstract void onDeviceEvent(DeviceEvent event);
	
	/**
	 * TODO aggiungere altri eventi tipo:
	 * - nuovo device
	 * - 
	 */
	
	/**
	 * Registra il riferimento al controller che ha instanziato il plugin
	 */
	public void setController(Controller controller) {
		this.controller = controller;		
	}

	/**
	 * (ri)configura il plugin
	 */
	public void configure() {
		// TODO Auto-generated method stub
		
	}
	
}
