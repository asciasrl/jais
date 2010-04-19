package it.ascia.avs;

import org.apache.log4j.Logger;

import it.ascia.ais.Connector;

public abstract class CentraleAVS {
	
	protected Connector connector;
	
    protected Logger logger;

	public CentraleAVS(Connector connector) {
		logger = Logger.getLogger(getClass());
		this.connector = connector;
	}

	/**
	 * Gestisce le informazioni ricevute dalla centrale
	 * @param m Messaggio ricevuto dalla centrale
	 */
	abstract void processMessage(EL88Message m);

}
