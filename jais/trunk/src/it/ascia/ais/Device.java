/**
 * Copyright (C) 2008 ASCIA S.r.l.
 */
package it.ascia.ais;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

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

	/**
     * Il nostro logger.
     */
    protected Logger logger;

	/**
	 * le porte del dispositivo.
	 */
	private HashMap ports = new HashMap();

	public Device(Connector connector) throws AISException {
		this(connector,"");
	}

	public Device(Connector connector, String address) throws AISException {
		this.connector = connector;
		this.address = address;
		connector.addDevice(this);
		logger = Logger.getLogger(getClass());
	}
	
	// TODO Rendere parametrizzable da config cacheRetention 
	private long cacheRetention = 60000; // millisecondi
	
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
	 * @param port il nome della porta da restituire, o "*" per indicarle
	 * tutte.
	 * 
	 * @param timeStamp il timestamp che "screma" i cambiamenti dello stato
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
	 * @return
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
	 * @return
	 * @throws AISException
	 */
	public String getStatus() throws AISException {
		return getStatus(0);
	}

	/**
	 * 
	 * @param portId
	 * @param portName
	 */
	protected void addPort(String portId, String portName) {
		ports.put(portId, new DevicePort(this, portId, portName));		
	}

	/**
	 * @param portId
	 */
	protected void addPort(String portId) {
		addPort(portId, null);		
	}
	
	/**
	 * 
	 * @param portId
	 * @return 
	 * @throws AISException Se la porta non esite
	 */
	public DevicePort getPort(String portId) throws AISException {
		if (ports.containsKey(portId)) {
			return (DevicePort) ports.get(portId); 
		} else {
			throw(new AISException("Il device "+getFullAddress()+" non ha la porta "+portId));
		}
	}
	
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
	 * @return false Se la porta non esiste
	 * @throws AISException 
	 */
	public void setPortName(String portId, String portName) throws AISException {
		DevicePort p = getPort(portId);
		p.setName(portName);
	}
	
	/*
	protected void generateEvent(String portId) throws AISException {
		generateEvent(portId,getPortValue(portId));
	}
	*/
		
	/**
	 * Imposta il valore di una porta di un device reale, gestendo la trasmissione del relativo messaggio
	 * 
	 * @param portId il nome della porta
	 * @param value il valore da impostare
	 * 
	 * @throws un'eccezione se qualcosa va male.
	 * @deprecated Basta il metodo writePort, che deve invalidare la cache se lo ritiene necessario
	 * TODO da rivedere per semplificare l'annidazione, spostando quasi tutto qui e lasciando al codice dei figlio solo cose specifiche 
	 */
	public void poke(String portId, Object newValue) throws AISException {
		DevicePort p = getPort(portId); 
		p.invalidate();
		writePort(portId, newValue);
	}
		
	/**
	 * 
	 */
	public void invalidate(String portId) throws AISException {
		DevicePort p = (DevicePort) ports.get(portId); 
		p.invalidate();
	}
    
    /**
     * Scrive un nuovo valore sulla porta del dispositivo fisico 
     * @param portId
     * @param newValue
     */
	public abstract void writePort(String portId, Object newValue) throws AISException ;
	
	/**
     * Legge dal dispositivo fisisco lo stato della porta ed aggiorna il valore corrispondente (portValues)  
	 * @param portId
	 * @return Tempo massimo previsto per l'aggiornamento in millisecondi
	 * @throws AISException
	 */
	public abstract long updatePort(String portId) throws AISException;
	
	/**
	 * Aggiorna il valore della porta
	 * Se il valore della porta è modificato genera un deviceEvent
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
		DevicePort p = (DevicePort) ports.get(portId);			
		return p.getValue();
	}
	
	/**
	 * Fornisce il timestamp corrispondente all'ultimo aggiornamento del valore della porta
	 * @param portId
	 * @return
	 */
	protected long getPortTimestamp(String portId) {
		DevicePort p = (DevicePort) ports.get(portId);			
		return p.getTimestamp();
	}

	public void fireDevicePortChangeEvent(DevicePortChangeEvent evt) {
		connector.fireDevicePortChangeEvent( evt );
	}

	/*
	public void onDevicePortChangeEvent(DevicePortChangeEvent event) {
		connector.onDevicePortChangeEvent(event);		
	}
	*/

}
