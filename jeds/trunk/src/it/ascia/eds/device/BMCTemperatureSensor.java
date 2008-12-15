/**
 * Copyright (C) 2008 ASCIA S.r.l.
 */
package it.ascia.eds.device;

import it.ascia.ais.AISException;
import it.ascia.eds.EDSConnector;
import it.ascia.eds.EDSException;
import it.ascia.eds.msg.ImpostaParametroMessage;
import it.ascia.eds.msg.EDSMessage;
import it.ascia.eds.msg.RichiestaParametroMessage;
import it.ascia.eds.msg.RichiestaStatoTermostatoMessage;
import it.ascia.eds.msg.RispostaParametroMessage;
import it.ascia.eds.msg.TemperatureMessage;

/**
 * @author capoccia
 *
 */
public class BMCTemperatureSensor extends BMC {
	/**
	 * Modalita' OFF.
	 */
	private final static int MODE_OFF = 0;
	/**
	 * Modalita' crono.
	 */
	private final static int MODE_CRONO = 1;
	/**
	 * Modalita' manuale.
	 */
	private final static int MODE_MANUAL = 2;
	/**
	 * Array per trasformare la modalita' da numero a stringa.
	 */
	private final static String modeStrings[] = {
		"OFF", // 0
		"crono", // 1
		"manual" // 2
	};
	/**
	 * Nome compatto della porta "temperatura"
	 */
	private static final String port_temperature = "temp";
	/**
	 * Nome compatto della porta "temperatura di allarme".
	 */
	private static final String port_alarmTemp = "alarmTemp";
	/**
	 * Nome compatto della porta "tempo di invio automatico".
	 */
	private static final String port_autoSendTime = "autoSendTime";
	/**
	 * Nome compatto della porta "modalita' di funzionamento della sonda".
	 */
	private static final String port_mode = "mode";
	/**
	 * Tempo di auto-invio.
	 */
	private int autoSendTime;
	/**
	 * True se autoSendTime non e' aggiornato.
	 */
	private boolean dirtyAutoSendTime;
	/**
	 * Timestamp di aggiornamento di autoSendTime.
	 */
	private long autoSendTimeTimestamp = 0;
	/**
	 * Temperatura di allarme.
	 */
	private int alarmTemperature;
	/**
	 * True se alarmTemperature non e' aggiornata.
	 */
	private boolean dirtyAlarmTemperature;
	/**
	 * Timestamp di aggiornamento di alarmTemperature.
	 */
	private long alarmTemperatureTimestamp = 0;
	/**
	 * Temperatura (ultima lettura).
	 */
	private double temperature;
	/**
	 * True se la temperatura non e' mai stata letta.
	 */
	private boolean dirtyTemperature;
	/**
	 * Timestamp di aggiornamento della temperatura.
	 */
	private long temperatureTimestamp = 0;
	/**
	 * Modalita' di funzionamento.
	 * 
	 * <p>Si vedano le costanti MODE_*</p>
	 */
	private int mode;
	/**
	 * True se la modalita' di funzionamento non e' mai stata letta.
	 */
	private boolean dirtyMode;
	/**
	 * Timestamp di aggiornamento della modalità di funzionamento.
	 */
	private long modeTimestamp = 0;
	
	/**
	 * Costruttore.
	 */
	public BMCTemperatureSensor(int address, int model, EDSConnector bus, String name) {
		super(address, model, bus, name);
		dirtyAutoSendTime = true;
		dirtyAlarmTemperature = true;
		dirtyTemperature = true;
		dirtyMode = true;
	}
	
	/* (non-Javadoc)
	 * @see it.ascia.eds.device.BMC#getFirstInputPortNumber()
	 */
	public int getFirstInputPortNumber() {
		return 1;
	}

	/* (non-Javadoc)
	 * @see it.ascia.eds.device.BMC#getInfo()
	 */
	public String getInfo() {
		return getName() + "BMC sonda di temperatura (modello " + model + ")";
	}

	/* (non-Javadoc)
	 * @see it.ascia.eds.device.BMC#getOutPortsNumber()
	 */
	public int getOutPortsNumber() {
		// TODO verificare
		return 0;
	}

