/**
 * Copyright (C) 2008 ASCIA S.r.l.
 */
package it.ascia.eds.device;

import java.util.LinkedHashSet;
import java.util.Set;

import it.ascia.ais.AISException;
import it.ascia.ais.Connector;
import it.ascia.eds.EDSConnector;
import it.ascia.eds.msg.ImpostaParametroMessage;
import it.ascia.eds.msg.EDSMessage;
import it.ascia.eds.msg.RichiestaAssociazioneUscitaMessage;
import it.ascia.eds.msg.RichiestaParametroMessage;
import it.ascia.eds.msg.RichiestaSetPointMessage;
import it.ascia.eds.msg.RichiestaStatoTermostatoMessage;
import it.ascia.eds.msg.RispostaAssociazioneUscitaMessage;
import it.ascia.eds.msg.RispostaParametroMessage;
import it.ascia.eds.msg.RispostaSetPointMessage;
import it.ascia.eds.msg.RispostaStatoTermostatoMessage;

/**
 * @author sergio
 *
 */
public class BMCRegT22 extends BMCStandardIO {
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
	 * Nome della porta della temperatura impostata.
	 */
	private static final String port_setPoint = "setPoint";
	/**
	 * Costruttore.
	 * @param connector 
	 * @throws AISException 
	 */
	public BMCRegT22(Connector connector, String address, int model,  String name) throws AISException {
		super(connector, address, model, name);
		broadcastBindingsByPort = new Set[16];
		for (int i = 0; i < 16; i++) {
			broadcastBindingsByPort[i] = new LinkedHashSet();
		}
		addPort(port_temperature);
		addPort(port_mode);
		addPort(port_alarmTemp);
		addPort(port_autoSendTime);
		addPort(port_setPoint);
	}
	
	/* (non-Javadoc)
	 * @see it.ascia.eds.device.BMC#getFirstInputPortNumber()
	 */
	public int getFirstInputPortNumber() {
		return 1;
	}

	public int getInPortsNumber() {
		return 2;
	}

	/* (non-Javadoc)
	 * @see it.ascia.eds.device.BMC#getOutPortsNumber()
	 */
	public int getOutPortsNumber() {
		return 2;
	}


	/* (non-Javadoc)
	 * @see it.ascia.eds.device.BMC#getInfo()
	 */
	public String getInfo() {
		return getName() + "BMC regolatore di temperatura (modello " + model + ")";
	}

	public void discoverUscite() {
		// RegT non ha uscite configurabili
	}
	
    public void discoverBroadcastBindings(){
    	int outPort;
    	int casella;
    	EDSConnector connector = (EDSConnector) getConnector();
    	int connectorAddress = connector.getMyAddress();
    	for (outPort = 0; outPort < 16; outPort++) {
    		for (casella = 0; casella < getCaselleNumber(); casella++) {
    			RichiestaAssociazioneUscitaMessage m;
    			m = new RichiestaAssociazioneUscitaMessage(getIntAddress(),
    					connectorAddress, outPort, casella);
        		connector.queueMessage(m);
    		}
    	}
    }
	
	/* (non-Javadoc)
	 * @see it.ascia.eds.device.BMC#messageReceived(it.ascia.eds.msg.Message)
	 */
	public void messageReceived(EDSMessage m) throws AISException {
		switch (m.getMessageType()) {
		case EDSMessage.MSG_IMPOSTA_PARAMETRO:
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
		case EDSMessage.MSG_RICHIESTA_MODELLO:
		case EDSMessage.MSG_RICHIESTA_ASSOCIAZIONE_BROADCAST:
		case EDSMessage.MSG_CAMBIO_VELOCITA:
		case EDSMessage.MSG_PROGRAMMAZIONE:
		case EDSMessage.MSG_RICHIESTA_PARAMETRO:
		case EDSMessage.MSG_RICHIESTA_STATO_TERMOSTATO:
		case EDSMessage.MSG_RICHIESTA_SET_POINT:
			// messaggi gestiti dal BMC reale
			break;
		default:
			logger.warn("Messaggio non gestito: " + m.toString());			
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
		case EDSMessage.MSG_RISPOSTA_SET_POINT:
			RispostaSetPointMessage rspm = (RispostaSetPointMessage) m;
			setPortValue(port_setPoint,new Double(rspm.getSetPoint()));
			break;
		case EDSMessage.MSG_RISPOSTA_STATO_TERMOSTATO:
			RispostaStatoTermostatoMessage rsttm = (RispostaStatoTermostatoMessage) m;
			setPortValue(port_temperature,new Double(rsttm.getSensorTemperature()));
			setPortValue(port_mode,getModeAsString(rsttm.getMode()));
			break;
		default:
			super.messageSent(m);
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
	
	/**
	 * Richiede il setPoint.
	 * 
	 * <p>Invia un messaggio al BMC richiedendo il valore del parametro.</p>
	 */
	public long updateSetPoint() {
		getConnector().sendMessage(new RichiestaSetPointMessage(getIntAddress(), getBMCComputerAddress()));
		// FIXME calcolare il timeout
		return 300;
	}

	public long updateStatus() {
		long timeout = 0;
		timeout += updateAlarmTemperature();
		timeout += updateAutoSendTime();
		timeout += updateTermStatus();
		timeout += updateSetPoint();
		return timeout;
	}
	
	public long updatePort(String portId) {
		if (portId.startsWith("Inp")) {
			try {
				setPortValue(portId, new Boolean(false));
			} catch (AISException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//FIXME Gestire aggiornamento stato, se applicabile
			return 0;
		} else if (portId.startsWith("Out")) {
			// le porte "Out" sono "finte", quindi non possono essere aggiornate 
			try {
				setPortValue(portId, new Boolean(false));
			} catch (AISException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return super.updateStatus();
			//return 0;
		} else {
			return updateStatus();
		}
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
	 
	
	public boolean writePort(String portId, Object newValue) throws AISException {
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
