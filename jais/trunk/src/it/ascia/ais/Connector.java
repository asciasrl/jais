/**
 * Copyright (C) 2008 ASCIA S.r.l.
 */
package it.ascia.ais;

import java.util.LinkedHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

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

	protected LinkedBlockingQueue receiveQueue;
	protected LinkedBlockingQueue sendQueue;
	private Thread sendingThread;
	private Thread receivingThread;
	private boolean running = false;
	
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
	public Controller controller;

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
     */
    public Connector(String name, Controller controller) {
		this.name = name;
		this.controller = controller;
        devices = new LinkedHashMap();
        devicesAlias = new LinkedHashMap();
		logger = Logger.getLogger(getClass());
		transportSemaphore = new Semaphore(1,true);
		running = true;
		receiveQueue = new LinkedBlockingQueue();
		receivingThread = new ReceivingThread();
		receivingThread.setName("Receiving-"+getClass().getSimpleName()+"-"+getName());
		receivingThread.start();
		sendQueue = new LinkedBlockingQueue();
		sendingThread = new SendingThread();
		sendingThread.setName("Sending-"+getClass().getSimpleName()+"-"+getName());
		sendingThread.start();
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
     * Aggiunge un messaggio alla coda dei messaggi da inviare e ritorna immediatamente
     * @param m
     */
	public void queueMessage(Message m) {
		sendQueue.offer(m);
	}
    
	/**
	 * Propaga l'evento al controller
	 * @param event
	 */
	public void fireDevicePortChangeEvent(DevicePortChangeEvent evt) {
		controller.fireDevicePortChangeEvent( evt );
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
		transport.close();
		running = false;
    	receivingThread.interrupt();
		sendingThread.interrupt();
	}
	
    private class ReceivingThread extends Thread {
        
    	public void run() {
    		while (running) {
    			Message m;
				try {
					m = (Message) receiveQueue.take();
			    	logger.debug("Dispatching (+"+receiveQueue.size()+"): " + m);
					dispatchMessage(m);
				} catch (InterruptedException e) {
					logger.debug("Interrotto.");
				} catch (AISException e) {
					logger.error(e.getMessage(),e);
				} catch (Exception e) {
					logger.fatal(e.getMessage(),e);
				}
    		}
			logger.debug("Stop.");
    	}
    }

    private class SendingThread extends Thread {
        
    	public void run() {
    		while (running) {
    			Message m;
				try {
					//logger.debug("Messaggi in coda: "+sendQueue.size());
					m = (Message) sendQueue.take();
			    	logger.debug("Sending (+"+sendQueue.size()+"): " + m);
					sendMessage(m);
				} catch (InterruptedException e) {
					logger.debug("Interrotto.");
				} catch (Exception e) {
					logger.fatal("Errore:",e);
				}
    		}
			logger.debug("Stop.");
    	}
    }

}

