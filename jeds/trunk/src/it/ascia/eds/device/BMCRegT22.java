/**
 * Copyright (C) 2008 ASCIA S.r.l.
 */
package it.ascia.eds.device;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashSet;
import java.util.Set;
import it.ascia.ais.AISException;
import it.ascia.ais.Connector;
import it.ascia.ais.DatePort;
import it.ascia.ais.DevicePort;
import it.ascia.ais.IntegerPort;
import it.ascia.ais.StatePort;
import it.ascia.ais.TemperaturePort;
import it.ascia.ais.TemperatureSetpointPort;
import it.ascia.eds.EDSConnector;
import it.ascia.eds.msg.ImpostaParametroMessage;
import it.ascia.eds.msg.EDSMessage;
import it.ascia.eds.msg.ImpostaRTCCMessage;
import it.ascia.eds.msg.ImpostaSetPointMessage;
import it.ascia.eds.msg.RichiestaAssociazioneUscitaMessage;
import it.ascia.eds.msg.RichiestaParametroMessage;
import it.ascia.eds.msg.RichiestaRTCCMessage;
import it.ascia.eds.msg.RichiestaSetPointMessage;
import it.ascia.eds.msg.RichiestaStatoTermostatoMessage;
import it.ascia.eds.msg.RispostaParametroMessage;
import it.ascia.eds.msg.RispostaRTCCMessage;
import it.ascia.eds.msg.RispostaSetPointMessage;
import it.ascia.eds.msg.RispostaStatoTermostatoMessage;

