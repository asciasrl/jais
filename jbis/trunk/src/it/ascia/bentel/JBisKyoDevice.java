/**
 * Copyright (C) 2008 ASCIA S.r.l.
 */
package it.ascia.bentel;

import java.util.LinkedList;

import org.apache.log4j.Logger;

import it.ascia.ais.AISException;
import it.ascia.ais.Connector;
import it.ascia.ais.Device;
import it.ascia.ais.DeviceEvent;
import it.ascia.ais.DeviceListener;

/**
 * Centralina Bentel KYO8.
 * 
 * <p>La decodifica dei valori ricevuti (allarmi, errori, ecc.) 
 * e' lasciata ai singoli metodi getter.</p>
 * 
 * @author arrigo
 */
public class JBisKyoDevice implements Device {
	/**
	 * Dimensione massima del registro interno alla centralina (n. di eventi).
	 */
	private static final int LOG_SIZE = 128;
	/**
	 * Dimensione masssima del nostro log di eventi.
	 */
	private static final int MAX_LOG_SIZE = 100;
	/**
	 * Massima eta' dei dati ricevuti dalla centrale, perche' si debbano
	 * considerare "vecchi" [msec].
	 * 
	 * @see #statusTime
	 */
	private static final long OLD_DATA_AGE = 10000; // 10 sec
	/**
	 * Codici di warning.
	 * 
	 * <p>Gli indici delle stringhe nell'array corrispondono al bit del byte
	 * che elenca i warning.</p>
	 */
	private static final String warningMessages[] = {
		"Mancanza rete", // 0
		"Scomparsa BPI", // 1
		"Warning Fusibile", // 2
		"Batteria bassa", // 3
		"", // 4: non contemplato dal manuale
		"Guasto linea telefonica", // 5
		"Codici Default"}; // 6
	/**
	 * Descrizioni degli altri sabotaggi.
	 * 
	 * <p>Gli indici delle stringhe nell'array corrispondono al bit del byte
	 * che elenca i warning - 4.</p>
	 * 
	 * @see #otherSabotages
	 */
	private static final String otherSabotagesMessages[] = {
		"Sabotaggio di zona", // 4
		"Chiave falsa", // 5
		"Sabotaggio BPI", // 6
		"Sabotaggio sistema"}; // 7
	/**
	 * Nomi delle porte che corrispondono agli allarmi di zona.
	 */
	private static final String zone_alarms_ports[] = {"zonealarm1", 
		"zonealarm2", "zonealarm3", "zonealarm4", "zonealarm5", "zonealarm6",
		"zonealarm7", "zonealarm8"};
	/**
	 * Nomi delle porte che corrispondono ai sabotaggi di zona.
	 */
	private static final String zone_sabotages_ports[] = {"zonesabotage1", 
		"zonesabotage2", "zonesabotage3", "zonesabotage4", "zonesabotage5", "zonesabotage6",
		"zonesabotage7", "zonesabotage8"};
	/**
	 * Nomi delle porte che corrispondono ai warning.
	 */
	private static final String warnings_ports[] = { "warningnopower",
		"warningnobpi", "warningnofuse", "warninglowbattery", "warningunknown",
		"warningtelephoneline", "warningdefaultcodes"};
	/**
	 * Nomi delle porte che corrispondono agli allarmi di area.
	 */
	private static final String area_alarms_ports[] = {"areaalarm1", 
		"areaalarm2", "areaalarm3", "areaalarm4", "areaalarm5", "areaalarm6",
		"areaalarm7", "areaalarm8"};
	/**
	 * Nomi delle porte che corrispondono ai sabotaggi ulteriori.
	 */
	private static final String other_sabotages_ports[] = { "unknownsabotage0",
		"unknownsabotage1", "unknownsabotage2", "unknownsabotage3",
		"zonesabotages", "sabotagefalsekey", "sabotagebpi", "sabotagesystem"};
	/**
	 * Numero di zone gestite dalla centralina.
	 */
	public static final int ZONES_NUM = 8;
	/**
	 * Numero di aree gestite dalla centralina.
	 */
	public static final int AREAS_NUM = 4;
	/**
	 * Istante nel quale abbiamo fatto l'ultima lettura.
	 * 
	 * @see #OLD_DATA_AGE
	 */
	private long statusTime;
	
	/**
	 * Allarmi di zona attivi.
	 * 
	 * <p>Questo byte contiene lo stato degli allarmi di zona, tale e quale 
	 * viene ricevuto dalla centralina.</p>
	 * 
	 * @see #updateStatus
	 */
	private byte zoneAlarms;
	
