/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.device;

import it.ascia.ais.AISException;
import it.ascia.ais.Connector;
import it.ascia.ais.DevicePort;
import it.ascia.eds.EDSConnector;
import it.ascia.eds.msg.ComandoBroadcastMessage;
import it.ascia.eds.msg.ComandoUscitaDimmerMessage;
import it.ascia.eds.msg.ComandoUscitaMessage;
import it.ascia.eds.msg.EDSMessage;
import it.ascia.eds.msg.PTPRequest;
import it.ascia.eds.msg.RichiestaStatoMessage;
import it.ascia.eds.msg.RispostaAssociazioneUscitaMessage;
import it.ascia.eds.msg.RispostaStatoDimmerMessage;
import it.ascia.eds.msg.VariazioneIngressoMessage;

/**
 * Un BMC dimmer con 1 o 2 porte di output.
 * 
 * Modelli: 101, 102, 103, 104, 106, 1110
 * 
 * Tutti metodi di set assumono che un canale a 0% sia spento.
 * 
 * Questo BMC risponde ai comandi broadcast, ai comandi di impostazione uscita
 * per dimmer e non.
 * 
 * @author arrigo
 */
public class BMCDimmer extends BMC {
	/**
	 * Numero di uscite digitali.
	 */
	protected int outPortsNum;
	/**
	 * Potenza in uscita [Watt] o -1 se l'uscita è 0-10 V
	 */
	private int power;
	/**
	 * Nome dato dal costruttore
	 */
	private String modelName;
	
	/**
	 * Costruttore
	 * @param address indirizzo del BMC
	 * @param model numero del modello
	 * @throws AISException 
	 */
	public BMCDimmer(Connector connector, String address, int model, String name) throws AISException {
		super(connector, address, model, name);
		switch(model) {
		case 101:
			outPortsNum = 2;
			power = 800;
			modelName = "Base low power";
			break;
		case 102:
			outPortsNum = 2;
			power = 800;
			modelName = "Evolution low power";
			break;
		case 103:
			outPortsNum = 2;
			power = 2000;
			modelName = "Base high power";
			break;
		case 104:
			outPortsNum = 2;
			power = 2000;
			modelName = "Evolution high power";
			break;
		case 106:
			outPortsNum = 1;
			power = -1;
			modelName = "sperimentale 0-10 V";
			break;
		case 111:
			outPortsNum = 1;
			power = -1;
			modelName = "0-10 V";
		break;
		default: // This should not happen(TM)
			logger.error("Errore: modello di BMCDimmer sconosciuto:" +
					model);
			power = outPortsNum = 0;
			modelName = "Dimmer sconosciuto";
		}
		for (int i = 0; i < outPortsNum; i++) {
			addPort(getOutputPortId(i));
		}
	}
	
	/* (non-Javadoc)
	 * @see it.ascia.eds.device.BMC#receiveMessage(it.ascia.eds.msg.Message)
	 */
	public void messageReceived(EDSMessage m) throws AISException {
		int uscita;
		ComandoBroadcastMessage bmsg;
		VariazioneIngressoMessage vmsg;
		int[] ports;
//		logger.debug("Ricevuto un messaggio di tipo " + m.getTipoMessaggio());
		switch (m.getMessageType()) {
		case EDSMessage.MSG_COMANDO_USCITA_DIMMER:
			// L'attuazione viene richiesta, non sappiamo se sara' 
			// effettuata
			ComandoUscitaDimmerMessage cmdDimmer = 
				(ComandoUscitaDimmerMessage) m;
			uscita = cmdDimmer.getOutputPortNumber();
			invalidate(getOutputPortId(uscita));
			break;
		case EDSMessage.MSG_COMANDO_USCITA:
			// L'attuazione viene richiesta, non sappiamo se sara' 
			// effettuata.
			ComandoUscitaMessage cmd = (ComandoUscitaMessage) m;
			uscita = cmd.getOutputPortNumber();
			invalidate(getOutputPortId(uscita));
			break;
		case EDSMessage.MSG_COMANDO_BROADCAST:
			// Messaggio broadcast: potrebbe interessare alcune porte.
			bmsg = (ComandoBroadcastMessage) m;
			ports = getBoundOutputs(bmsg.getCommandNumber());
			if (ports.length > 0) {
				logger.trace("Ricevuto un comando broadcast che ci interessa");
				for (int i = 0; i < ports.length; i++) {
					invalidate(getOutputPortId(i));
				}
			}
			break;
		case EDSMessage.MSG_VARIAZIONE_INGRESSO:
			// Qualcuno ha premuto un interruttore, e la cosa ci interessa.
			vmsg = (VariazioneIngressoMessage) m;
			invalidate(getOutputPortId(vmsg.getOutputNumber()));
			break;
		}
	}
	