/**
 * 
 * Per forzare l'aggiornamento della porta RTCC basta impostarla al valore null
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
	 * Nome della porta della data/ora
	 */
	private static final String port_RTCC = "RTCC";

	/**
	 * Tempo di auto-invio della temperatura, per una sonda termica.
	 */
	public static final int PARAM_TERM_AUTO_SEND_TIME = 1;

	/**
	 * Temperatura di allarme, per una sonda termica.
	 */
	public static final int PARAM_TERM_ALARM_TEMPERATURE = 3;
		
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
		addPort(new TemperaturePort(this,port_temperature));
		addPort(new StatePort(this,port_mode));
		addPort(new TemperaturePort(this,port_alarmTemp));
		addPort(new IntegerPort(this,port_autoSendTime));
		addPort(new TemperaturePort(this,port_setPoint));
		addPort(new DatePort(this,port_RTCC));
		for (int stagione = 0; stagione <= 1; stagione++) {
			for (int giorno = 0; giorno <= 6; giorno++) {
				for (int ora = 0; ora <= 23; ora++) {
					addPort(new TemperatureSetpointPort(this,getSetPointPortId(stagione,giorno,ora)));					
				}
			}		
		}
	}
	
	public static String getSetPointPortId(int stagione, int giorno, int ora) {
		if (ora == 31) {
			return "setPoint";
		} else {
			//return "setPoint-"+ImpostaSetPointMessage.stagione(stagione)+"-"+ImpostaSetPointMessage.giorno(giorno)+"-"+ImpostaSetPointMessage.ora(ora);
			return "setPoint-"+stagione+"-"+giorno+"-"+ora;
		}
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
		return getName() + " Cronotermostato WDB (modello " + model + ")";
	}

	public void discover() {
		discoverBroadcastBindings();
		readSetPoints();
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

    /**
	 * Ritorna il valore del campo "tempo di auto invio" [sec].
	 * 
	 * @return il valore in secondi del parametro.
	 */    
    static Integer autoSendTime(int value) { 
	    if ((value & 0x80) == 0) { // Minuti
			return new Integer(value * 60);
		} else { // Secondi
			return new Integer(value);
		}
    }

	/**
	 * Ritorna la temperatura di allarme [gradi C].
	 * 
	 * @return la temperatura di allarme.
	 */
	static Integer alarmTemperature(int value) {
		int temp = (value & 0x7f);
		if ((value & 0x80) == 0) {
			return new Integer(temp);
		} else {
			return new Integer(-temp);
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
			switch (mesg.getParameter()) {
				case PARAM_TERM_AUTO_SEND_TIME: 
					setPortValue(port_autoSendTime,autoSendTime(mesg.getValue()));
					break;
				case PARAM_TERM_ALARM_TEMPERATURE:
					setPortValue(port_alarmTemp,alarmTemperature(mesg.getValue()));
					break;
				default:
					logger.warn("Parametro non gestito: " + mesg.getParameter()+" Value:"+mesg.getValue());
			}
			break;
		case EDSMessage.MSG_RICHIESTA_MODELLO:
		case EDSMessage.MSG_RICHIESTA_ASSOCIAZIONE_BROADCAST:
		case EDSMessage.MSG_CAMBIO_VELOCITA:
		case EDSMessage.MSG_PROGRAMMAZIONE:
		case EDSMessage.MSG_RICHIESTA_PARAMETRO:
		case EDSMessage.MSG_RICHIESTA_STATO:
		case EDSMessage.MSG_IMPOSTA_SET_POINT:
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
			switch (rpm.getParameter()) {
				case PARAM_TERM_ALARM_TEMPERATURE:
					setPortValue(port_alarmTemp,new Integer(rpm.getValue()));
					break;
				case PARAM_TERM_AUTO_SEND_TIME:
					setPortValue(port_autoSendTime,new Integer(rpm.getValue()));
					break;
				default:
					logger.warn("Parametro sconosciuto: " + rpm.getParameter()+" Value:"+rpm.getValue());					
			}
			break;
		case EDSMessage.MSG_RISPOSTA_SET_POINT:
			RispostaSetPointMessage risp = (RispostaSetPointMessage) m;
			RichiestaSetPointMessage rich = (RichiestaSetPointMessage) risp.getRequest();
			int stagione = risp.getStagione();			
			if (rich != null && rich.getStagione() != stagione) {
				logger.error("Risposta 'stagione' non coerente. "+rich+" "+risp);
			}
			int giorno = risp.getGiorno();
			if (rich != null && rich.getGiorno() != giorno) {
				logger.error("Risposta 'giorno' non coerente");
			}
			int ora = risp.getOra();
			if (rich != null && rich.getOra() != ora) {
				logger.error("Risposta 'ora' non coerente");
			}
			DevicePort p = getPort(getSetPointPortId(stagione,giorno,ora));
			p.setCacheRetention(86400000);	// 1 giorno		
			p.setValue(new Double(risp.getSetPoint()));
			break;
		case EDSMessage.MSG_RISPOSTA_STATO_TERMOSTATO:
			RispostaStatoTermostatoMessage rsttm = (RispostaStatoTermostatoMessage) m;
			setPortValue(port_temperature,new Double(rsttm.getSensorTemperature()));
			setPortValue(port_mode,getModeAsString(rsttm.getMode()));
			break;
		case EDSMessage.MSG_RISPOSTA_RTCC:
			RispostaRTCCMessage rrtcc = (RispostaRTCCMessage) m;
			Date d = (Date) getPortCachedValue(port_RTCC);
			Calendar c = new GregorianCalendar();
			if (d == null) {
				c.setTimeInMillis(0);
			} else {
				c.setTime(d);
			}
			// TODO: verificare che siano arrivati tutti e 3 in ordine 
			switch (rrtcc.getIndex()) {
			case 0:
				c.set(Calendar.HOUR_OF_DAY,rrtcc.getOre());
				c.set(Calendar.MINUTE,rrtcc.getMinuti());
				break;
			case 1:
				c.set(Calendar.MONTH,rrtcc.getMese()-1);
				c.set(Calendar.YEAR,rrtcc.getAnno()+2000);
				break;
			case 2:
				c.set(Calendar.DAY_OF_MONTH,rrtcc.getGiorno());
				c.set(Calendar.SECOND,rrtcc.getSecondi());
				break;
			}
			setPortValue(port_RTCC,c.getTime());
			break;
		default:
			super.messageSent(m);
		}
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
				PARAM_TERM_ALARM_TEMPERATURE);
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
				PARAM_TERM_AUTO_SEND_TIME));
		// FIXME calcolare il timeout
		return 300;
	}
	
	/**
	 * Richiede il setPoint.
	 * 
	 * <p>Invia un messaggio al BMC richiedendo il valore del parametro.</p>
	 */
	public long updateSetPoint() {
		getConnector().sendMessage(new RichiestaSetPointMessage(getIntAddress(), getBMCComputerAddress(), true));
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
			return super.updateStatus();
		} else if (portId.startsWith("Out")) {
			return super.updateStatus();
		} else if (portId.equals(port_RTCC)) {
			readRTCC();
			return 300;
		} else if (portId.startsWith("setPoint-")) {
			readSetPoint(portId);
			return 300;
		} else {
			return updateStatus();
		}
	}
	
	private void readRTCC() {
		EDSConnector conn = (EDSConnector) getConnector();
		int m = conn.getMyAddress();
		int d = getIntAddress();
		conn.queueMessage(new RichiestaRTCCMessage(d,m,0));
		conn.queueMessage(new RichiestaRTCCMessage(d,m,1));
		conn.queueMessage(new RichiestaRTCCMessage(d,m,2));
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
	 
	
	public boolean sendPortValue(String portId, Object newValue) throws AISException {
		boolean res = false;
		if (portId.equals(port_autoSendTime)) {
			res = getConnector().sendMessage(new ImpostaParametroMessage(getIntAddress(), getBMCComputerAddress(), 
					((Integer) newValue).intValue(),
					PARAM_TERM_AUTO_SEND_TIME)); 
		} else if (portId.equals(port_alarmTemp)) {
			res = getConnector().sendMessage(new ImpostaParametroMessage(getIntAddress(), getBMCComputerAddress(), 
					((Integer) newValue).intValue(),					
					PARAM_TERM_ALARM_TEMPERATURE));
		} else if (portId.equals(port_RTCC)) {
			try {
				GregorianCalendar cal = new GregorianCalendar();
				Date date;
				DateFormat df = DateFormat.getDateTimeInstance();				
				if (((String)newValue).toLowerCase().equals("now")) {
					date = new Date();
					logger.debug("Set to now: "+date);
				} else {
					date = df.parse((String) newValue);
					cal.setTime(date);
					logger.debug("Parsed data: "+newValue+" -> "+cal.getTime().toString());
				}
				EDSConnector conn = (EDSConnector) getConnector();
				int m = conn.getMyAddress();
				int d = getIntAddress();
				conn.queueMessage(new ImpostaRTCCMessage(d,m,0, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE)));
				conn.queueMessage(new ImpostaRTCCMessage(d,m,1, cal.get(Calendar.MONTH)+1, cal.get(Calendar.YEAR)-2000));
				conn.queueMessage(new ImpostaRTCCMessage(d,m,2, cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.SECOND)));
				res = true;
				newValue = date;
			} catch (ParseException e) {
				logger.error("Data non valida: "+e);
			}
		} else if (portId.startsWith("setPoint-")) {
			String[] temp = portId.split("-");
			String stagione = temp[1];
			String giorno = temp[2];
			int ora = Integer.parseInt(temp[3]);
			//logger.info("write:"+stagione+","+giorno+","+ora+"="+newValue);
			res = getConnector().sendMessage(new ImpostaSetPointMessage(getIntAddress(), getBMCComputerAddress(),
					Double.parseDouble((String) newValue), stagione, giorno, ora));
		} else {
			logger.fatal("Non so come scrivere sulla porta "+portId);
		}
		return res;
	}
	
	public void readSetPoints() {
		EDSConnector conn = (EDSConnector) getConnector();
		int m = conn.getMyAddress();
		int d = getIntAddress();
		for (int stagione = 0; stagione <= 1; stagione++) {
			for (int giorno = 0; giorno <= 6; giorno++) {
				for (int ora = 0; ora <= 23; ora++) {					
					RichiestaSetPointMessage rich = new RichiestaSetPointMessage(d,m,stagione,giorno,ora);
					conn.queueMessage(rich);
				}
			}		
		}
	}
	
	public void readSetPoint(String portId) {
		EDSConnector conn = (EDSConnector) getConnector();
		int m = conn.getMyAddress();
		int d = getIntAddress();
		String[] temp = portId.split("-");
		String stagione = temp[1];
		String giorno = temp[2];
		int ora = Integer.parseInt(temp[3]);
		RichiestaSetPointMessage rich = new RichiestaSetPointMessage(d,m,stagione,giorno,ora);
		conn.queueMessage(rich);
	}	

}