	/**
	 * Sabotaggi di zona rilevati.
	 * 
	 * <p>Questo byte contiene lo stato dei sabotaggi di zona, tale e quale 
	 * viene ricevuto dalla centralina.</p>
	 * 
	 * @see #updateStatus
	 */
	private byte zoneSabotages;
	
	/**
	 * Warning rilevati.
	 * 
	 * <p>Questo byte contiene lo stato dei warning, tale e quale
	 * viene ricevuto dalla centralina, ma con i bit non significativi posti a 
	 * 0.</p>
	 * 
	 * @see #updateStatus
	 */
	private byte warnings;
	
	/**
	 * Allarmi di area attivi.
	 * 
	 * <p>Questo byte contiene lo stato degli allarmi di area, tale e quale 
	 * viene ricevuto dalla centralina, ma con i bit non significativi posti a 
	 * 0.</p>
	 * 
	 * @see #updateStatus
	 */
	private byte areaAlarms;
	
	/**
	 * Altri sabotaggi rilevati.
	 * 
	 * <p>Questo byte contiene lo stato dei sabotaggi rilevati, tale e quale 
	 * viene ricevuto dalla centralina, ma con i bit non significativi posti a 
	 * 0.</p>
	 * 
	 * @see #updateStatus
	 */
	private byte otherSabotages;
	/**
	 * Registro degli eventi. I piu' nuovi sono all'inizio.
	 */
	private LinkedList eventLog;	

	/**
	 * Il nostro Connector.
	 */
	private JBisKyoUnit connector;
	/**
	 * Chi ascolta i nostri eventi.
	 */
	private DeviceListener listener;
	/**
	 * Il nostro logger.
	 */
	private Logger logger;
	
	public JBisKyoDevice(JBisKyoUnit connector) {
		logger = Logger.getLogger(getClass());
		eventLog = new LinkedList();
		this.connector = connector;
		listener = null;
		statusTime = 0;
	}
	
	/**
	 * Confronta i bit di stato attuali con quelli "vecchi" e genera gli eventi
	 * per quelli cambiati.
	 * 
	 * @param oldAlarms il valore precedente del byte che contiene uno stato.
	 * @param newAlarms il valore attuale del byte che contiene uno stato.
	 * @param portNames array con i nomi delle porte che corrispondono ai bit.
	 */
	private void alertListener(byte oldAlarms, byte newAlarms, 
			String portNames[]) {
		if (listener != null) {
			int i;
			for (i = 0; i < 8; i++) {
				int b = 1 << i;
				if ((oldAlarms & b) != (newAlarms & b)) {
					DeviceEvent event;
					String newValue;
					if ((newAlarms & b) != 0) {
						newValue = "ON";
					} else {
						newValue = "OFF";
					}
					event = new DeviceEvent(this, portNames[i], newValue);
					listener.statusChanged(event);
				}
			}
		}
	}
	
	/**
	 * Aggiorna lo stato della centralina.
	 * 
	 * <p>Dopo aver chiamato questo metodo, si possono ottenere informazioni
	 * aggiornate sullo stato (warning, allarmi, ecc.)</p>
	 *
	 * <p>Se lo stato cambia, vengono generati gli eventi corrispondenti.</p>
	 */
	public void updateStatus() throws JBisException {
		byte[] data = connector.sendCommand(0x304, null);
		byte oldValue;
		oldValue = zoneAlarms;
		zoneAlarms = data[0];
		if (oldValue != zoneAlarms) {
			alertListener(oldValue, zoneAlarms, zone_alarms_ports);
		}
		oldValue = zoneSabotages;
		zoneSabotages = data[1];
		if (oldValue != zoneSabotages) {
			alertListener(oldValue, zoneSabotages, zone_sabotages_ports);
		}
		oldValue = warnings;
		warnings = (byte)(data[2] & 0x7f); // Contano solo i bit 0-6
		if (oldValue != warnings) {
			alertListener(oldValue, warnings, warnings_ports);
		}
		oldValue = areaAlarms;
		areaAlarms = (byte)(data[3] & 0xf); // Contano solo i bit 0-3
		if (oldValue != areaAlarms) {
			alertListener(oldValue, areaAlarms, area_alarms_ports);
		}
		oldValue = otherSabotages;
		otherSabotages = (byte)(data[4] & 0xf0); // Contano solo i bit 4-7
		if (oldValue != otherSabotages) {
			alertListener(oldValue, otherSabotages, other_sabotages_ports);
		}
		statusTime = System.currentTimeMillis();
	}
	
	/**
	 * Verifica la presenza di allarmi (zone e/o aree).
	 * 
	 * @return true se c'e' almeno un allarme attivo di qualunque tipo.
	 */
	public boolean hasAlarms() {
		return (hasZoneAlarms() || hasAreaAlarms());
	}
	
