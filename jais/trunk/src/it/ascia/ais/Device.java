/**
 * Copyright (C) 2008 ASCIA S.r.l.
 */
package it.ascia.ais;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

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
	private String address;

	/**
	 * Il bus a cui il dispositivo e' collegato.
	 */
	private Connector connector;
	
	private String description;

	/**
     * Il nostro logger.
     */
    protected Logger logger;

	/**
	 * le porte del dispositivo.
	 */
	private LinkedHashMap<String, DevicePort> ports = new LinkedHashMap<String, DevicePort>();

	/**
	 * How much (mS) wait for each unanswered message
	 */
	private static final long UNREACHABLE_PAUSE_1 = 1000;

	/**
	 * Max time to pause polling/sending to unreachable device 
	 */
	private static final long UNREACHABLE_PAUSE_MAX = 60000;

	/**
	 * When device not answered last time
	 */
	private long lastReachError = 0;

	/**
	 * How many consecutives times device not answered
	 */
	private int reachErrors = 0;
	
	/**
	 * Enable retry 
	 */
	private boolean reachRetry = false;
	
	/**
	 * Device con indirizzo specificato
	 * Il device si aggiunge al connettore con il metodo {@link Connector.addDevice(Device)}
	 * @param address Indirizzo del Device
	 * @throws AISException
	 */
	public Device(String address) throws AISException {
		this.address = address;
		logger = Logger.getLogger(getClass());
	}

	/**
	 * Ritorna l'indirizzo del Device, senza considerare il connettore.
	 * La sintassi dell'indirizzo dipende dal tipo di connettore, cioe' della tecnologia
	 * di trasmissione utilizzata. Ad esempio:
	 * - KNX: 1.3.5
	 * - EDS: 12
	 * Can be an empty String
	 */
	public String getSimpleAddress() {
		return address;
	}
	
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Ritorna l'indirizzo del Device completo del nome del connettore
	 * @deprecated Use getAddress
	 */
	public String getFullAddress() {
		return connector.getName() + "." + address;
	}
	
	/**
	 * 
	 * @return Device address (port part of address is undefined)
	 */
	public Address getAddress() {
		return new Address(connector.getName(),address,null);
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
	public String getInfo() {
		return getAddress() + " " + getClass().getSimpleName() + " " + getDescription();
	}

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
	public Map getStatus(String portId, long timestamp) throws AISException {
		if (portId.equals("*")) {
			return getStatus(timestamp);
		} else {
			DevicePort p = getPort(portId);
			Map m = new LinkedHashMap();
			m.put(p.getPortId(),p.getStatus());
			return m;
		}
	}

	/**
	 * Restituisce lo stato di tutte le porte del Device che sono state aggiornate dopo il timestamp
	 * @param timestamp
	 * @return Una Array con un elemento per porta 
	 * @throws AISException
	 */
	public Map getStatus(long timestamp) throws AISException {
		Map m = new LinkedHashMap();
		for (Iterator i = ports.values().iterator(); i.hasNext(); ) {
			DevicePort p = (DevicePort) i.next();				
			m.put(p.getPortId(),p.getStatus());
		}
		return m;
	}

	/**
	 * Restituisce lo stato di tutte le porte del Device
	 * @return Righe di testo nel formato: porta=valore
	 * @throws AISException
	 */
	public Map getStatus() throws AISException {
		return getStatus(0);
	}

	/**
	 * Aggiunge una porta al device
	 * @param port
	 */
	public void addPort(DevicePort port) {
		port.setDevice(this);
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
	 * Imposta il nome descrittivo della porta del device
	 * 
	 * @param portId identificatore univoco della porta del device
	 * @param portName Nuovo nome per la porra
	 * @throws AISException Se la porta non esiste 
	 */
	public void setPortName(String portId, String portName) throws AISException {
		DevicePort p = getPort(portId);
		p.setDescription(portName);
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
     * @TODO: updatePort e' sincrona, quindi non ha piu' senso ritornare il tempo di attesa
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
	 * Se il valore della porta e' modificato genera un DevicePortChangeEvent
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

	public void setConnector(Connector connector) {
		this.connector = connector;		
	}


	
}
