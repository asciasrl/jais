/**
 * Copyright (C) 2008 ASCIA S.r.l.
 */
package it.ascia.ais;

import java.util.Iterator;
import java.util.LinkedHashMap;
import org.apache.log4j.Logger;

/**
 * Device generico connesso a un Connector generico.
 * 
 * @author arrigo, sergio
 */
public abstract class Device {
	/**
	 * L'indirizzo sul connector.
	 */
	protected String address;

	/**
	 * Il bus a cui il dispositivo e' collegato.
	 */
	private Connector connector;

	/**
     * Il nostro logger.
     */
    protected Logger logger;

	/**
	 * le porte del dispositivo.
	 */
	protected LinkedHashMap ports = new LinkedHashMap();

	/**
	 * How much (mS) wait for each unaswered message
	 */
	private static final long UNREACHABLE_PAUSE_1 = 1000;

	/**
	 * Max time to pause polling/sending to unreacheable device 
	 */
	private static final long UNREACHABLE_PAUSE_MAX = 60000;

	/**
	 * When device not answered last time
	 */
	private long lastReachError = 0;

	/**
	 * How many consecutives times device not aswered
	 */
	private int reachErrors = 0;
	
	/**
	 * Enable retry 
	 */
	private boolean reachRetry = false;
	
	/**
	 * Device con indirizzo vuoto
	 * @param connector
	 * @throws AISException
	 */
	public Device(Connector connector) throws AISException {
		this(connector,"");
	}

	/**
	 * Device con indirizzo specificato
	 * Il device si aggiunge al connettore con il metodo {@link Connector.addDevice(Device)}
	 * @param connector Connettore al quale il device si deve aggiungere
	 * @param address Indirizzo del Device
	 * @throws AISException
	 */
	public Device(Connector connector, String address) throws AISException {
		this.connector = connector;
		this.address = address;
		connector.addDevice(this);
		logger = Logger.getLogger(getClass());
	}
	
	/**
	 * Ritorna l'indirizzo del Device, senza considerare il connettore.
	 * La sintassi dell'indirizzo dipende dal tipo di connettore, cioè della tecnologia
	 * di trasmissione utilizzata. Ad esempio:
	 * - KNX: 1.3.5
	 * - EDS: 12
	 * Pue' essere anche una stringa vuota
	 */
	public String getAddress() {
		return address;
	}
	
	/**
	 * Ritorna l'indirizzo del Device completo del nome del connettore 
	 */
	public String getFullAddress() {
		return connector.getName() + "." + address;
	}
	
	/**
	 * Ritorna il Connector di questo device.
	 */
	public Connector getConnector() {
		return connector;
	}

	/**
	 * Ritorna una descrizione del dispositivo.
	 */
	public abstract String getInfo();

	/**
	 * Ritorna lo stato del device cambiato rispetto a un certo istante,
	 * in formato utile per AUI.
	 * 
	 * <p>Lo stato deve essere aggiornato.</p>
	 * 
	 * @return lo stato del device che e' cambiato al timestamp specificato, 
	 * oppure piu' tardi.
	 * 
	 * @param portId il nome della porta da restituire, o "*" per indicarle
	 * tutte.
	 * 
	 * @param timestamp il timestamp che "screma" i cambiamenti dello stato
	 * che ci interessano, nella forma ritornata da System.currentTimeMillis().
	 * Se posto a 0, richiede l'intero stato del sistema.
	 * @throws AISException 
	 */
	public String getStatus(String portId, long timestamp) throws AISException {
		if (portId.equals("*")) {
			return getStatus(timestamp);
		} else {
			DevicePort p = getPort(portId);
			return p.getStatus();
		}
	}

	/**
	 * Restituisce lo stato di tutte le porte del Device che sono state aggiornate dopo il timestamp
	 * @param timestamp
	 * @return Righe di testo nel formato: porta=valore
	 * @throws AISException
	 */
	public String getStatus(long timestamp) throws AISException {
		StringBuffer s = new StringBuffer();
		for (Iterator i = ports.values().iterator(); i.hasNext(); ) {
			DevicePort p = (DevicePort) i.next();				
			s.append(p.getStatus()+"\n");
		}
		return s.toString();
	}

	/**
	 * Restituisce lo stato di tutte le porte del Device
	 * @return Righe di testo nel formato: porta=valore
	 * @throws AISException
	 */
	public String getStatus() throws AISException {
		return getStatus(0);
	}

	/**
	 * Aggiunge una porta al device
	 * @param portId
	 * @param portName
	 */
	public void addPort(String portId, String portName) {
		ports.put(portId, new DevicePort(this, portId, portName));		
	}

	/**
	 * Aggiunge una porta con nome null
	 * @param portId
	 */
	public void addPort(String portId) {
		addPort(portId, null);		
	}
	
	/**
	 * Aggiunge una porta al device
	 * @param port
	 */
	public void addPort(DevicePort port) {
		ports.put(port.getPortId(),port);
	}

	public boolean havePort(String portId) {
		return ports.containsKey(portId);
	}
	
	/**
	 * 
	 * @param portId Identificativo della porta
	 * @return La instanza di DevicePort
	 * @throws AISException Se la porta non esite
	 */
	public DevicePort getPort(String portId) throws AISException {
		if (ports.containsKey(portId)) {
			return (DevicePort) ports.get(portId); 
		} else {
			throw(new AISException("Il device "+getFullAddress()+" non ha la porta "+portId));
		}
	}
	
	/**
	 * 
	 * @return Tutte le DevicePort del Device
	 */
	public DevicePort[] getPorts() {
		return (DevicePort[]) ports.values().toArray(new DevicePort[ports.size()]);
	}
	
