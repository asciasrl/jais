/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.device;

import it.ascia.ais.AISException;
import it.ascia.eds.EDSConnector;
import it.ascia.eds.EDSException;
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
	 * 
	 * @param address indirizzo del BMC
	 * @param model numero del modello
	 */
	public BMCIntIR(int address, int model, String name) {
		super(address, model, name);
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
	 */
	private void alertListener() {
		String value;
		if (irInput) {
			value = "ON";
		} else {
			value = "OFF";
		}
		generateEvent(getInputPortId(0), value);
	}
	
	public void messageSent(EDSMessage m) {
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
				alertListener();
			}
		}
		break;
		}
	}
	
	public String getInfo() {
		return getName() + ": BMC Int IR (modello " + model + ")";
	}

	public String getStatus(String port, long timestamp) { // TODO
		String busName = connector.getName();
		String compactName = busName + "." + getAddress();
		if ((timestamp <= irInputTimestamp) &&
				(port.equals("*") || port.equals(getInputPortId(0)))) {
			return compactName + ":" + getInputPortId(0) +
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

	public void poke(String port, String value) throws EDSException {
		throw new EDSException("Not implemented.");
	}

	public int getInPortsNumber() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String peek(String portId) throws AISException {
		// TODO Auto-generated method stub
		return null;
	}

}
