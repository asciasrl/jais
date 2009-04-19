/**
 * Copyright (C) 2008 ASCIA S.r.l.
 */
package it.ascia.eds.device;

import it.ascia.ais.AISException;
import it.ascia.ais.Connector;
import it.ascia.eds.msg.ImpostaParametroMessage;
import it.ascia.eds.msg.EDSMessage;
import it.ascia.eds.msg.RichiestaParametroMessage;
import it.ascia.eds.msg.RichiestaStatoTermostatoMessage;
import it.ascia.eds.msg.RispostaParametroMessage;
import it.ascia.eds.msg.RispostaStatoTermostatoMessage;

/**
 * @author capoccia
 *
 */
public class BMCTemperatureSensor extends BMC {
	/**
	 * Modalita' OFF.
	 */
	//private final static int MODE_OFF = 0;
	/**
	 * Modalita' crono.
	 */
	//private final static int MODE_CRONO = 1;
	/**
	 * Modalita' manuale.
	 */
	//private final static int MODE_MANUAL = 2;
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
	 * Costruttore.
	 * @param connector 
	 * @throws AISException 
	 */
	public BMCTemperatureSensor(Connector connector, String address, int model,  String name) throws AISException {
		super(connector, address, model, name);
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
	public void messageReceived(EDSMessage m) throws AISException {
		switch (m.getMessageType()) {
		case EDSMessage.MSG_IMPOSTA_PARAMETRO: {
			// Aggiorniamo i parametri interni e li contrassegnamo "dirty"
			ImpostaParametroMessage mesg = (ImpostaParametroMessage) m;
			if (mesg.hasAutoSendTime()) {
				setPortValue(port_autoSendTime,new Integer(mesg.getAutoSendTime()));
			} else if (mesg.hasAlarmTemperature()) {
				setPortValue(port_alarmTemp,new Integer(mesg.getAlarmTemperature()));
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
	public void messageSent(EDSMessage m) throws AISException {
		switch(m.getMessageType()) {
		case EDSMessage.MSG_RISPOSTA_PARAMETRO:
			RispostaParametroMessage rpm = (RispostaParametroMessage) m;
			if (rpm.hasAlarmTemperature()) {
				setPortValue(port_alarmTemp,new Integer(rpm.getAlarmTemperature()));
			} else if (rpm.hasAutoSendTime()) {
				setPortValue(port_autoSendTime,new Integer(rpm.getAutoSendTime()));
			} else {
				logger.warn("Ricevuto messaggio di lettura parametro " +
						"sconosciuto: " + m.toString());
			}
			break;
		case EDSMessage.MSG_RISPOSTA_STATO_TERMOSTATO:
			RispostaStatoTermostatoMessage rsttm = (RispostaStatoTermostatoMessage) m;
			setPortValue(port_temperature,new Double(rsttm.getSensorTemperature()));
			setPortValue(port_mode,getModeAsString(rsttm.getMode()));
			break;
		default:
			logger.warn("Messaggio sconosciuto: " + m.toString());			
		} // switch tipo di messaggio
	}
	
	/**
	 * Aggiorna lo stato del sensore (temperatura, modalita' di funzionamento).
	 */
	public long updateTermStatus() {
		getConnector().sendMessage(new RichiestaStatoTermostatoMessage(getIntAddress(), getBMCComputerAddress()));
		// FIXME calcolare il timeout
		return 300;
	}
	
	/**
	 * Aggiorna la temperatura di allarme.
	 * 
	 * <p>Invia un messaggio al BMC richiedendo il valore del parametro.</p>
	 */
	public long updateAlarmTemperature() {
		RichiestaParametroMessage m = new RichiestaParametroMessage(getIntAddress(), getBMCComputerAddress(), 
				ImpostaParametroMessage.PARAM_TERM_ALARM_TEMPERATURE);
		getConnector().sendMessage(m);
		// FIXME calcolare il timeout
		return 300;
	}
	
	/**
	 * Aggiorna il tempo di invio automatico.
	 * 
	 * <p>Invia un messaggio al BMC richiedendo il valore del parametro.</p>
	 */
	public long updateAutoSendTime() {
		getConnector().sendMessage(new RichiestaParametroMessage(getIntAddress(), getBMCComputerAddress(), 
				ImpostaParametroMessage.PARAM_TERM_AUTO_SEND_TIME));
		// FIXME calcolare il timeout
		return 300;
	}
	
	public long updateStatus() {
		long timeout = 0;
		timeout += updateAlarmTemperature();
		timeout += updateAutoSendTime();
		timeout += updateTermStatus();
		return timeout;
	}
	
	/**
	 * Ritorna lo stato della sonda sotto forma di stringa.
	 */
	 private String getModeAsString(int mode) {
		// Sanity check
		if ((mode >= 0) && (mode < modeStrings.length)) {
			return modeStrings[mode];
		} else {
			logger.error("Internal mode is invalid: " + mode);
			return "ERROR";
		}
	}
	 
	
	public int getInPortsNumber() {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean sendPortValue(String portId, Object newValue) throws AISException {
		if (portId.equals(port_autoSendTime)) {
			return getConnector().sendMessage(new ImpostaParametroMessage(getIntAddress(), getBMCComputerAddress(), 
					((Integer) newValue).intValue(),
					ImpostaParametroMessage.PARM_TEMPERATURE));
		} else
		if (portId.equals(port_alarmTemp)) {
			return getConnector().sendMessage(new ImpostaParametroMessage(getIntAddress(), getBMCComputerAddress(), 
					((Integer) newValue).intValue(),					
					ImpostaParametroMessage.PARAM_TERM_ALARM_TEMPERATURE));
		} else 
		if (portId.equals(port_autoSendTime)) {
			return getConnector().sendMessage(new ImpostaParametroMessage(getIntAddress(), getBMCComputerAddress(), 
				ImpostaParametroMessage.PARM_TIME,
				((Integer) newValue).intValue()));
		} else {
			logger.fatal("Non so come scrivere sulla porta "+portId);
			return false;
		}
	}

}
