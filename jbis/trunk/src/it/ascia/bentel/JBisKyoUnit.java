/**
 * Copyright (C) 2008 ASCIA S.r.l.
 */
package it.ascia.bentel;

import java.util.LinkedList;
import org.apache.log4j.Logger;

import it.ascia.ais.AlarmReceiver;

/**
 * Interfaccia di comunicazione con la centralina Bentel Kyo8.
 * 
 * <p>La decodifica dei valori ricevuti (allarmi, errori, ecc.) 
 * e' lasciata ai singoli metodi getter.</p>
 * 
 * @author sergio, arrigo
 */
public class JBisKyoUnit implements Runnable {
	/**
	 * Dimensione massima del registro interno alla centralina (n. di eventi).
	 */
	private static final int LOG_SIZE = 128;
	
	/**
	 * Dimensione masssima del nostro log di eventi.
	 */
	private static final int MAX_LOG_SIZE = 100;
	
	/**
	 * Attesa tra due polling dello stato [msec].
	 */
	private static final int POLL_PERIOD = 3000;
	
	/**
	 * Array per tradurre i codici di errore numerici in stringhe.
	 */
	 private static final String errorMessages[] = {
		 "Operazione andata a buon fine", // 0
		 "Codice utente non valido", // 1 
		 "Errore apertura porta seriale", // 2
		 "Errore di comunicazione", // 3
		 "Comando sconosciuto", // 4
		 "Tipo centrale non riconosciuto", // 5
		 "Versione firmware non riconosciuto", // 6
		 "Aree inserite, impossibile aprire sessione di programmazione", // 7
		 "Sessione di programmazione già aperta", // 8
		 "Dati forniti per la scrittura non validi", // 9
		 "Errore chiusura sessione di programmazione", // 10
		 "Codice utente non abilitato"}; // 11
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
	 * @see otherSabotages
	 */
	private static final String otherSabotagesMessages[] = {
		"Sabotaggio di zona", // 4
		"Chiave falsa", // 5
		"Sabotaggio BPI", // 6
		"Sabotaggio sistema"}; // 7
	
	/**
	 * Numero di zone gestite dalla centralina.
	 */
	public final int ZONES_NUM = 8;
	
	/**
	 * Numero di aree gestite dalla centralina.
	 */
	public final int AREAS_NUM = 4;
	
	/**
	 *  Numero porta seriale (1 - COM1 ... 4 - COM4).
	 */
	private byte seriale;
	
	/** 
	 * Numero di ritentativi in caso di errore di comunicazione (result = 3).
	 */
	private byte tentativi;
	
	/**
	 * Allarmi di zona attivi.
	 * 
	 * <p>Questo byte contiene lo stato degli allarmi di zona, tale e quale 
	 * viene ricevuto dalla centralina.</p>
	 * 
	 * @see updateStatus
	 */
	private byte zoneAlarms;
	
	/**
	 * Sabotaggi di zona rilevati.
	 * 
	 * <p>Questo byte contiene lo stato dei sabotaggi di zona, tale e quale 
	 * viene ricevuto dalla centralina.</p>
	 * 
	 * @see updateStatus
	 */
	private byte zoneSabotages;
	
	/**
	 * Warning rilevati.
	 * 
	 * <p>Questo byte contiene lo stato dei warning, tale e quale
	 * viene ricevuto dalla centralina, ma con i bit non significativi posti a 
	 * 0.</p>
	 * 
	 * @see updateStatus
	 */
	private byte warnings;
	
	/**
	 * Allarmi di area attivi.
	 * 
	 * <p>Questo byte contiene lo stato degli allarmi di area, tale e quale 
	 * viene ricevuto dalla centralina, ma con i bit non significativi posti a 
	 * 0.</p>
	 * 
	 * @see updateStatus
	 */
	private byte areaAlarms;
	
	/**
	 * Altri sabotaggi rilevati.
	 * 
	 * <p>Questo byte contiene lo stato dei sabotaggi rilevati, tale e quale 
	 * viene ricevuto dalla centralina, ma con i bit non significativi posti a 
	 * 0.</p>
	 * 
	 * @see updateStatus
	 */
	private byte otherSabotages;
	
	/**
	 * True se dobbiamo smettere il polling.
	 */
	private boolean exiting;
	/**
	 * False se abbiamo chiamato close(), quindi non si deve piu' comunicare.
	 */
	private boolean dllReady;
	
	/**
	 * Registro degli eventi. I piu' nuovi sono all'inizio.
	 */
	private LinkedList eventLog;	
	
	private String PIN;
	
	/**
	 * Oggetto che ricevera' gli allarmi.
	 */
	private AlarmReceiver alarmReceiver;
	
	/**
	 * Il nostro logger.
	 */
	private Logger logger;
	
	/**
	 * Apre la DLL.
	 * @return true se tutto e' andato bene.
	 */
	private native boolean openLibrary();
	
