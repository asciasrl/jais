/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.device;

import it.ascia.ais.AISException;
import it.ascia.ais.Connector;
import it.ascia.eds.msg.EDSMessage;
import it.ascia.eds.msg.RispostaStatoMessage;

/**
 * Una centralina scenari.
 * 
 * Modelli: 152, 154, 156, 158
 * 
 * @author arrigo
 */
public class BMCScenarioManager extends BMC {

	/**
	 * Numero di porte in ingresso
	 */
	private int inPortsNum;

	/**
	 * Numero di porte in ingresso
	 */
	private int outPortsNum;

	/**
	 * Ingressi.
	 */
	private boolean inPorts[];
	/**
	 * Timestamp di aggiornamento degli ingressi.
	 */
	private long inPortsTimestamps[];
	/**
	 * Uscite.
	 */
	private boolean outPorts[];
	/**
	 * Timestamp di aggiornamento delle uscite.
	 */
	private long outPortsTimestamps[]; 
	
	/**
	 * Costruttore
	 * @param connector 
	 * @param address indirizzo del BMC
	 * @param model numero del modello
	 * @throws AISException 
	 */
	public BMCScenarioManager(Connector connector, String address, int model, String name) throws AISException {
		super(connector, address, model, name);
		switch(model) {
		case 152:
			inPortsNum = 2;
			break;
		case 154:
			inPortsNum = 4;
			break;
		case 156:
			inPortsNum = 6;
			break;
		case 158:
			inPortsNum = 8;
			break;
		default: // This should not happen(TM)
			logger.error("Errore: modello di centralina scenari " + 
					"sconosciuto: " + model);
			inPortsNum = 0;
		}
		if (inPortsNum > 0) {
			inPorts = new boolean[inPortsNum];
			inPortsTimestamps = new long[inPortsNum];
			for (int i = 0; i < inPortsNum; i++) {
				inPortsTimestamps[i] = 0;
			}
		}
		outPortsNum = 0;
		outPorts = new boolean[outPortsNum];
		outPortsTimestamps = new long[outPortsNum];
		for (int i = 0; i < outPortsNum; i++) {
			outPortsTimestamps[i] = 0;
		}
	}
	
	public void messageReceived(EDSMessage m) {
		// TODO
	}
	
	/**
	 * Avvisa il DeviceListener che una porta e' cambiata.
	 * 
	 * <p>
	 * Questa funzione deve essere chiamata dopo che il	valore della porta viene
	 * cambiato.
	 * </p>
	 * 
	 * @param port numero della porta
	 * @param isOutput true se si tratta di un'uscita, false se e' un ingresso.
	 */
	private void generateEvent(int port, boolean isOutput) {
		String newValue, portName;
		boolean val;
		if (isOutput) {
			val = outPorts[port];
			portName = getOutputPortId(port);
		} else {
			val = inPorts[port];
			portName = getInputPortId(port);
		}
		if (val) {
			newValue = "on";
		} else {
			newValue = "off";
		}
		super.generateEvent(portName, newValue);
	}
	
	public void messageSent(EDSMessage m) {
		switch (m.getMessageType()) {
		case EDSMessage.MSG_RISPOSTA_STATO: {
			RispostaStatoMessage r;
			r = (RispostaStatoMessage)m;
			// Il RispostaStatoMessage da' sempre 8 valori. Dobbiamo
			// prendere solo quelli effettivamente presenti sul BMC
			boolean temp[], oldValue;
			int i;
			long currentTime = System.currentTimeMillis();
			temp = r.getOutputs();
			for (i = 0; i < outPortsNum; i++) {
				oldValue = outPorts[i];
				outPorts[i] = temp[i];
				if (oldValue != outPorts[i]) {
					outPortsTimestamps[i] = currentTime;
					generateEvent(i, true);
				}
			}
			temp = r.getInputs();
			for (i = 0; i < inPortsNum; i++) {
				oldValue = inPorts[i];
				inPorts[i] = temp[i];
				if (oldValue != inPorts[i]) {
					inPortsTimestamps[i] = currentTime;
					generateEvent(i, false);
				}
			}
		}
		break;
		}
	}
	
	public String getInfo() {
		return getName() + ": BMC centralina scenari (modello " + model + ")" +
			" con " + inPortsNum + " ingressi digitali";
	}

	public String getStatus(String port, long timestamp) {
		String retval = "";
		int i;
		String fullAddress = getFullAddress();
		//updateStatus();
		for (i = 0; i < inPortsNum; i++) {
			if ((timestamp <= inPortsTimestamps[i]) &&
					(port.equals("*") || port.equals(getInputPortId(i)))) {
				retval += fullAddress + ":" + getInputPortId(i) + "=" + 
					(inPorts[i]? "ON" : "OFF") + "\n";
			}
		}
	 	for (i = 0; i < outPortsNum; i++) {
	 		if ((timestamp <= outPortsTimestamps[i]) &&
	 				(port.equals("*") || port.equals(getOutputPortId(i)))){
	 			retval += fullAddress + ":" + getOutputPortId(i) + "=" + 
	 				(outPorts[i]? "ON" : "OFF") + "\n";
	 		}
		}
	 	return retval;
	}
	
	public int getFirstInputPortNumber() {
		return 1;
	}

	// TODO: quante uscite ???
	public int getOutPortsNumber() {
		return 0;
	}
	/**
	 * Gli scenari non sono attivabili con comandi broadcast 
	 */
	public int getCaselleNumber() {
		return 0;
	}
	
	public int getInPortsNumber() {
		return inPortsNum;
	}

}
