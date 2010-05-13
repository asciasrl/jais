package it.ascia.avs;


import org.apache.log4j.Logger;

public abstract class CentraleAVS {
	
	protected AVSConnector connector;
	
    protected Logger logger;

	public CentraleAVS(AVSConnector connector) {
		logger = Logger.getLogger(getClass());
		this.connector = connector;
	}

	/**
	 * Gestisce le informazioni ricevute dalla centrale
	 * @param m Messaggio ricevuto dalla centrale
	 */
	abstract void processMessage(AVSMessage m);

}
