/**
 * Copyright (C) 2008 ASCIA S.r.l.
 */
package it.ascia.ais;

import java.util.LinkedHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

import org.apache.commons.configuration.HierarchicalConfiguration;
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
 * @author arrigo, sergio
 * TODO Aggiungere gestione stato del trasport (connesso, disconnesso, passivo, ecc.) e riconnessione
 */
public abstract class Connector {

	protected LinkedBlockingQueue updateQueue;
	private Thread updatingThread;
	private boolean running = false;
	private ControllerModule module = null;
	
    /**
     * Il nostro nome secondo AUI.
     */
    protected String name;
    
	/**
	 * Transport con il quale il Connector comunica con il sistema
	 */
	protected Transport transport;
	
	protected Semaphore transportSemaphore;

	/**
	 * Controller che ha instanziato il Connector
	 */
	private Controller controller;

	/**
     * Il nostro logger.
     */
    protected Logger logger;

	/**
	 * I dispositivi presenti nel sistema.
	 */
    private LinkedHashMap devices;
    
	/**
	 * L'elenco degli indirizzi - primari o alias - dei dispositivi.
	 */
    private LinkedHashMap devicesAlias;

    /**
     * 
     * @param name Nome del connettore
     * @param controller Controller del sistema
     * 
     * TODO Sostituire Controller con ControllerModule
     */
    public Connector(String name, Controller controller) {
		this.name = name;
		this.setController(controller);
        devices = new LinkedHashMap();
        devicesAlias = new LinkedHashMap();
		logger = Logger.getLogger(getClass());
		transportSemaphore = new Semaphore(1,true);
		running = true;
		updateQueue = new LinkedBlockingQueue();
		updatingThread = new UpdatingThread();
		updatingThread.setName("Updating-"+getClass().getSimpleName()+"-"+getName());
		updatingThread.start();
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
     * Non sono ammessi duplicati
     * @param device
     * @throws AISException
     */
    void addDevice(Device device) throws AISException {
    	String address = device.getAddress();
    	if (devices.containsKey(address)) {
    		throw(new AISException("Dispositivo duplicato con indirizzo "+address+" connettore "+getName()));
    	}
    	devices.put(address, device);
    	addDeviceAlias(address, device);
    }
    
    /**
     * Aggiunge un indirizzo "alias" che corrisponde allo stesso device fisico
     * Se l'alias e' gia' presente viene semplicemente ridefinito
     * @param address Indirizzo alias del dispositivo
     * @param device Dispositivo effettivo
     */
    public void addDeviceAlias(String address, Device device) {
    	devicesAlias.put(address, device);
    }
        
    /**
     * Ottiene il device con il nome specificato, o null se non esiste
     * Usa l'elenco degli indirizzi alias 
     * @param address Indirizzo o alias del device
     * @return null se nessun device ha l'indirizzo
     */
    public Device getDevice(String address) {
    	return (Device) devicesAlias.get(address);
    }

    public LinkedHashMap getDevices() {
		return devices;
    }
        
    /**
     * Invia un messaggio e attende una risposta dal destinatario, se il
     * messaggio lo richiede.
     * 
     * @return true se il messaggio di risposta e' arrivato, o se l'invio e'
     * andato a buon fine.
     * @throws AISException 
     */
    public abstract boolean sendMessage(Message m);
    
    /**
     * Invia un messaggio e attende una risposta dal destinatario, se il
     * messaggio lo richiede.
     * 
     * @return true se il messaggio di risposta e' arrivato, o se l'invio e'
     * andato a buon fine.
     * @throws AISException 
     */
    public abstract boolean sendMessage(String messageCode, Object value);

    /**
     * Aggiunge un porta alla coda delle porte da aggiornare
     * @param m
     */
	public void queueUpdate(DevicePort p) {
		if (p.isQueuedForUpdate()) {
			logger.debug("Port already queued for update: "+p.getFullAddress());			
		}
		if (updateQueue.contains(p)) {
			logger.warn("Port already in update queue: "+p.getFullAddress());
		} else {
			if (updateQueue.offer(p)) {
				p.setQueuedForUpdate();
				logger.debug("Port queued for update: "+p.getFullAddress());
			} else {
				logger.error("Queue full queuing for update: "+p.getFullAddress());
			}
		}
	}
    
	/**
	 * Propaga l'evento al controller
	 */
	public void fireDevicePortChangeEvent(DevicePortChangeEvent evt) {
		module.fireDevicePortChangeEvent(evt);
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
     * @return Nome
     */
	public String getName() {
		return name;
	}

	/**
	 * Questo metodo viene chiamato dal Transport per ogni byte che viene ricevuto 
	 * @param b Dato ricevuto
	 */
	public abstract void received(int b);

    /**
     * Invia il messaggio alle istanze di dispositivo di questo connettore.
     * Ogni sottoclasse implementa la logica con la quale decide a quali vada inviato.
     * Il messaggio potrebbe essere gestito direttamente solo da questo metodo.
     * 
     * @param m il messaggio da gestire
     * @throws AISException 
     * @throws AISException 
     */
	protected abstract void dispatchMessage(Message m) throws AISException;
    
	/**
	 * Chiude il Transport e le code di invio/ricezione 
	 */
	public void close() {
		if (transport != null) {
			transport.close();
			transport = null;
		}
		running = false;
		updatingThread.interrupt();
    	try {
    		updatingThread.join();
		} catch (InterruptedException e) {
			logger.error("Interrupted:",e);
		}
	}
	
    /**
	 * @param controller the controller to set
	 */
	public void setController(Controller controller) {
		this.controller = controller;
	}

	/**
	 * @return the controller
	 */
	public Controller getController() {
		return controller;
	}

    private class UpdatingThread extends Thread {
        
    	public void run() {
    		while (running) {
    			DevicePort p;
				try {
					p = (DevicePort) updateQueue.take();
			    	logger.debug("Updating (+"+updateQueue.size()+"): " + p.getFullAddress());
			    	if (p.isDirty() || p.isExpired()) {
			    		p.update();
			    	} else {
			    		logger.trace("Port already updated: "+p.getFullAddress());
			    	}
			    	p.resetQueuedForUpdate();
				} catch (InterruptedException e) {
					logger.debug("Interrotto.");
				} catch (Exception e) {
					logger.fatal("Errore:",e);
				}
    		}
			logger.debug("Stop.");
    	}
    }


	/**
	 * @param Set the Controller Module to which connector belongs
	 */
	public void setModule(ControllerModule module) {
		this.module = module;
	}

	/**
	 * @return Get the Controller Module to which connector belongs
	 */
	public ControllerModule getModule() {
		return module;
	}
	
	
	public HierarchicalConfiguration getConfiguration() {		
		return getModule().getConfiguration();
	}

}