	/* (non-Javadoc)
	 * @see it.ascia.eds.device.BMC#messageReceived(it.ascia.eds.msg.Message)
	 */
	public void messageReceived(EDSMessage m) {
		long currentTime = System.currentTimeMillis();
		switch (m.getMessageType()) {
		case EDSMessage.MSG_IMPOSTA_PARAMETRO: {
			// Aggiorniamo i parametri interni e li contrassegnamo "dirty"
			ImpostaParametroMessage mesg = (ImpostaParametroMessage) m;
			if (mesg.hasAutoSendTime()) {
				int oldTime = autoSendTime;
				autoSendTime = mesg.getAutoSendTime();
				if (oldTime != autoSendTime) {
					autoSendTimeTimestamp = currentTime;
					generateEvent(port_autoSendTime, 
							String.valueOf(autoSendTime));
				}
				dirtyAutoSendTime = true;
			} else if (mesg.hasAlarmTemperature()) {
				int oldTemp = alarmTemperature;
				alarmTemperature = mesg.getAlarmTemperature();
				if (oldTemp != alarmTemperature) {
					alarmTemperatureTimestamp = currentTime;
					generateEvent(port_alarmTemp, 
							String.valueOf(alarmTemperature));
				}
				dirtyAlarmTemperature = true;
			} else {
				logger.warn("Ricevuto messaggio di impostazione per un " +
						"parametro sconosciuto: " + mesg.toString());
			}
			break;
		}
		}
	}

	/* (non-Javadoc)
	 * @see it.ascia.eds.device.BMC#messageSent(it.ascia.eds.msg.Message)
	 */
	public void messageSent(EDSMessage m) {
		long currentTime = System.currentTimeMillis();
		switch(m.getMessageType()) {
		case EDSMessage.MSG_RISPOSTA_PARAMETRO: {
			RispostaParametroMessage mesg =
				(RispostaParametroMessage) m;
			if (mesg.hasAlarmTemperature()) {
				int oldTemp = alarmTemperature;
				alarmTemperature = mesg.getAlarmTemperature();
				if (oldTemp != alarmTemperature) {
					alarmTemperatureTimestamp = currentTime;
					generateEvent(port_alarmTemp,
							String.valueOf(alarmTemperature));
				}
				dirtyAlarmTemperature = false;
			} else if (mesg.hasAutoSendTime()) {
				int oldTime = autoSendTime;
				autoSendTime = mesg.getAutoSendTime();
				if (oldTime != autoSendTime) {
					autoSendTimeTimestamp = currentTime;
					generateEvent(port_autoSendTime,
							String.valueOf(autoSendTime));
				}
				dirtyAutoSendTime = false;
			} else {
				logger.warn("Ricevuto messaggio di lettura parametro " +
						"sconosciuto: " + m.toString());
			}
			break;
		}
		case EDSMessage.MSG_TEMPERATURA: {
			TemperatureMessage mesg = (TemperatureMessage) m;
			double oldTemp = temperature;
			int oldMode = mode;
			temperature = mesg.getSensorTemperature();
			if (oldTemp != temperature) {
				temperatureTimestamp = currentTime;
				generateEvent(port_temperature, String.valueOf(temperature));
			}
			dirtyTemperature = false;
			dirtyMode = false;
			// Questo switch e' ridondante, ma ci permette di verificare che il
			// valore sia valido.
			switch (mesg.getMode()) {
			case MODE_OFF:
				mode = MODE_OFF;
				break;
			case MODE_CRONO:
				mode = MODE_CRONO;
				break;
			case MODE_MANUAL:
				mode = MODE_MANUAL;
				break;
			default:
				logger.warn("Modalita' di funzionamento non valida: " + 
						mesg.getMode());
				dirtyMode = true;
			}
			if (oldMode != mode) {
				modeTimestamp = currentTime;
				generateEvent(port_mode, getModeAsString());
			}
			break;
			}
		} // switch tipo di messaggio
	}
	
	/**
	 * Aggiorna lo stato del sensore (temperatura, modalita' di funzionamento).
	 */
	public void updateTermStatus() {
		connector.sendMessage(new RichiestaStatoTermostatoMessage(getIntAddress(), 
				connector.getBMCComputerAddress()));
	}
	
	/**
	 * Aggiorna la temperatura di allarme.
	 * 
	 * <p>Invia un messaggio al BMC richiedendo il valore del parametro.</p>
	 */
	public void updateAlarmTemperature() {
		connector.sendMessage(new RichiestaParametroMessage(getIntAddress(), 
				connector.getBMCComputerAddress(), 
				RichiestaParametroMessage.PARAM_TERM_ALARM_TEMPERATURE));
	}
	
	/**
	 * Aggiorna il tempo di invio automatico.
	 * 
	 * <p>Invia un messaggio al BMC richiedendo il valore del parametro.</p>
	 */
	public void updateAutoSendTime() {
		connector.sendMessage(new RichiestaParametroMessage(getIntAddress(), 
				connector.getBMCComputerAddress(), 
				RichiestaParametroMessage.PARAM_TERM_AUTO_SEND_TIME));
	}
	
	public void updateStatus() {
		updateAlarmTemperature();
		updateAutoSendTime();
		updateTermStatus();
	}
	