	/**
	 * Verifica la presenza di allarmi di zona.
	 * @return true se c'e' almeno un allarme di zona attivo.
	 */
	public boolean hasZoneAlarms() {
		return (zoneAlarms != 0);		
	}
	
	/**
	 * Verifica la presenza di un allarme in una zona.
	 * 
	 * @param zone numero zona da verificare (1 -- 8).
	 * 
	 * @return true se la zona indicata e' in allarme.
	 */
	public boolean hasZoneAlarm(int zone) {
		int bit = zone - 1;
		if ((bit >= 0) && (bit <= 7)) {
			return ((zoneAlarms & (1 << bit)) != 0);
		} else {
			logger.error("Richiesto allarme per zona non valida: " + zone);
			return false;
		}
	}
	
	/**
	 * Verifica la presenza di sabotaggi (zona o altro).
	 * 
	 * @return true se sono stati rilevati sabotaggi.
	 */
	public boolean hasSabotages() {
		return (hasZoneSabotages() || hasOtherSabotages());
	}
	
	/**
	 * Verifica la presenza di sabotaggi di zona.
	 * @return true se c'e' almeno un sabotaggio di zona attivo.
	 */
	public boolean hasZoneSabotages() {
		return (zoneSabotages != 0);		
	}
	
	/**
	 * Verifica la presenza di un sabotaggio di una zona.
	 * 
	 * @param zone numero zona da verificare (1 -- ZONES_NUM).
	 * 
	 * @return true se nella zona indicata e' stato rilevato un sabotaggio.
	 */
	public boolean hasZoneSabotage(int zone) {
		int bit = zone - 1;
		if ((bit >= 0) && (bit <= 7)) {
			return ((zoneSabotages & (1 << bit)) != 0);
		} else {
			logger.error("Richiesto stato sabotaggio per zona non valida: " + 
					zone);
			return false;
		}
	}
	
	
	/**
	 * Verifica la presenza di warning.
	 * 
	 * @return true se c'e' almeno un warning attivo.
	 */
	public boolean hasWarnings() {
		return (warnings != 0);		
	}
	
	/**
	 * Ritorna i warning rilevati, sotto forma di lista di stringhe.
	 * 
	 * @return una lista di warning (stringhe)
	 */
	public LinkedList getWarnings() {
		LinkedList retval = new LinkedList();
		int i;
		for (i=0; i < warningMessages.length; i++) {
			if ((warnings & (1 << i)) != 0) {
				retval.add(warningMessages[i]);
			}
		}
		return retval;
	}
	
	/**
	 * Verifica la presenza di allarmi di area.
	 * @return true se c'e' almeno un allarme di zona attivo.
	 */
	public boolean hasAreaAlarms() {
		return (areaAlarms != 0);		
	}
	
	/**
	 * Invia un comando di reset allarmi.
	 */
	public void resetAlarm() throws JBisException {
		connector.sendCommand(0x383, null);
	}
	
	/**
	 * Verifica la presenza di un allarme in un'area.
	 * 
	 * @param area numero area da verificare (1 -- AREAS_NUM).
	 * 
	 * @return true se l'area indicata e' in allarme.
	 */
	public boolean hasAreaAlarm(int area) {
		int bit = area - 1;
		if ((bit >= 0) && (bit <= 3)) {
			return ((areaAlarms & (1 << bit)) != 0);
		} else {
			logger.error("Richiesto allarme per area non valida: " + area);
			return false;
		}
	}
	
	/**
	 * Verifica la presenza di sabotaggi ulteriori.
	 * 
	 * @return true se c'e' almeno un sabotaggio (non di zona) attivo.
	 */
	public boolean hasOtherSabotages() {
		return (otherSabotages != 0);		
	}
	
	/**
	 * Ritorna i sabotaggi ulteriori rilevati, sotto forma di lista di stringhe.
	 * 
	 * @return una lista di sabotaggi (stringhe)
	 */
	public LinkedList getOtherSabotages() {
		LinkedList retval = new LinkedList();
		int i;
		for (i=0; i < otherSabotagesMessages.length; i++) {
			// I bit iniziano dal n. 4
			if ((otherSabotages & (1 << (i + 4))) != 0) {
				retval.add(otherSabotagesMessages[i]);
			}
		}
		return retval;
	}
	
