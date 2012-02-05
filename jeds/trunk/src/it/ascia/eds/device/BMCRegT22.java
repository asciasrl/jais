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
import it.ascia.ais.DevicePort;
import it.ascia.ais.port.DatePort;
import it.ascia.ais.port.IntegerPort;
import it.ascia.ais.port.StatePort;
import it.ascia.ais.port.TemperaturePort;
import it.ascia.ais.port.TemperatureSetpointPort;
import it.ascia.ais.port.NullPort;
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
		"off", // 0
		"chrono", // 1
		"manual" // 2
	};
	/**
	 * Nome compatto della porta "temperatura"
	 */
	private static final String port_temperature = "temp";
	private static final String port_season = "season";

	/**
	 * 1 = Estate
	 * 0 = Inverno
	 */
	private static final String SEASONS[] = {"WINTER","SUMMER"};

	private static final String DAYS[] = {"Sun","Mon","Tue","Wed","Thu","Fri","Sat"};

	/**
	 * Nome compatto della porta "temperatura di allarme".
	 */
	private static final String port_alarmeTemp = "alarmTemp";
	private static final String port_T0 = "T0";
	private static final String port_T1_SUMMER = "T1-1";
	private static final String port_T1_WINTER = "T1-0";
	private static final String port_T2_SUMMER = "T2-1";
	private static final String port_T2_WINTER = "T2-0";
	private static final String port_T3_SUMMER = "T3-1";
	private static final String port_T3_WINTER = "T3-0";

	/**
	 * Porte virtuali, usate per inviare comandi
	 */
	private String virtual_RESET_DAY = "ResetDay";
	private String virtual_RESET_SEASON = "ResetSeason";

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
	private static final int PARAM_TERM_ALARM_TEMPERATURE = 3;
	private static final int PARAM_SEASON = 5;
	private static final int PARAM_MODE = 7;

	private static final int PARAM_CLEAR_SEASON = 40;
	private static final int PARAM_CLEAR_DAY = 41;

	private static final int PARAM_T0_VALUE = 78;
	private static final int PARAM_T1_SUMMER_VALUE = 69;
	private static final int PARAM_T1_WINTER_VALUE = 60;
	private static final int PARAM_T2_SUMMER_VALUE = 72;
	private static final int PARAM_T2_WINTER_VALUE = 63;
	private static final int PARAM_T3_SUMMER_VALUE = 75;
	private static final int PARAM_T3_WINTER_VALUE = 66;
	
	/**
	 * Offset in secondi per effettuare la reimpostazione dell'orologio
	 */
	private static final int MAX_CLOCK_OFFSET = 2;
		
	/**
	 * Costruttore.
	 * @param connector 
	 * @throws AISException 
	 */
	public BMCRegT22(String address, int model, int version, String name) throws AISException {
		super(address, model, version, name);
		broadcastBindingsByPort = new Set[16];
		for (int i = 0; i < 16; i++) {
			broadcastBindingsByPort[i] = new LinkedHashSet();
		}
		addPort(new TemperaturePort(port_temperature));
		addPort(new TemperatureSetpointPort(port_alarmeTemp));
		addPort(new StatePort(port_season,SEASONS));
		addPort(new StatePort(port_mode,modeStrings));
		addPort(new TemperatureSetpointPort(port_T0));
		addPort(new TemperatureSetpointPort(port_T1_SUMMER));
		addPort(new TemperatureSetpointPort(port_T1_WINTER));
		addPort(new TemperatureSetpointPort(port_T2_SUMMER));
		addPort(new TemperatureSetpointPort(port_T2_WINTER));
		addPort(new TemperatureSetpointPort(port_T3_SUMMER));
		addPort(new TemperatureSetpointPort(port_T3_WINTER));
		addPort(new IntegerPort(port_autoSendTime));
		addPort(new TemperatureSetpointPort(port_setPoint));
		addPort(new DatePort(port_RTCC));
		addPort(new NullPort(virtual_RESET_DAY));
		addPort(new NullPort(virtual_RESET_SEASON));
		for (int stagione = 0; stagione <= 1; stagione++) {
			for (int giorno = 0; giorno <= 6; giorno++) {
				for (int ora = 0; ora <= 23; ora++) {
					addPort(new TemperatureSetpointPort(getSetPointPortId(stagione,giorno,ora)));					
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

	public int getDigitalInputPortsNumber() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see it.ascia.eds.device.BMC#getOutPortsNumber()
	 */
	public int getDigitalOutputPortsNumber() {
		return 0;
	}


	/* (non-Javadoc)
	 * @see it.ascia.eds.device.BMC#getInfo()
	 */
	public String getInfo() {
		return getName() + " Cronotermostato WDB (modello " + model + ")";
	}

	public void discover() {
		writeRTCC();
		try {
			logger.trace("Attendo stabilizzazione BMC...");
			Thread.sleep(500);
		} catch (InterruptedException e) {
		}
		discoverBroadcastBindings();
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
        		connector.sendMessage(m);
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
	
	/**
	 * Converte la temperatura di setpoint di un parametro.
	 */
	private double setPoint(int value) {
		return (double)(value & 0x7F) + (double)((value & 0x80) >> 7) * 0.5;
	}
	
	/**
	 * Converte la temperatura di setpoint di un parametro.
	 */
	private int setPoint(double value) {
		if (value > 60.0 || value < 0) {
			throw(new IllegalArgumentException());
		}
		int t = (int)Math.floor(value);
		return (((value - new Double(t).doubleValue()) >= 0.5) ? 0x80  : 0) + (t & 0x7F); 
	}

	/* (non-Javadoc)
	 * @see it.ascia.eds.device.BMC#messageReceived(it.ascia.eds.msg.Message)
	 */
	public void messageReceived(EDSMessage m) throws AISException {
		switch (m.getMessageType()) {
		case EDSMessage.MSG_IMPOSTA_PARAMETRO:
		case EDSMessage.MSG_RICHIESTA_MODELLO:
		case EDSMessage.MSG_RICHIESTA_ASSOCIAZIONE_BROADCAST:
		case EDSMessage.MSG_CAMBIO_VELOCITA:
		case EDSMessage.MSG_PROGRAMMAZIONE:
		case EDSMessage.MSG_RICHIESTA_PARAMETRO:
		case EDSMessage.MSG_RICHIESTA_STATO:
		case EDSMessage.MSG_IMPOSTA_SET_POINT:
		case EDSMessage.MSG_RICHIESTA_STATO_TERMOSTATO:
		case EDSMessage.MSG_RICHIESTA_SET_POINT:
		case EDSMessage.MSG_RICHIESTA_RTCC:
		case EDSMessage.MSG_IMPOSTA_RTCC:
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
			// Il valore della porta viene aggiornato nel metodo updatePort()
			break;
		case EDSMessage.MSG_RISPOSTA_SET_POINT:
			RispostaSetPointMessage risp = (RispostaSetPointMessage) m;
			RichiestaSetPointMessage rich = (RichiestaSetPointMessage) risp.getRequest();
			int stagione = risp.getStagione();			
			if (rich != null && rich.getOra() != 31 && rich.getStagione() != stagione) {
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
			p.setValue(new Double(risp.getSetPoint()));
			break;
		case EDSMessage.MSG_RISPOSTA_STATO_TERMOSTATO:
			RispostaStatoTermostatoMessage rsttm = (RispostaStatoTermostatoMessage) m;
			setPortValue(port_temperature,new Double(rsttm.getSensorTemperature()));
			setPortValue(port_mode,getMode(rsttm.getMode()));
			break;
		case EDSMessage.MSG_RISPOSTA_RTCC:
			// Il valore della porta viene aggiornato nel metodo readRTCC()
			break;
		default:
			super.messageSent(m);
		}
	}
	
	/**
	 * Aggiorna lo stato del sensore (temperatura, modalita' di funzionamento).
	 */
	public boolean updateTermStatus() {
		return getConnector().sendMessage(new RichiestaStatoTermostatoMessage(getIntAddress(), getBMCComputerAddress()));
	}
	
	/**
	 * Aggiorna la temperatura di allarme.
	 * 
	 * <p>Invia un messaggio al BMC richiedendo il valore del parametro.</p>
	 */
	private boolean updateAlarmTemperature() {
		RichiestaParametroMessage m = new RichiestaParametroMessage(getIntAddress(), getBMCComputerAddress(), 
				PARAM_TERM_ALARM_TEMPERATURE);
		if (getConnector().sendMessage(m)) {
			RispostaParametroMessage rpm = (RispostaParametroMessage) m.getResponse();
			setPortValue(port_alarmeTemp,new Integer(rpm.getValue()));
			return true;
		} else {
			return false;
		}
	}

	private boolean readSeason() {
		RichiestaParametroMessage m = new RichiestaParametroMessage(getIntAddress(), getBMCComputerAddress(), 
				PARAM_SEASON);
		if (getConnector().sendMessage(m)) {
			RispostaParametroMessage rpm = (RispostaParametroMessage) m.getResponse();
			setPortValue(port_season,getSeason(rpm.getValue()));
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Aggiorna il tempo di invio automatico.
	 * 
	 * <p>Invia un messaggio al BMC richiedendo il valore del parametro.</p>
	 */
	private boolean updateAutoSendTime() {
		RichiestaParametroMessage m = new RichiestaParametroMessage(getIntAddress(), getBMCComputerAddress(), 
				PARAM_TERM_AUTO_SEND_TIME); 
		if (getConnector().sendMessage(m)) {
			RispostaParametroMessage rpm = (RispostaParametroMessage) m.getResponse();
			setPortValue(port_autoSendTime,new Integer(rpm.getValue()));
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Richiede il setPoint.
	 * 
	 * <p>Invia un messaggio al BMC richiedendo il valore del parametro.</p>
	 */
	private boolean updateSetPoint() {
		return getConnector().sendMessage(new RichiestaSetPointMessage(getIntAddress(), getBMCComputerAddress(), true));
	}

	/**
	 * @todo separare le chiamate
	 */
	public boolean updateStatus() {
		return updateAlarmTemperature() &&
		updateAutoSendTime() &&
		updateTermStatus() &&
		updateSetPoint();
	}
	
	public boolean updatePort(String portId) {
		if (portId.startsWith("Inp")) {
			return super.updateStatus();
		} else if (portId.startsWith("Out")) {
			return super.updateStatus();
		} else if (portId.equals(port_RTCC)) {
			return readRTCC();
		} else if (portId.equals(port_season)) {
			return readSeason();
		} else if (portId.startsWith("setPoint-")) {
			return readSetPoint(portId);
		} else if (portId.equals(port_T0)) {
			return readSetpointParameter(portId,PARAM_T0_VALUE);
		} else if (portId.equals(port_T1_SUMMER)) {
			return readSetpointParameter(portId,PARAM_T1_SUMMER_VALUE);
		} else if (portId.equals(port_T1_WINTER)) {
			return readSetpointParameter(portId,PARAM_T1_WINTER_VALUE);
		} else if (portId.equals(port_T2_SUMMER)) {
			return readSetpointParameter(portId,PARAM_T2_SUMMER_VALUE);
		} else if (portId.equals(port_T2_WINTER)) {
			return readSetpointParameter(portId,PARAM_T2_WINTER_VALUE);
		} else if (portId.equals(port_T3_SUMMER)) {
			return readSetpointParameter(portId,PARAM_T3_SUMMER_VALUE);
		} else if (portId.equals(port_T3_WINTER)) {
			return readSetpointParameter(portId,PARAM_T3_WINTER_VALUE);
		} else {
			return updateStatus();
		}
	}
	
	private boolean readRTCC() {
		EDSConnector conn = (EDSConnector) getConnector();
		int m = conn.getMyAddress();
		int d = getIntAddress();
		Calendar c = new GregorianCalendar();
		Date deviceDate = null;
		RichiestaRTCCMessage m0 = new RichiestaRTCCMessage(d,m,0);
		RichiestaRTCCMessage m1 = new RichiestaRTCCMessage(d,m,1);
		RichiestaRTCCMessage m2 = new RichiestaRTCCMessage(d,m,2); 
		conn.sendMessage(m0);
		if (m0.isAnswered()) {
			RispostaRTCCMessage r0 = (RispostaRTCCMessage) m0.getResponse(); 
			conn.sendMessage(m1);
			if (m1.isAnswered()) {
				RispostaRTCCMessage r1 = (RispostaRTCCMessage) m1.getResponse(); 
				conn.sendMessage(m2);
				if (m2.isAnswered()) {
					RispostaRTCCMessage r2 = (RispostaRTCCMessage) m2.getResponse(); 
					c.set(Calendar.HOUR_OF_DAY,r0.getOre());
					c.set(Calendar.MINUTE,r0.getMinuti());
					c.set(Calendar.MONTH,r1.getMese()-1);
					c.set(Calendar.YEAR,r1.getAnno()+2000);
					c.set(Calendar.DAY_OF_MONTH,r2.getGiorno());
					if (r2.getSecondi() == 0) {
						// ripete la lettura, perche' potrebbe essere affetta da errore di lettura
						return readRTCC();
					}
					c.set(Calendar.SECOND,r2.getSecondi());
					deviceDate = c.getTime();
					setPortValue(port_RTCC,deviceDate);
				}
			}
		}	
		//Determina se l'orologio e' sballato e nel caso lo sincronizza
		if (deviceDate != null) {
			long diff = deviceDate.getTime() - System.currentTimeMillis();
			if ( Math.abs(diff) > MAX_CLOCK_OFFSET * 1000) {
				logger.warn("Clock offset= "+diff+" mS");
				writeRTCC();
			} else {
				logger.trace("Clock offset= "+diff+" mS");
			}
		}
		return true;
	}
	
	/**
	 * Reimposta i setpoint della intera stagione
	 * @param season Stagione da cancellare {@link BMCRegT22.SEASONS}
	 * @return true if send ok
	 */
	private boolean clearSeason(int season) {
		logger.info("Cancellazione setPoint stagione "+getSeason(season));
		if (sendParameter(PARAM_CLEAR_SEASON,season & 0x01)) {
			for (int day = 0; day < 7; day++) {
				for (int h = 0; h < 24 ; h++) {
					invalidate("setPoint-"+season+"-"+day+"-"+h);
				}
			}
			return true;
		} else {
			return false;			
		}
	}
	
	private boolean clearSeason(String value) {
		int season = Integer.parseInt(value);
		return clearSeason(season);
	}
	
	/**
	 * Reimposta i setpoint della intera stagione
	 * @param season Stagione da cancellare {@link BMCRegT22.SEASONS}
	 * @param day Giorno da cancellare {@link BMCRegT22.DAYS}
	 * @return true if send ok
	 */
	private boolean clearDay(int season, int day) {
		logger.info("Cancellazione setPoint "+getSeason(season)+" "+getDay(day));
		if (sendParameter(PARAM_CLEAR_DAY,((season & 0x01) << 3) + (day & 0x07))) {
			for (int hour = 0; hour < 24 ; hour++) {
				invalidate("setPoint-"+season+"-"+day+"-"+hour);
			}
			return true;
		} else {
			return false;
		}
	}

	private boolean clearDay(String value) {
		String[] a = value.split("-");
		if (a.length == 2) {
			int season = Integer.parseInt(a[0]);
			int day = Integer.parseInt(a[1]);
			return clearDay(season, day);
		} else {
			throw(new IllegalArgumentException("Format: <season>-<day>"));
		}
	}

	private boolean writeRTCC() {
		return writeRTCC("now");
	}
	
	private boolean writeRTCC(Object newValue) {
		if (String.class.isInstance(newValue)) {
			return writeRTCC((String)newValue);
		} else if (GregorianCalendar.class.isInstance(newValue)) {
			return writeRTCC((GregorianCalendar)newValue);
		} else {
			throw(new AISException("writeRTCC invalid type:"+newValue.getClass().getSimpleName()));
		}
	}
	
	private boolean writeRTCC(String newValue) {
		try {
			GregorianCalendar cal = new GregorianCalendar();
			Date date;
			DateFormat df = DateFormat.getDateTimeInstance();				
			if (((String)newValue).toLowerCase().equals("now")) {
				date = new Date();
				logger.debug("Set to now: "+date);
				cal.setTime(date);
			} else {
				date = df.parse((String) newValue);
				cal.setTime(date);
				logger.debug("Parsed data: "+newValue+" -> "+cal.getTime().toString());
			}
			return writeRTCC(cal);
		} catch (ParseException e) {
			logger.error("Data non valida: "+e);
			return false;
		}		
	}
	
	private boolean writeRTCC(GregorianCalendar cal) {
		EDSConnector conn = (EDSConnector) getConnector();
		int m = conn.getMyAddress();
		int d = getIntAddress();
		// aggiunge un secondo per ridurre lo sfalsamento
		cal.add(Calendar.SECOND, 1);
		boolean s1 = conn.sendMessage(new ImpostaRTCCMessage(d,m,0, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE)));
		boolean s2 = conn.sendMessage(new ImpostaRTCCMessage(d,m,1, cal.get(Calendar.MONTH)+1, cal.get(Calendar.YEAR)-2000));
		boolean s3 = conn.sendMessage(new ImpostaRTCCMessage(d,m,2, cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.SECOND)));
		getPort(port_RTCC).invalidate();
		return s1 && s2 && s3;		
	}

	/**
	 * Ritorna la stagione sotto forma di stringa.
	 * @param season Indice della stagione
	 * @return Nome della stagione
	 */
	private String getSeason(int season) {
		if ((season >= 0) && (season < SEASONS.length)) {
			return SEASONS[season];
		} else {
			throw(new AISException("Season is invalid: " + season));
		}
	}
	 
	/**
	* Ritorna l'indice relativo alla stagione
	* @param season Nome della stagione
	* @return Indice della stagione
	*/
	private int getSeason(String season) {
		for(int i=0; i < SEASONS.length; i++) {
			if (SEASONS[i].equalsIgnoreCase(season)) {
				return i;
			}
		}
		throw(new AISException("Season is invalid: " + season));
	}

	/**
	 * Ritorna il giorno sotto forma di stringa.
	 * @param day Indice del giorno
	 * @return Nome del giorno
	 */
	private String getDay(int day) {
		if ((day >= 0) && (day < DAYS.length)) {
			return DAYS[day];
		} else {
			throw(new AISException("Day is invalid: " + day));
		}
	}
	 
	/**
	* Ritorna l'indice relativo al giorno
	* @param season Nome del giorno
	* @return Indice del giorno
	*/
	private int getDay(String day) {
		for(int i=0; i < DAYS.length; i++) {
			if (DAYS[i].equalsIgnoreCase(day)) {
				return i;
			}
		}
		throw(new AISException("Day is invalid: " + day));
	}

	
	/**
	 * Ritorna lo stato della sonda sotto forma di stringa.
	 */
	 private String getMode(int mode) {
		// Sanity check
		if ((mode >= 0) && (mode < modeStrings.length)) {
			return modeStrings[mode];
		} else {
			throw(new AISException("Internal mode is invalid: " + mode));
		}
	}
	 
	/**
	* Ritorna il modo di funzionamento
	*/
	private int getMode(String mode) {
		// Sanity check
		for(int i=0; i < modeStrings.length; i++) {
			if (modeStrings[i].equalsIgnoreCase(mode)) {
				return i;
			}
		}
		throw(new AISException("Internal mode is invalid: " + mode));
	}
	
	 /**
	  * Invia un messaggio di impostazione parametro ed aggiorna il valore della porta se la comunicazione va a buon fine  
	  * @param portId Porta nella quale memorizzare il parametro
	  * @param param Indice del parametro
	  * @param newValue Nuovo valore
	  */
	private void sendSetpointParameter(String portId,int param,Double newValue) {
		if (sendParameter(param,setPoint(newValue.doubleValue()))) {
			DevicePort p = getPort(portId);
			p.setValue(newValue);
		}
	}
	 
	/**
	 * Invia un messaggio di impostazione parametro 
	 * @param parameter Numero del parametro
	 * @param value Valore del parametro
	 * @return
	 */
	private boolean sendParameter(int parameter, int value) {
		ImpostaParametroMessage richiesta = new ImpostaParametroMessage(getIntAddress(), getBMCComputerAddress(), 
				parameter, value);
		return getConnector().sendMessage(richiesta);		 
	}
	
	/**
	 * Invia un messaggio di richiesta parametro ed aggiorna il valore della porta  
	 * @param portId Porta nella quale memorizzare il parametro
	 * @param param Indice del parametro
	 */
	public boolean readSetpointParameter(String portId,int param) {
		RichiestaParametroMessage richiesta = new RichiestaParametroMessage(getIntAddress(), getBMCComputerAddress(), 
				param);
		if (getConnector().sendMessage(richiesta)) {
			DevicePort p = getPort(portId);		
			p.setValue(new Double(setPoint(((RispostaParametroMessage) (richiesta.getResponse())).getValue())));
			return true;
		} else {
			return false;
		}
	}

	public boolean sendPortValue(String portId, Object newValue) throws AISException {
		boolean res = false;
		if (portId.equals(port_autoSendTime)) {
			res = getConnector().sendMessage(new ImpostaParametroMessage(getIntAddress(), getBMCComputerAddress(), 
					PARAM_TERM_AUTO_SEND_TIME,((Integer) newValue).intValue())); 
		} else if (portId.equals(port_alarmeTemp)) {
			res = getConnector().sendMessage(new ImpostaParametroMessage(getIntAddress(), getBMCComputerAddress(), 
					PARAM_TERM_ALARM_TEMPERATURE,((Integer) newValue).intValue()));
		} else if (portId.equals(port_RTCC)) {
			writeRTCC(newValue);
		} else if (portId.equals(port_season)) {
			res = getConnector().sendMessage(new ImpostaParametroMessage(getIntAddress(), getBMCComputerAddress(), 
					PARAM_SEASON,getSeason((String)newValue)));
			invalidate("setPoint");
		} else if (portId.equals(port_mode)) {
			res = getConnector().sendMessage(new ImpostaParametroMessage(getIntAddress(), getBMCComputerAddress(), 
					PARAM_MODE,getMode((String)newValue)));
			invalidate("setPoint");
		} else if (portId.equals(virtual_RESET_DAY)) {
			res = clearDay((String)newValue);
		} else if (portId.equals(virtual_RESET_SEASON)) {
			res = clearSeason((String)newValue);			
		} else if (portId.equals(port_setPoint)) {
			if (Integer.class.isInstance(newValue)) {
				newValue = new Double(((Integer)newValue).intValue());
			}
			res = getConnector().sendMessage(new ImpostaSetPointMessage(getIntAddress(), getBMCComputerAddress(),((Double)newValue).doubleValue(),0));					
		} else if (portId.startsWith("setPoint-")) {
			String[] temp = portId.split("-");
			int stagione = Integer.parseInt(temp[1]);
			int giorno = Integer.parseInt(temp[2]);
			int ora = Integer.parseInt(temp[3]);
			if (Integer.class.isInstance(newValue)) {
				newValue = new Double(((Integer)newValue).intValue());
			}
			//logger.info("write:"+stagione+","+giorno+","+ora+"="+newValue);
			res = getConnector().sendMessage(new ImpostaSetPointMessage(getIntAddress(), getBMCComputerAddress(),
					((Double)newValue).doubleValue(), stagione, giorno, ora));
		} else if (portId.equals(port_T0)) {
			sendSetpointParameter(portId,PARAM_T0_VALUE,(Double)newValue);
		} else if (portId.equals(port_T1_SUMMER)) {
			sendSetpointParameter(portId,PARAM_T1_SUMMER_VALUE,(Double)newValue);
		} else if (portId.equals(port_T1_WINTER)) {
			sendSetpointParameter(portId,PARAM_T1_WINTER_VALUE,(Double)newValue);
		} else if (portId.equals(port_T2_SUMMER)) {
			sendSetpointParameter(portId,PARAM_T2_SUMMER_VALUE,(Double)newValue);
		} else if (portId.equals(port_T2_WINTER)) {
			sendSetpointParameter(portId,PARAM_T2_WINTER_VALUE,(Double)newValue);
		} else if (portId.equals(port_T3_SUMMER)) {
			sendSetpointParameter(portId,PARAM_T3_SUMMER_VALUE,(Double)newValue);
		} else if (portId.equals(port_T3_WINTER)) {
			sendSetpointParameter(portId,PARAM_T3_WINTER_VALUE,(Double)newValue);
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
					conn.sendMessage(rich);
				}
			}		
		}
	}
	
	private boolean readSetPoint(String portId) {
		EDSConnector conn = (EDSConnector) getConnector();
		int m = conn.getMyAddress();
		int d = getIntAddress();
		String[] temp = portId.split("-");
		int stagione = Integer.parseInt(temp[1]);
		int giorno = Integer.parseInt(temp[2]);
		int ora = Integer.parseInt(temp[3]);
		RichiestaSetPointMessage rich = new RichiestaSetPointMessage(d,m,stagione,giorno,ora);
		return conn.sendMessage(rich);
	}	

}
