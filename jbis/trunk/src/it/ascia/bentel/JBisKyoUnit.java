/**
 * Copyright (C) 2008 ASCIA S.r.l.
 */
package it.ascia.bentel;

import org.apache.log4j.Logger;

/**
 * Interfaccia di comunicazione con la centralina Bentel Kyo8.
 * 
 * @author sergio, arrigo
 */
public class JBisKyoUnit implements  Runnable {
	/**
	 * Attesa tra due polling dello stato [msec].
	 */
	private static final int POLL_PERIOD = 3000;
	
	/**
	 * Array per tradurre i codici di errore numerici in stringhe.
	 * 
	 * <p>Questi errori sono rilevati dalla routine di comunicazione via
	 * seriale.</p>
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
		 "Sessione di programmazione gi� aperta", // 8
		 "Dati forniti per la scrittura non validi", // 9
		 "Errore chiusura sessione di programmazione", // 10
		 "Codice utente non abilitato"}; // 11
	
	/**
	 *  Numero porta seriale (1 - COM1 ... 4 - COM4).
	 */
	private byte seriale;
	
	/** 
	 * Numero di ritentativi in caso di errore di comunicazione (result = 3).
	 */
	private byte tentativi;
	
	/**
	 * True se dobbiamo smettere il polling.
	 */
	private boolean exiting;
	/**
	 * False se abbiamo chiamato close(), quindi non si deve piu' comunicare.
	 */
	private boolean dllReady;
	/**
	 * PIN da fornire alla centrale.
	 */
	private String PIN;
	/**
	 * Nome del connector (usato da AUI).
	 */
	private String name;	
	/**
	 * Il nostro logger.
	 */
	private Logger logger;
	/**
	 * Il nostro JBisKyoDevice.
	 */
	private JBisKyoDevice device;
	/**
	 * Apre la DLL.
	 * @return true se tutto e' andato bene.
	 */
	private native boolean openLibrary();
	
	/**
	 * Costruttore.
	 * 
	 * @param s Numero porta seriale (1 - COM1 ... 4 - COM4)
	 * @param t Numero di ritentativi in caso di errore di comunicazione (result = 3)
	 * @param p PIN di accesso alle funzioni con password
	 */
	public JBisKyoUnit(int s, int t, String p, String connectorName) 
	throws JBisException
	{
	    logger = Logger.getLogger(getClass());
	    logger.info("Bentel GW (C) Ascia S.r.l. 2007-2008");
		seriale = (byte)s;
		tentativi = (byte)t;
		PIN = p;
		name = connectorName;
        System.loadLibrary("JBisKyoUnit");
		if (!openLibrary()) {
			throw new JBisException("Impossibile aprire la DLL");
		}
		dllReady = true;
		device = new JBisKyoDevice(this);
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
	
	/**
	 * Invia un comando alla centralina.
	 * 
	 * @param command numero del comando.
	 * 
	 * @param data array contenente i parametri, conterra' i dati ritornati. Se
	 * null, non verra' passato nessun parametro.
	 * 
	 * @return l'array data popolata con i dati ritornati (o una nuova array se
	 * data == null)
	 * 
	 * @throws JBisException in caso di errore.
	 */
	protected byte[] sendCommand(int command, byte data[]) throws JBisException {
    	if (!dllReady) {
    		throw new JBisException("DLL Chiusa.");
    	}
		Integer[] res1 = new Integer[1];
		res1[0] = Integer.valueOf(command);
		// System.out.printf("Comando: 0x%h\r\n",res1);
    	if (data == null) data = new byte[1024];
    	byte result = PanelConnection(command, seriale, tentativi, PIN, data);
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
	
	

	/*
    static
    {
        System.loadLibrary("JBisKyoUnit");
    }
    */

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
     * 8: Sessione di programmazione gi� aperta<br>
     * 9: Dati forniti per la scrittura non validi<br>
     * 10: Errore chiusura sessione di programmazione<br>
     * 11: Codice utente non abilitato</p>
     * 
     * @param comando numero del comando da inviare
     * @param seriale numero della porta seriale
     * @param tentativi numero di tentativi da effettuare in caso di errore
     * @param PIN codice PIN
     * @param data dati da inviare, conterra' i dati ricevuti
     * @return vedi sopra.
     */
    private static synchronized native byte PanelConnection(int comando, 
    		byte seriale, byte tentativi, String PIN, byte[] data);

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
					device.updateStatus();
				} catch (JBisException e) {
					logger.warn("Errore durante il polling: " +	e.getMessage());
				}
			} // While !exiting
		} catch (InterruptedException e) {
			logger.warn("Polling interrotto.");
		}
	}

	public JBisKyoDevice getDevice() {
		return device;
	}

	/*
	public Device[] getDevices(String address) {
		Device retval[];
		if (address.equals("0") || address.equals("*")) {
			retval = new Device[1];
			retval[0] = device;
		} else {
			retval = new Device[0];
		}
		return retval;
	}
	*/

}
