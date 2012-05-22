/**
 * Copyright (C) 2008 ASCIA S.r.l.
 * $Id$
 */
package it.ascia.ais;

import java.util.Collection;
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
	private String address;

	/**
	 * Il bus a cui il dispositivo e' collegato.
	 */
	private ConnectorInterface connector;
	
	private String description;

	/**
     * Il nostro logger.
     */
    protected Logger logger;

	/**
	 * le porte del dispositivo.
	 * @TODO Wrap in Collections.synchronizedMap
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
		return new Address(connector,this,null);
	}
	
	/**
	 * Ritorna il Connector di questo device.
	 */
	public ConnectorInterface getConnector() {
		return connector;
	}

	/**
	 * Ritorna una descrizione del dispositivo.
	 */
	public String getInfo() {
		return getClass().getSimpleName() + " " + getAddress() + " " + getDescription();
	}

	/**
	 * Aggiunge una porta al device
	 * Also call DevicePort.setDevice(this)
	 * Call Controller.fireNewDevicePortEvent()
	 * @param port
	 */
	public void addPort(DevicePort port) {
		port.setDevice(this);
		ports.put(port.getPortId(),port);
		Controller.getController().fireNewDevicePortEvent(new NewDevicePortEvent(port));
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
	public Collection<DevicePort> getPorts() {
		return ports.values();
	}
	
	/**
	 * Imposta il nome descrittivo della porta del device
	 * 
	 * @param portId identificatore univoco della porta del device
	 * @param portName Nuovo nome per la porra
	 * @throws AISException Se la porta non esiste 
	 */
	public void setPortDescription(String portId, String portName) throws AISException {
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
     * Richiede l'aggiornamento del valore di una porta del device.
     * L'implementazione deve:
     * 1) creare il messaggio necessario per richiedere il valore della porta del device fisico
     * 2) inviare il messaggio con il metodo Connector.sendMessage()
     * 3) leggere il valore aggiornato ricevuto nella risposta
     * 4) eseguire setValue()
     * 
	 * @param portId Nome della porta del device
	 * @return True se l'aggiornamento e' andato a buon fine 
	 * @throws AISException
	 */
	public abstract boolean updatePort(String portId) throws AISException;
	
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
	protected Object getPortValue(String portId) throws AISException {
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
	protected Object getPortCachedValue(String portId) throws AISException {
		DevicePort p = getPort(portId);
		return p.getCachedValue();
	}

	/**
	 * Fornisce il timestamp corrispondente all'ultimo aggiornamento del valore della porta
	 * @param portId
	 * @return TimeStamp in mS
	 */
	protected long getPortTimestamp(String portId) {
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

	/**
	 * Used by Connector to set belonging connector
	 * @param connector
	 */
	public void setConnector(ConnectorInterface connector) {
		this.connector = connector;		
	}
	
	public String toString() {
		return getInfo();
	}
	
}
