/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.device;

import it.ascia.ais.AISException;
import it.ascia.ais.Connector;
import it.ascia.eds.msg.EDSMessage;
import it.ascia.eds.msg.RispostaStatoMessage;

/**
 * Un BMC con porte di input + 1 a infrarossi.
 * 
 * Gli ingressi sono "mappati" su porte di ingresso. Quindi gli ingressi
 * logici sono sempre 8. Alcuni possono essere _anche_ fili.
 * 
 * Modelli: 41, 61, 81
 * 
 * @author arrigo
 */
public class BMCIR extends BMC {

	/**
	 * Numero di porte in ingresso
	 */
	private final int inPortsNum = 8;

	/**
	 * Costruttore
	 * @param address indirizzo del BMC
	 * @param model numero del modello
	 * @throws AISException 
	 */
	public BMCIR(Connector connector, String address, int model, String name) throws AISException {
		super(connector, address, model, name);
		switch(model) {
		case 41:
		case 61:
		case 81:
			break;
		default: // This should not happen(TM)
			logger.error("Errore: modello di BMCIR sconosciuto:" + model);
		}
	}
	
	/* (non-Javadoc)
	 * @see it.ascia.eds.device.BMC#receiveMessage(it.ascia.eds.msg.Message)
	 */
	public void messageReceived(EDSMessage m) {
		// TODO
	}
	
	public void messageSent(EDSMessage m) throws AISException {
		switch (m.getMessageType()) {
		case EDSMessage.MSG_RISPOSTA_STATO:
			RispostaStatoMessage r;
			r = (RispostaStatoMessage)m;
			// Di questo messaggio ci interessano solo gli ingressi.
			boolean temp[];
			temp = r.getInputs();
			for (int i = 0; i < inPortsNum; i++) {
				setPortValue(getInputPortId(i),new Boolean(temp[i]));
			}
		break;
		}
	}
	
	public String getInfo() {
		return getName() + ": BMC IR (modello " + model + ") con " + 
			inPortsNum + " porte di input";
	}
	
	/**
	 * Questo e' uno dei pochi BMC con gli ingressi che possono avere
	 * indirizzo 0.
	 */
	public int getFirstInputPortNumber() {
		return 0;
	}

	public int getOutPortsNumber() {
		return 0;
	}

	public void setPort(String port, String value) throws AISException {
		throw new AISException("Not implemented.");
	}
	
	public int getInPortsNumber() {
		return inPortsNum;
	}
	
	public boolean sendPortValue(String portId, Object newValue)
		throws AISException {
		// TODO Non implementato
		logger.error("sendPortValue non implementato");
		return false;
	}

}