	/**
	 * 
	 * @param s Numero porta seriale (1 - COM1 ... 4 - COM4)
	 * @param t Numero di ritentativi in caso di errore di comunicazione (result = 3)
	 * @param p PIN di accesso alle funzioni con password
	 */
	JBisKyoUnit(int s, int t, String p, AlarmReceiver aR) throws JBisException
	{
	    logger = Logger.getLogger(getClass());
	    logger.info("Bentel GW (C) Ascia S.r.l. 2007-2008");
		seriale = (byte)s;
		tentativi = (byte)t;
		PIN = p;
		eventLog = new LinkedList();
		alarmReceiver = aR;
		if (!openLibrary()) {
			throw new JBisException("Impossibile aprire la DLL");
		}
		dllReady = true;
	}
	
	/**
	 * Aggiorna lo stato della centralina.
	 * 
	 * <p>Dopo aver chiamato questo metodo, si possono ottenere informazioni
	 * aggiornate sullo stato (warning, allarmi, ecc.)</p>
	 */
	public void updateStatus() throws JBisException {
		byte[] data = Leggi(0x304);
		zoneAlarms = data[0];
		zoneSabotages = data[1];
		warnings = (byte)(data[2] & 0x7f); // Contano solo i bit 0-6
		areaAlarms = (byte)(data[3] & 0xf); // Contano solo i bit 0-3
		otherSabotages = (byte)(data[4] & 0xf0); // Contano solo i bit 4-7
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
		byte[] data = Leggi(0x30d);
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
	 * Converte un codice di errore numerico in un messaggio testuale.
	 * 
	 * @param err codice di errore.
	 * @return una stringa che descrive l'errore.
	 */
	protected static String strerror(int err) {
		if ((err > 0) && (err < errorMessages.length)) {
			return errorMessages[err];
		} else {
			return "Errore sconosciuto.";
		}
	}
	
	byte[] Leggi(int comando) throws JBisException {
    	if (!dllReady) {
    		throw new JBisException("DLL Chiusa.");
    	}
		Integer[] res1 = new Integer[1];
		res1[0] = Integer.valueOf(comando);
		// System.out.printf("Comando: 0x%h\r\n",res1);
    	byte[] data;
    	data = new byte[1024];
    	byte result = PanelConnection(comando, seriale, tentativi, PIN, data);
    	// System.out.println("result: " + result);
    	if (result != 0) {
    		Byte[] res = new Byte[1];
    		res[0] = Byte.valueOf(result);
    		throw new JBisException("Errore " + result + "(" + strerror(result) +
    			")");
    		//data = new byte[0];
    	}
    	return data;
    }

    static
    {
        System.loadLibrary("JBisKyoUnit");
    }

    /**
     * Comunicazione con la centralina attraverso la seriale.
     * 
     * <p>0: Operazione andata a buon fine<br>
     * 1: Codice utente non valido<br>
     * 2: Errore apertura porta seriale<br>
     * 3: Errore di comunicazione<br>
     * 4: Comando sconosciuto<br>
     * 5: Tipo centrale non riconosciuto<br>
     * 6: Versione firmware non riconosciuto<br>
     * 7: Aree inserite, impossibile aprire sessione di programmazione<br>
     * 8: Sessione di programmazione già aperta<br>
     * 9: Dati forniti per la scrittura non validi<br>
     * 10: Errore chiusura sessione di programmazione<br>
     * 11: Codice utente non abilitato</p>
     * 
     * @param comando numero del comando da inviare
     * @param seriale numero della porta seriale
     * @param tentativi numero di tentativi da effettuare in caso di errore
     * @param PIN codice PIN
     * @param PINLength lunghezza della stringa che contiene il PIN
     * @param data dati da inviare
     * @return vedi sopra.
     */
    private static synchronized native byte PanelConnection(int comando, byte seriale, byte tentativi, String PIN, byte[] data);

    /**
	 * Chiude la DLL.
	 */
	private native void close();
	
	/**
	 * Inizia il polling periodico.
	 */
	public void start() {
		new Thread(this).start();
	}

	/**
	 * Ferma il polling periodico e chiude la libreria.
	 * 
	 * <p>Questa funzione deve essere chiamata prima della chiusura del programma.</p>
	 */
	public void stop() {
		exiting = true;
		dllReady = false;
		close();
	}
	
	/**
	 * Esegue regolarmente il polling dello stato della centrale e avvisa
	 * l'AlarmReceiver in caso di allarmi.
	 */
	public void run() {
		exiting = false;
		try {
			while (!exiting) {
				Thread.sleep(POLL_PERIOD);
				try {
					updateStatus();
					if (hasAlarms()) {
						if (hasZoneAlarms()) {
							for (int i = 1; i <= ZONES_NUM; i++) {
								if (hasZoneAlarm(i)) {
									alarmReceiver.alarmReceived("Zona  " + i);
								}
							}
						}
						if (hasAreaAlarms()) {
							for (int i = 1; i <= AREAS_NUM; i++) {
								if (hasAreaAlarm(i)) {
									alarmReceiver.alarmReceived("Area  " + i);
								}
							}
						}
					} // Allarmi
				} catch (JBisException e) {
					logger.warn("Errore durante il polling: " +	e.getMessage());
				}
			} // While !exiting
		} catch (InterruptedException e) {
			logger.warn("Polling interrotto.");
		}
	}
}
