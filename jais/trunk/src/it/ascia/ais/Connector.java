/**
 * Copyright (C) 2008 ASCIA S.r.l.
 */
package it.ascia.ais;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Vector;
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

	public static final boolean DEGUG = false;
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
    private LinkedHashMap<String,Device> devices;
    
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
     * @param addr l'indirizzo da cercare.
     * @return Elenco devices (eventualmente vuoto)
     */
    public Collection<Device> getDevices(Address addr) {
    	if (addr.getDevice() == null) {
    		return devices.values();
    	}
    	Vector<Device> res = new Vector<Device>();
    	Device device1 = getDevice(addr);
    	if (device1 != null) {
    		res.add(device1);
    	} else {
			for (Device device : devices.values()) {
				if (addr.matches(device.getFullAddress())) {
					res.add(device);
				}
			}
    	}
    	return res;    	
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

    public Device getDevice(Address address) {
    	return (Device) devicesAlias.get(address.getDeviceAddress());
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
			logger.trace("Port already queued for update: "+p.getFullAddress());			
		}
		if (updateQueue.contains(p)) {
			logger.warn("Port already in update queue: "+p.getFullAddress());
		} else {
			if (updateQueue.offer(p)) {
				p.setQueuedForUpdate();
				logger.trace("Port queued for update: "+p.getFullAddress());
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
    	logger.info("Using transport: "+transport.getInfo());
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
			    	if (p.isDirty() || p.isExpired()) {
				    	logger.trace("Updating (+"+updateQueue.size()+"): " + p.getFullAddress());
			    		p.update();
			    	} else {
			    		if (DEGUG) logger.trace("Port already updated: "+p.getFullAddress());
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
	 * TODO cambiare il costruttore in modo che venga fornito il ControllerModule invece del Controller ed eliminare setModule
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