	/**
	 * Aggiorna il log degli eventi.
	 * 
	 * <p>Richiede il registro degli ultimi eventi alla centralina e (ri)crea
	 * il registro interno di questa classe.</p>
	 * 
	 * @throws JBisException in caso di errore di accesso alla centralina.
	 */
	public void updateLog() throws JBisException {
		logger.debug("Lettura log centralina...");
		byte[] data = connector.sendCommand(0x30d, null);
		int eventsNumber, firstEvent, event, eventAddress;
		// data[0] se 0 indica che il buffer ha circolato.
		// data[1] e' il numero della prossima entry che sara' inserita.
		// Sembra che data[1] possa anche essere maggiore di LOG_SIZE, il
		// che non dovrebbe accadere.
		if (data[0] == 0) { // Il buffer circolare ha circolato
			logger.trace("Il buffer ha circolato!");
			eventsNumber = LOG_SIZE;
			firstEvent = data[1] & (LOG_SIZE - 1);
		} else {
			eventsNumber = data[1] & (LOG_SIZE - 1);
			firstEvent = 0;
		}
		eventLog.clear();
		for (event = 0; event < eventsNumber; event++) {
			// L'indirizzo deve circolare con il buffer
			if ((event + firstEvent) < LOG_SIZE) {
				eventAddress = 2 + (event + firstEvent) * 7;
			} else {
				eventAddress = 2 + (event + firstEvent - LOG_SIZE) * 7;
				// System.out.println("Rolling");
			}
			Event ev = new Event(data, eventAddress);
			logger.debug("Evento: " + ev.getInfo());
			eventLog.addFirst(ev);
		} // Cicla su tutti gli eventi
		// Eliminazione dei messaggi in eccesso
		while (eventLog.size() > MAX_LOG_SIZE) {
			eventLog.removeLast();
		}
	}
	
	/**
	 * Attiva o disattiva un'uscita.
	 * 
	 * @param port numero dell'uscita
	 * @param value true per attivare, false per disattivare
	 * 
	 * @throws JBisException in caso di errore
	 */
	public void setOutput(int port, boolean value) throws JBisException {
		int command;
		byte data[] = new byte[1];
		data[0] = (byte)(port & 0xff);
		if (value) {
			command = 0x387;
		} else {
			command = 0x388;
		}
		connector.sendCommand(command, data);
	}
	
	
	/* (non-Javadoc)
	 * @see it.ascia.ais.Device#getAddress()
	 */
	public String getAddress() {
		// Su ogni JBisKyoUnit ci siamo solo noi.
		return "0";
	}

	/* (non-Javadoc)
	 * @see it.ascia.ais.Device#getConnector()
	 */
	public Connector getConnector() {
		return connector;
	}

	/**
	 * Ritorna il valore di una o piu' porte, basandosi sui bit di stato.
	 * 
	 * @param portName nome della porta richiesta o "*"
	 * @param status il valore precedente del byte che contiene uno stato.
	 * @param portNames array con i nomi delle porte che corrispondono ai bit.
	 * 
	 * @return una lista delle porte in formato buono per AUI.
	 */
	private String getPort(String portName, byte status, String portNames[]) {
		String retval = "";
		String compactName = connector.getName() + "." + getAddress();
		int i;
		for (i = 0; i < portNames.length; i++) {
			String thisPort = portNames[i];
			if (portName.equals("*") || portName.equals(thisPort)) {
				retval += compactName + ":" + thisPort + "=";
				if ((status & (1 << i)) != 0) {
					retval += "ON";
				} else {
					retval += "OFF";
				}
				retval += "\n";
			} // Se la porta e' questa
		} // Cicla sui bit di stato.
		return retval;
	}
	/* (non-Javadoc)
	 * @see it.ascia.ais.Device#getStatus(java.lang.String)
	 */
	public String getStatus(String port) {
		String retval;
		if ((System.currentTimeMillis() - statusTime) < OLD_DATA_AGE) {
			retval = getPort(port, zoneAlarms, zone_alarms_ports);
			retval += getPort(port, zoneSabotages, zone_sabotages_ports);
			retval += getPort(port, areaAlarms, area_alarms_ports);
			retval += getPort(port, warnings, warnings_ports);
			retval += getPort(port, otherSabotages, other_sabotages_ports);
		} else {
			// I dati sono troppo vecchi
			retval = "ERROR: data is too old!";
		}
		return retval;
	}

	/* (non-Javadoc)
	 * @see it.ascia.ais.Device#setDeviceListener(it.ascia.ais.DeviceListener)
	 */
	public void setDeviceListener(DeviceListener listener) {
		this.listener = listener;
	}

	/* (non-Javadoc)
	 * @see it.ascia.ais.Device#setPort(java.lang.String, java.lang.String)
	 */
	public void setPort(String port, String value) throws AISException {
		throw new AISException("Unsupported.");
	}

}
