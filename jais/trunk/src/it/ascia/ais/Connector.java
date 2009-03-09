/**
 * Copyright (C) 2008 ASCIA S.r.l.
 */
package it.ascia.ais;

import java.util.HashMap;

import org.apache.log4j.Logger;

/**
 * Connector domotico.
 * 
 * <p>Un Connector e' un'interfaccia per JAIS verso un sistema. Ad esempio un
 * bus domotico o una centrale d'allarme.</p>
 * 
 * <p>Un Connector permette l'accesso a un insieme di {@link Device}.</p>
 * 
 * <p>A livello di protocollo, un connector e' identificato da un nome,
 * nella forma "tipo.numero".</p>
 * 
 * @author arrigo
 */
public abstract class Connector {
	
    /**
     * Il nostro nome secondo AUI.
     */
    protected String name;
    
	/**
	 * Transport con il quale il Connector comunica con il sistema
	 */
	public Transport transport;

	/**
	 * Controller che ha instanziato il Connector
	 */
	public Controller controller;

	/**
     * Il nostro logger.
     */
    protected Logger logger;

	/**
	 * I dispositivi presenti nel sistema.
	 */
    private HashMap devices;

    /**
     * 
     * @param name Nome del connettore
     * @param controller Controller del sistema
     */
    public Connector(String name, Controller controller) {
		this.name = name;
		this.controller = controller;
        devices = new HashMap();
		logger = Logger.getLogger(getClass());
	}

	/**
     * Ritorna tutti i Device collegati che rispondono a un certo indirizzo.
     * 
     * <p>Questa funzione deve gestire anche wildcard.</p>
     * 
     * @param deviceAddress l'indirizzo da cercare.
     * @return un'array di Device, eventualmente di lunghezza zero.
     * TODO gestire wildcards tipo 1.0.* o 1.*.0
     */
    public Device[] getDevices(String deviceAddress) {
		if (deviceAddress.equals("*")) {
			return (Device[]) devices.values().toArray(new Device[devices.size()]);
		} else {
			Device device = getDevice(deviceAddress);
			if (device == null) {
				return new Device[] {};
			} else {
				return new Device[] {device};
			}
				
		}
    }
    
    /**
     * Aggiunge un dispositivo all'elenco di quelli gestiti
     * @param device
     * @throws AISException
     */
    void addDevice(Device device) throws AISException {
    	String address = device.getAddress();
    	if (devices.containsKey(address)) {
    		throw(new AISException("Dispositivo duplicato con indirizzo "+address+" connettore "+getName()));
    	}
    	devices.put(address, device);
    }
    
    /**
     * Ottiene il device con il nome specificato, o null se non esiste 
     * @param address
     * @return
     */
    public Device getDevice(String address) {
    	return (Device) devices.get(address);
    }

    public HashMap getDevices() {
		return devices;
    }
        
    /**
     * Invia un messaggio e attende una risposta dal destinatario, se il
     * messaggio lo richiede.
     * 
     * @return true se il messaggio di risposta e' arrivato, o se l'invio e'
     * andato a buon fine.
     */
    public abstract boolean sendMessage(Message m);
    
	/**
	 * Propaga l'evento al controller
	 * @param event
	 */
    public void onDeviceEvent(DeviceEvent event) {
		controller.onDeviceEvent(event);
	}

	/**
     * Associa il Transport al Connector
     * @param transport Il Transport associato
     */
    public void bindTransport(Transport transport) {
    	this.transport = transport;
    	transport.connector = this;
    }


    /**
     * Resituisce il nome del controllore, come specificato alla creazione
     * @return
     */
	public String getName() {
		return name;
	}

	public abstract void received(byte b);

}

