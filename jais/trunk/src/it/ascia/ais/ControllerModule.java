package it.ascia.ais;

import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

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

	protected List myConnectors = new Vector();
	
    /**
     * Riferimento al controller che ha instanziato il modulo
     */
    protected Controller controller;

	public ControllerModule() {
		logger = Logger.getLogger(getClass());
	}

	/**
	 * TODO aggiungere altri eventi tipo:
	 * - nuovo device
	 * - 
	 */
	
	public abstract void start();
	
	/**
	 * Chiude tutti i connettori
	 */
	public void stop() {	
		for (Iterator c = myConnectors.iterator(); c.hasNext();)
		{
			Connector connector = (Connector) c.next(); 
			logger.info("Chiusura connettore "+connector.getName());
			connector.close();
		}
			
	}


	public void setController(Controller controller) {
		this.controller = controller;		
	}

	public void setConfiguration(HierarchicalConfiguration config) {
		this.config = config;
	}
		
}