	/**
	 * Ritorna lo stato della sonda sotto forma di stringa.
	 */
	 public String getModeAsString() {
		// Sanity check
		if ((mode >= 0) && (mode < modeStrings.length)) {
			return modeStrings[mode];
		} else {
			logger.error("Internal mode is invalid: " + mode);
			return "ERROR";
		}
	}
	 
	/**
	 * Imposta la temperatura di allarme, inviando un messaggio al BMC.
	 * 
	 * @param temp temperatura da impostare.
	 * 
	 * @return true se il BMC ha risposto.
	 */
	public boolean setAlarmTemperature(int temp) {
		return connector.sendMessage(new ImpostaParametroMessage(getIntAddress(), 
				connector.getBMCComputerAddress(), 
				RichiestaParametroMessage.PARAM_TERM_ALARM_TEMPERATURE,
				temp,
				ImpostaParametroMessage.PARM_TYPE_TEMPERATURE));
	}
	
	/**
	 * Imposta il tempo di invio automatico, inviando un messaggio al BMC.
	 * 
	 * @param time tempo da impostare, in secondi.
	 * 
	 * @return true se il BMC ha risposto.
	 */
	public boolean setAutoSendTime(int time) {
		return connector.sendMessage(new ImpostaParametroMessage(getIntAddress(), 
				connector.getBMCComputerAddress(), 
				RichiestaParametroMessage.PARAM_TERM_ALARM_TEMPERATURE,
				time,
				ImpostaParametroMessage.PARM_TYPE_TIME));
	}
	
	/* (non-Javadoc)
	 * @see it.ascia.ais.Device#getStatus(java.lang.String)
	 */
	public String getStatus(String port, long timestamp) {
		String retval = "";
		String busName = connector.getName();
		String compactName = busName + "." + getAddress();
		if (dirtyAlarmTemperature) {
			updateAlarmTemperature();
		}
		if (dirtyAutoSendTime) {
			updateAutoSendTime();
		}
		updateTermStatus();
		if ((timestamp <= temperatureTimestamp) && 
				(port.equals("*") || port.equals(port_temperature))) {
			retval += compactName + ":" + port_temperature + "=" + temperature + 
				"\n";
		}
		if ((timestamp <= alarmTemperatureTimestamp) && 
				(port.equals("*") || port.equals(port_alarmTemp))) {
			retval += compactName + ":" + port_alarmTemp + "=" + 
				alarmTemperature + "\n";
		}
		if ((timestamp <= autoSendTimeTimestamp) && 
				(port.equals("*") || port.equals(port_autoSendTime))) {
			retval += compactName + ":" + port_autoSendTime + "=" +
				autoSendTime + "\n";
		}
		if ((timestamp <= modeTimestamp) && 
				(port.equals("*") || port.equals(port_mode))) {
			retval += compactName + ":" + port_mode + "=" + 
				getModeAsString() + "\n";
		}
		return retval;
	}

	/* (non-Javadoc)
	 * @see it.ascia.ais.Device#setPort(java.lang.String, java.lang.String)
	 */
	public void setPort(String port, String value) throws AISException {
		boolean success = false;
		if (port.equals(port_alarmTemp)) {
			// Impostiamo la temperatura di allarme
			int alarmTemp;
			try {
				alarmTemp = (int)(Double.parseDouble(value));
			} catch (NumberFormatException e) {
				throw new EDSException("Invalid alarm temperature: " + value);
			}
			success = setAlarmTemperature(alarmTemp);
		} else if (port.equals(port_autoSendTime)) {
			// Impostiamo il periodo di auto-invio
			int time;
			try {
				time = (int)(Double.parseDouble(value));
			} catch (NumberFormatException e) {
				throw new EDSException("Invalid auto send time: " + value);
			}
			if (time <= 0) {
				throw new EDSException("Illegal value: " + value);
			}
			success = setAutoSendTime(time);
		} else {
			// Non impostiamo niente: la richiesta e' errata.
			throw new EDSException("Invalid port: " + port);
		}
		if (!success) {
			throw new EDSException("Il device non risponde");
		}
	}
	
	public void printStatus() {
		System.out.println("Stato: " + getModeAsString() + " (" + mode + ")" + 
				(dirtyMode? "?" : ""));
		System.out.println("Temperatura: " + temperature + 
				(dirtyTemperature? "?" : ""));
		System.out.println("Tempo di auto invio: " + autoSendTime + 
				(dirtyAutoSendTime? "?" : ""));
	}

	public int getInPortsNumber() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String peek(String portId) throws AISException {
		// TODO Auto-generated method stub
		return null;
	}

	public void poke(String portId, String value) throws AISException {
		// TODO Auto-generated method stub
		
	}

}