	public void messageSent(EDSMessage m) throws AISException {
		RispostaStatoDimmerMessage r;
		RispostaAssociazioneUscitaMessage ra;
		switch(m.getMessageType()) {
		case EDSMessage.MSG_RISPOSTA_STATO_DIMMER:
			r = (RispostaStatoDimmerMessage)m;
			// Il RispostaStatoMessage da' sempre 8 valori. Dobbiamo
			// prendere solo quelli effettivamente presenti sul BMC
			int temp[];
			int i;
			temp = r.getOutputs();
			for (i = 0; i < outPortsNum; i++) {
				String portId = getOutputPortId(i);
				DevicePort p = getPort(portId);
				Integer newValue = new Integer(temp[i]);
				Integer oldValue = (Integer) p.getCachedValue();
				// TODO gestire meglio lo stato "in variazione" del dimmer
				if (p.getCachedValue() != null && (p.isDirty() || ! newValue.equals(oldValue))) {
					p.setCacheRetention(100);
				} else {
					long ret = p.getCacheRetention() * 3;
					p.setCacheRetention(Math.min(ret,DevicePort.DEFAULT_CACHE_RETENTION));
				}
				p.setValue(newValue);
			}
			break;
		case EDSMessage.MSG_RISPOSTA_ASSOCIAZIONE_BROADCAST: 
			ra = (RispostaAssociazioneUscitaMessage) m;
			// Stiamo facendo un discovery delle associazioni.
			if (ra.getComandoBroadcast() != 0) {
				logger.debug("L'uscita " + ra.getUscita() + " " +
					"e' legata al comando broadcast " + 
					ra.getComandoBroadcast());
				bindOutput(ra.getComandoBroadcast(), ra.getUscita());					
			}
			break;
		} // switch(tipo del messaggio)
	}
	
	public String getInfo() {
		String retval;
		retval = getName() + ": Dimmer (modello " + model + ", \"" + modelName +
			"\") con " + outPortsNum + " uscite"; 
		if (power >= 0) retval += " a " + power + " Watt";
		return retval;
	}
	
	public void printStatus() throws AISException {
		int i;
		System.out.print("Uscite:");
		for (i = 0; i < outPortsNum; i++) {
			System.out.print(" " + getPortValue(getOutputPortId(i)));
		}
		System.out.println();
		// Stampiamo anche i binding, ordinati per segnale
	 	for (i = 1; i < 32; i++) {
	 		int outputs[] = getBoundOutputs(i);
	 		if (outputs.length != 0) {
	 			System.out.print("Il messaggio broadcast " + i +
	 					" e' legato alle porte: ");
	 			for (int j = 0; j < outputs.length; j++) {
	 				System.out.print(outputs[j] + " ");
	 			}
	 			System.out.println();
	 		} // Se ci sono messaggi legati
	 	}
	}
		
	public long updateStatus() {
		PTPRequest m;
		EDSConnector connector = (EDSConnector) getConnector();
		// Il protocollo permette di scegliere più uscite. Qui chiediamo solo le
		// prime due.
		m = new RichiestaStatoMessage(getIntAddress(),getBMCComputerAddress(), 3);
		connector.sendMessage(m);
		return connector.getRetryTimeout() * m.getMaxSendTries(); 
	}

	public String getStatus(String port, long timestamp) throws AISException {
		int i;
		String retval = "";
		String fullAddress = getFullAddress();
		for (i = 0; i < outPortsNum; i++) {
			String portId = getOutputPortId(i);
			if ((timestamp <= getPortTimestamp(portId)) &&
					(port.equals("*") || port.equals(portId))){
				retval += fullAddress + ":" + portId + "=" +
					getPortValue(portId) + "\n";
			}
		}
		return retval;
	}
	
	/**
	 * Imposta istantaneamente il valore di un canale.
	 * 
	 * Rispetto a setOutput(), questo metodo invia un messaggio che non richiede
	 * risposta, quindi e' piu' rapido.
	 * 
	 * @param output il numero dell'uscita (di solito 0 o 1)
	 * @param value il valore da impostare (da 0 a 100, dove 0 e' OFF)
	 */
	public void setOutputRealTime(int output, int value) {
		if ((output >= 0) && (output <= outPortsNum)) {
			if ((value >= 0) && (value <= 100)) {
				ComandoUscitaDimmerMessage m;
				m = new ComandoUscitaDimmerMessage(getIntAddress(), 
						getBMCComputerAddress(), output, value);
				getConnector().sendMessage(m);
			} else {
				logger.error("Valore non valido per canale dimmer: " +
						value);
			}
		} else {
			logger.error("Porta dimmer non valida: " + output);
		}
	}
	
	/**
	 * Questo BMC non ha ingressi.
	 */
	public int getFirstInputPortNumber() {
		return 0;
	}
	
	/**
	 * Tutti i BMC hanno 4 caselle per uscita, tranne i Dimmer che ne hanno 8.
	 */
	public int getCaselleNumber() {
		return 8;
	}

	public int getOutPortsNumber() {
		return outPortsNum;
	}
	
	/**
	 * Imposta la porta di un dimmer.
	 * 
	 * @param port il nome compatto della porta.
	 * @value un numero, un valore percentuale o "OFF"
	 */
	public boolean writePort(String port, Object o) throws AISException {
		String value = o.toString();
		int outPort, numericValue;
		outPort = getOutputNumberFromPortId(port);
		if (outPort == -1) {
			throw new AISException("Porta non valida: " + port);
		}
		// Trattiamo percentuali e numeri
		if (value.endsWith("%")) {
			value = value.substring(0, value.length() - 1);
		}
		if (value.toUpperCase().equals("OFF")) {
			numericValue = 0;
		} else {
			try {
				numericValue = Integer.parseInt(value);
			} catch (NumberFormatException e) {
				throw new AISException("Valore non valido: " + value);
			}
		}
		ComandoUscitaMessage m = new ComandoUscitaMessage(getIntAddress(), getBMCComputerAddress(), 0, outPort, numericValue, 
				(numericValue > 0)? 1 : 0);
		return getConnector().sendMessage(m);		
	}

	public int getInPortsNumber() {
		return 0;
	}

}
