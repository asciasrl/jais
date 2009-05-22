/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.device;

import it.ascia.ais.AISException;
import it.ascia.ais.Connector;
import it.ascia.eds.msg.EDSMessage;
import it.ascia.eds.msg.RispostaStatoMessage;

/**
 * Un BMC con una porta a infrarossi.
 * 
 * Modelli: 131
 * 
 * @author arrigo
 */
public class BMCIntIR extends BMC {
	/**
	 * Ingresso IR.
	 */
	private boolean irInput;
	/**
	 * Timestamp dell'ultimo aggiornamento dell'ingresso.
	 */
	long irInputTimestamp = 0;
	
	/**
	 * Costruttore.
	 * @param connector 
	 * 
	 * @param address indirizzo del BMC
	 * @param model numero del modello
	 * @throws AISException 
	 */
	public BMCIntIR(Connector connector, String address, int model, String name) throws AISException {
		super(connector, address, model, name);
		if (model != 131) {
			logger.error("Errore: modello di BMC Int IR sconosciuto:" + 
					model);
		}
	}
	
	/* (non-Javadoc)
	 * @see it.ascia.eds.device.BMC#messageReceived(it.ascia.eds.msg.Message)
	 */
	public void messageReceived(EDSMessage m) {
		// TODO
	}
	
	/**
	 * Proxy per generateEvent.
	 * 
	 * <p>Informa il listener che l'input a IR ha cambiato valore.</p>
	 * @throws AISException 
	 */
	private void generateEvent() throws AISException {
		String value;
		if (irInput) {
			value = "ON";
		} else {
			value = "OFF";
		}
		setPortValue(getInputPortId(0), value);
	}
	
	public void messageSent(EDSMessage m) throws AISException {
		switch (m.getMessageType()) {
		case EDSMessage.MSG_RISPOSTA_STATO: {
			RispostaStatoMessage r;
			r = (RispostaStatoMessage)m;
			boolean oldInput = irInput;
			// Il RispostaStatoMessage da' sempre 8 valori. Dobbiamo
			// prendere solo quelli effettivamente presenti sul BMC
			boolean temp[];
			temp = r.getInputs();
			irInput = temp[0];
			if (oldInput != irInput) {
				irInputTimestamp = System.currentTimeMillis();
				generateEvent();
			}
		}
		break;
		}
	}
	
	public String getInfo() {
		return getName() + ": BMC Int IR (modello " + model + ")";
	}

	public String getStatus(String port, long timestamp) { // TODO
		String fullAddress = getFullAddress();
		if ((timestamp <= irInputTimestamp) &&
				(port.equals("*") || port.equals(getInputPortId(0)))) {
			return fullAddress + ":" + getInputPortId(0) +
			"=" + (irInput? "ON" : "OFF") + "\n";
		} else {
			return "";
		}
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

	public int getInPortsNumber() {
		return 0;
	}

	public boolean sendPortValue(String portId, Object newValue)
			throws AISException {
		// TODO Non implementato
		logger.error("sendPortValue non implementato");
		return false;
	}


}