	/**
	 * Fornisce il nome descrittivo della porta del device
	 * 
	 * @param portId identificatore univoco della porta del device
	 * @return null Se la porta non esiste
	 * @throws AISException 
	 */
	public String getPortName(String portId) throws AISException {
		DevicePort p = getPort(portId);
		return p.getName();
	}
	
	/**
	 * Imposta il nome descrittivo della porta del device
	 * 
	 * @param portId identificatore univoco della porta del device
	 * @param portName Nuovo nome per la porra
	 * @throws AISException Se la porta non esiste 
	 */
	public void setPortName(String portId, String portName) throws AISException {
		DevicePort p = getPort(portId);
		p.setName(portName);
	}
	
	/**
	 * Utility function to invalidate a port
	 * @param portId Nome della porta
	 * @throws AISException
	 */
	protected void invalidate(String portId) throws AISException {
		DevicePort p = getPort(portId);
		p.invalidate();
	}
    
    /**
     * Invia il nuovo valore sulla porta del dispositivo fisico.
     * Il valore memorizzato localmente viene aggiornato tramite una richiesta updatePort e conseguente setPortValue 
     * @see DevicePort.writeValue
     * @see updatePortValue
     * @see setPortValue
     * @param portId
     * @param newValue
     * @return true se operazione andata a buon fine
     */
	public abstract boolean sendPortValue(String portId, Object newValue) throws AISException;

    /**
     * Scrive un nuovo valore sulla porta del dispositivo fisico.
     * Richiama DevicePort.writeValue che in seguito chiama Device.sendPortValue
     * @see DevicePort.writeValue
     * @param portId
     * @param newValue
     * @return true se operazione andata a buon fine
     */
	public boolean writePortValue(String portId, Object newValue) {
		DevicePort p = getPort(portId);
		return p.writeValue(newValue);
	}

	/**
     * Richiede l'aggiornamento del valore di una porta 
     * <p>Se ritorna con tempo da attesa = 0 (zero) vuol dire che il chiamante non deve aspettare, perche' l'aggiornamento:
     * <ul>
     * <li>e' stato immediato</li>
     * <li>e' stato gia' fatto</li>
     * <li>non puo' essere fatto</li>
     * </ul>
     * altrimenti il chiamante deve attendere il tempo previsto prima di leggere il valore.
     * 
     * </p>
	 * @param portId
	 * @return Tempo massimo previsto per l'aggiornamento in millisecondi
	 * @throws AISException
	 */
	public abstract long updatePort(String portId) throws AISException;
	
	/**
	 * Aggiorna il valore della porta memorizzato localmente.
	 * Per scrivere un nuovo valore sulla porta del dispositivo fisico, usare {@see #writePortValue}
	 * Se il valore della porta è modificato genera un DevicePortChangeEvent
	 * @param portId
	 * @param newValue
	 * @throws AISException
	 */
	protected void setPortValue(String portId,Object newValue) throws AISException {
		DevicePort p = getPort(portId);
		p.setValue(newValue);
	}
	
	/**
	 * Legge il valore di una porta di un dispositivo reale
	 * Gestisce la cache ed effettua l'aggiornamento se necessario 
	 * 
	 * @param portId il nome della porta
	 * 
	 * @throws un'eccezione se qualcosa va male.
	 */	
	public Object getPortValue(String portId) throws AISException {
		DevicePort p = getPort(portId);
		return p.getValue();
	}

	/**
	 * Legge il valore in cache di una porta di un dispositivo reale
	 * 
	 * @param portId il nome della porta
	 * 
	 * @throws un'eccezione se qualcosa va male.
	 */	
	public Object getPortCachedValue(String portId) throws AISException {
		DevicePort p = getPort(portId);
		return p.getCachedValue();
	}

	/**
	 * Fornisce il timestamp corrispondente all'ultimo aggiornamento del valore della porta
	 * @param portId
	 * @return TimeStamp in mS
	 */
	public long getPortTimestamp(String portId) {
		DevicePort p = (DevicePort) ports.get(portId);			
		return p.getTimeStamp();
	}

	/**
	 * Trasmette l'evento al Connector del Device
	 * @param evt Evento da gestire
	 */
	public void fireDevicePortChangeEvent(DevicePortChangeEvent evt) {
		connector.fireDevicePortChangeEvent( evt );
	}

	/**
	 * Determine if device is not reachable.  
	 * Device implementation should recover automatically after a while, letting connector retry.    
	 * @return true if device cannot be reach
	 */
	public boolean isUnreachable() {
		if (reachRetry) {
			// riprova finche' non si presenta un altro errore
			return false;
		}
		if (reachErrors >= 1) {
			// dopo un po riprova
			if ((lastReachError + Math.min(reachErrors * UNREACHABLE_PAUSE_1, UNREACHABLE_PAUSE_MAX)) < System.currentTimeMillis()) {
				lastReachError = System.currentTimeMillis();
				reachRetry = true;
				return false;
			} else {
				return true;
			}			
		} else {
			return false;
		}
	}
	
	/**
	 * Called from Connector when detect that the device is not reacheable
	 * Increments error counter and record last error timestamp.
	 */
	public void setUnreachable() {
		lastReachError = System.currentTimeMillis();
		reachRetry = false;
		reachErrors++;
		logger.warn("Unreachable ("+reachErrors+"): "+getFullAddress());		
	}

	/**
	 * Called from Connector when detect that the device is reacheable.
	 * Reset errors counter
	 */
	public void setReachable() {
		lastReachError = 0;
		reachErrors = 0;
	}


	
}
