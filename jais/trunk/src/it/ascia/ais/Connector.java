/**
 * Copyright (C) 2008 ASCIA S.r.l.
 */
package it.ascia.ais;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
    protected Map devices;

    public Connector(String name, Controller controller) {
		this.setName(name);
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
     * 
     * @return un'array di Device, eventualmente di lunghezza zero.
     */
    public Device[] getDevices(String deviceAddress) {
		Collection values = devices.values();
		if (deviceAddress.equals("*")) {
			return (Device[]) values.toArray(new Device[values.size()]);
		}
		List devices = new LinkedList();
		Iterator it = values.iterator();
		while (it.hasNext()) {
			Device device =  (Device)it.next();
			if (device.getAddress().equals(deviceAddress)) {
				devices.add(device);
			}
		}
		return (Device[]) devices.toArray(new Device[devices.size()]);
    }
    
    public void addDevice(Device device) {
    	device.bindConnector(this);
    	String deviceAddress = device.getAddress();
    	devices.put(new Integer(deviceAddress), device);    	
    }
        
    /**
     * Legge e interpreta i dati in arrivo.
     * 
     * <p>Questa funzione viene invocata dal Transport quando ci sono dati da leggere con readByte().</p>
     * 
     */
    public abstract void readData();

    /**
     * Invia un messaggio e attende una risposta dal destinatario, se il
     * messaggio lo richiede.
     * 
     * @return true se il messaggio di risposta e' arrivato, o se l'invio e'
     * andato a buon fine.
     */
    public abstract boolean sendMessage(MessageInterface m);
    
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

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}

