/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.device;

import it.ascia.ais.AISException;
import it.ascia.eds.EDSConnector;
import it.ascia.eds.EDSException;
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
	 * Uscite. Possono assumere un valore 0-100 oppure -1 (OFF).
	 */
	private int[] outPorts;
	/**
	 * La conoscenza delle uscite puo' essere errata?
	 * 
	 * <p>Questa array ha un elemento per ogni porta.</p>
	 */
	private boolean dirty[];
	/**
	 * Timestamp di aggiornamento per le porte di uscita.
	 * 
	 * <p>Questa array ha un elemento per ogni porta.</p>
	 */
	private long outPortsTimestamps[];
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
	 */
	public BMCDimmer(int address, int model, String name) {
		super(address, model, name);
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
		if (outPortsNum > 0) {
			outPorts = new int[outPortsNum];
			dirty = new boolean[outPortsNum];
			outPortsTimestamps = new long[outPortsNum];
		}
		for (int i = 0; i < outPortsNum; i++) {
			dirty[i] = true;
			outPortsTimestamps[i] = 0;
		}
	}
	
	/**
	 * Proxy per generateEvent.
	 * 
	 * @param portNumber numero della porta che ha cambiato valore.
	 */
	private void alertListener(int portNumber) {
		generateEvent(getOutputPortId(portNumber), 
				String.valueOf(outPorts[portNumber]));
	}
	
	/* (non-Javadoc)
	 * @see it.ascia.eds.device.BMC#receiveMessage(it.ascia.eds.msg.Message)
	 */
	public void messageReceived(EDSMessage m) {
		int uscita, valore;
		ComandoBroadcastMessage bmsg;
		VariazioneIngressoMessage vmsg;
		int[] ports;
//		logger.debug("Ricevuto un messaggio di tipo " + m.getTipoMessaggio());
		switch (m.getMessageType()) {
		case EDSMessage.MSG_COMANDO_USCITA_DIMMER: {
			// L'attuazione viene richiesta, non sappiamo se sara' 
			// effettuata
			ComandoUscitaDimmerMessage cmdDimmer = 
				(ComandoUscitaDimmerMessage) m;
			int oldValue;
			uscita = cmdDimmer.getOutputPortNumber();
			valore = cmdDimmer.getValue();
			oldValue = outPorts[uscita];
			outPorts[uscita] = valore;
			if (oldValue != valore) {
				outPortsTimestamps[uscita] = System.currentTimeMillis();
				alertListener(uscita);
			}
			dirty[uscita] = true;
		}
		break;
		case EDSMessage.MSG_COMANDO_USCITA: {
			// L'attuazione viene richiesta, non sappiamo se sara' 
			// effettuata.
			ComandoUscitaMessage cmd = (ComandoUscitaMessage) m;
			int oldValue;
			uscita = cmd.getOutputPortNumber();
			oldValue = outPorts[uscita];
			valore = cmd.getPercentage();
			if (!cmd.isActivation()) {
				valore = 0;
			}
			outPorts[uscita] = valore;
			if (oldValue != valore) {
				outPortsTimestamps[uscita] = System.currentTimeMillis();
				alertListener(uscita);
			}
			dirty[uscita] = true;
		}
		break;
		case EDSMessage.MSG_COMANDO_BROADCAST:
			// Messaggio broadcast: potrebbe interessare alcune porte.
			bmsg = (ComandoBroadcastMessage) m;
			ports = getBoundOutputs(bmsg.getCommandNumber());
			if (ports.length > 0) {
				logger.trace("Ricevuto un comando broadcast che ci interessa");
				for (int i = 0; i < ports.length; i++) {
					dirty[ports[i]] = true;
					// Non aggiorniamo i timestamp, perché tanto non sappiamo
					// i valori che le porte prenderanno.
				}
			}
			break;
		case EDSMessage.MSG_VARIAZIONE_INGRESSO:
			// Qualcuno ha premuto un interruttore, e la cosa ci interessa.
			vmsg = (VariazioneIngressoMessage) m;
			dirty[vmsg.getOutputNumber()] = true;
			// FIXME: come facciamo a generare un evento? Ci serve il nuovo
			// valore! Perciò non aggiorniamo neanche il timestamp.
			break;
		}
	}
	
	public void messageSent(EDSMessage m) {
		RispostaStatoDimmerMessage r;
		RispostaAssociazioneUscitaMessage ra;
		switch(m.getMessageType()) {
		case EDSMessage.MSG_RISPOSTA_STATO_DIMMER:
			r = (RispostaStatoDimmerMessage)m;
			// Il RispostaStatoMessage da' sempre 8 valori. Dobbiamo
			// prendere solo quelli effettivamente presenti sul BMC
			int temp[];
			int i;
			long currentTime = System.currentTimeMillis();
			temp = r.getOutputs();
			for (i = 0; i < outPortsNum; i++) {
				int oldValue = outPorts[i];
				outPorts[i] = temp[i];
				if (oldValue != outPorts[i]) {
					outPortsTimestamps[i] = currentTime;
					alertListener(i);
				}
				dirty[i] = false;
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
	
	public void printStatus() {
		int i;
		System.out.print("Uscite:");
		for (i = 0; i < outPortsNum; i++) {
			System.out.print(" " + outPorts[i]);
			if (dirty[i]) {
				System.out.print("?");
			}
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
		
	public void updateStatus() {
		PTPRequest m;
		// Il protocollo permette di scegliere più uscite. Qui chiediamo solo le
		// prime due.
		m = new RichiestaStatoMessage(getIntAddress(),getBMCComputerAddress(), 3);
		connector.sendMessage(m);
	}

	/**
	 * Ritorna true se almeno un dato e' contrassegnato come "dirty".
	 */
	public boolean hasDirtyCache() {
		boolean retval = false;
		for (int i = 0; (i < outPortsNum) && !retval; i++) {
			retval = retval || dirty[i];
		}
		return retval;
	}

	public String getStatus(String port, long timestamp) {
		int i;
		String retval = "";
		String busName = connector.getName();
		String compactName = busName + "." + getAddress();
		if (hasDirtyCache()) {
			updateStatus();
		}
		for (i = 0; i < outPortsNum; i++) {
			if ((timestamp <= outPortsTimestamps[i]) &&
					(port.equals("*") || port.equals(getOutputPortId(i)))){
				retval += compactName + ":" + getOutputPortId(i) + "=" +
					outPorts[i] + "\n";
			}
		}
		return retval;
	}

	
	/**
	 * Manda un messaggio per impostare un canale del dimmer.
	 * 
	 * La velocità di accensione/spegnimento e' quella di default.
	 * 
	 * @return true se è arrivato un ACK del messaggio
	 *
	 * @param output il numero dell'uscita (di solito 0 o 1)
	 * @param value il valore da impostare (da 0 a 100, dove 0 e' OFF)
	 */
	public boolean setOutput(int output, int value) {
		boolean retval = false;
		if ((output >= 0) && (output <= outPortsNum)) {
			if ((value >= 0) && (value <= 100)) {
				ComandoUscitaMessage m;
				m = new ComandoUscitaMessage(getIntAddress(), getBMCComputerAddress(), 0, output, value, 
						(value > 0)? 1 : 0);
				retval = connector.sendMessage(m);
			} else {
				logger.error("Valore non valido per canale dimmer: " +
						value);
			}
		} else {
			logger.error("Porta dimmer non valida: " + output);
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
				connector.sendMessage(m);
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
	public void poke(String port, String value) throws EDSException {
		int outPort, numericValue;
		outPort = getOutputNumberFromPortId(port);
		if (outPort == -1) {
			throw new EDSException("Porta non valida: " + port);
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
				throw new EDSException("Valore non valido: " + value);
			}
		}
		setOutputRealTime(outPort, numericValue);
	}

	public int getInPortsNumber() {
		return 0;
	}

	public String peek(String portId) throws AISException {
		// TODO Auto-generated method stub
		return null;
	}

}
