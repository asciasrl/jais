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
	 * Ingressi
	 */
	private boolean inPorts[];
	/**
	 * Timestamp di aggiornamento degli ingressi.
	 */
	private long inPortsTimestamps[];
	/**
	 * Ingresso IR
	 */
	private int irInput;
	/**
	 * Timestamp di aggiornamento dell'ingresso IR.
	 */
	private long irInputTimestamp = 0;
	
	/**
	 * Avvisa il DeviceListener che un ingresso e' cambiato.
	 * 
	 * <p>
	 * Questa funzione deve essere chiamata dopo che il	valore della porta viene
	 * cambiato.
	 * </p>
	 * 
	 * @param port numero della porta
	 */
	private void alertListener(int port) {
		String portName, newValue;
		portName = getInputPortId(port);
		if (inPorts[port]) {
			newValue = "on";
		} else {
			newValue = "off";
		}
		generateEvent(portName, newValue);
	}
	/**
	 * Costruttore
	 * @param address indirizzo del BMC
	 * @param model numero del modello
	 */
	public BMCIR(int address, int model, EDSConnector bus, String name) {
		super(address, model, bus, name);
		switch(model) {
		case 41:
		case 61:
		case 81:
			break;
		default: // This should not happen(TM)
			logger.error("Errore: modello di BMCIR sconosciuto:" + model);
		}
		inPorts = new boolean[inPortsNum];
		inPortsTimestamps = new long[inPortsNum];
		for (int i = 0; i < inPortsNum; i++) {
			inPortsTimestamps[i] = 0;
		}
	}
	
	/* (non-Javadoc)
	 * @see it.ascia.eds.device.BMC#receiveMessage(it.ascia.eds.msg.Message)
	 */
	public void messageReceived(EDSMessage m) {
		// TODO
	}
	
	public void messageSent(EDSMessage m) {
		switch (m.getMessageType()) {
		case EDSMessage.MSG_RISPOSTA_STATO: {
			RispostaStatoMessage r;
			r = (RispostaStatoMessage)m;
			// Di questo messaggio ci interessano solo gli ingressi.
			boolean temp[];
			int i;
			long currentTime = System.currentTimeMillis();
			temp = r.getInputs();
			for (i = 0; i < inPortsNum; i++) {
				boolean oldValue = inPorts[i];
				inPorts[i] = temp[i];
				if (oldValue != inPorts[i]) {
					inPortsTimestamps[i] = currentTime;
					alertListener(i);
				}
			}
		}
		break;
		}
	}
	
	public String getInfo() {
		return getName() + ": BMC IR (modello " + model + ") con " + 
			inPortsNum + " porte di input";
	}

	// Attenzione: chiama sempre updateStatus() !
	public String getStatus(String port, long timestamp) {
		String retval = "";
		String busName = connector.getName();
		int i;
		String compactName = busName + "." + getAddress();
		updateStatus();
		for (i = 0; i < inPortsNum; i++) {
			if ((timestamp <= inPortsTimestamps[i]) &&
					(port.equals("*") || port.equals(getInputPortId(i)))) {
				retval += compactName + ":" + getInputPortId(i) + "=" + 
					(inPorts[i]? "ON" : "OFF") + "\n";
			}
		}
	 	return retval;
	
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

	public void setPort(String port, String value) throws EDSException {
		throw new EDSException("Not implemented.");
	}
	
	public int getInPortsNumber() {
		return inPortsNum;
	}
	public String peek(String portId) throws AISException {
		// TODO Auto-generated method stub
		return null;
	}
	public void poke(String portId, String value) throws AISException {
		// TODO Auto-generated method stub
		
	}
}
