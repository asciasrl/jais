/**
 * Copyright (C) 2008 ASCIA S.r.l.
 */
package it.ascia.ais;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;

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
public abstract class Connector extends SimpleConnector implements ConnectorInterface {

	public static final boolean DEGUG = false;
	private static final long DEFAULT_DISPATCH_TIMEOUT = 60;
	protected LinkedBlockingQueue<DevicePort> updateQueue;
	private Thread updatingThread;
	protected LinkedBlockingQueue<Message> dispatchQueue;
	private Thread dispatchingThread;
	protected MessageParser mp;

	private long autoupdate = 1000;

	/**
	 * Transport con il quale il Connector comunica con il sistema
	 */
	protected Transport transport;
	
    /**
     * 
     * @param name Nome del connettore
     */
    public Connector(String name) {
    	this(0,name);
    }

    /**
     * 
     * @param autoupdate Tempo autoaggiornamento porte scadute (expired)
     * @param name Nome del connettore
     */
    public Connector(long autoupdate, String name) {
    	super(name);
		this.autoupdate = autoupdate;
		updateQueue = new LinkedBlockingQueue<DevicePort>();
		updatingThread = new UpdatingThread();
		updatingThread.setName("Updating-"+getClass().getSimpleName()+"-"+getName());
		updatingThread.start();
		dispatchQueue = new LinkedBlockingQueue<Message>();
		dispatchingThread = new DispatchingThread(getDispatchingTimeout());
		dispatchingThread.setName("Dispatching-"+getClass().getSimpleName()+"-"+getName());
		dispatchingThread.start();
	}

    /**
     * How many time to wait for a message received before issuing a warning
     * @return time in seconds
     */
	protected long getDispatchingTimeout() {
		return DEFAULT_DISPATCH_TIMEOUT;
	}

    
    /**
     * Ottiene il device con l'indirizzo specificato, o null se non esiste
     * Usa l'elenco degli indirizzi alias 
     * @param address Indirizzo o alias del device
     * @return null se nessun device ha l'indirizzo
     */
	public Device getDevice(String address) {
    	return getDevice(new Address(address));
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
     * Aggiunge un porta alla coda delle porte da aggiornare
     * @param m
     */
	public synchronized void queueUpdate(DevicePort p) {
		if (p.isQueuedForUpdate()) {
			logger.trace("Port already queued for update: "+p);			
		}
		if (updateQueue.contains(p)) {
			logger.warn("Port already in update queue: "+p);
		} else {
			if (updateQueue.offer(p)) {
				p.setQueuedForUpdate();
				logger.trace("Port queued for update ("+updateQueue.size()+"): "+p);
			} else {
				logger.error("Queue full queuing for update: "+p);
			}
		}
	}
    
	/**
     * Associa il Transport al Connector
     * Questa implementazione permette di associare un solo transport per connector
     * @param transport Il Transport associato
     */
    public void addTransport(Transport transport) {
    	if (transport == null) {
    		logger.warn("Transport is null");
    		return;
    	}
    	logger.info("Using transport: "+transport.getInfo());
    	if (this.transport != null) {
			throw(new UnsupportedOperationException("Connector can have only one transport"));    		
    	}
    	this.transport = transport;
    	// FIXME Passare il connettore al costruttore del transport
    	transport.connector = this;
    }


	/**
	 * Questo metodo viene chiamato dal Transport per ogni byte che viene ricevuto
	 * Se il MessageParser ho ottenuto un messaggio valido, viene aggiunto alla coda di dispacciamento
	 * @param b Dato ricevuto
	 */
	public void received(int b) {
		if (mp == null) {
			return;
		}
		mp.push(b);
		if (mp.isValid()) {
			Message m = mp.getMessage();
			if (m != null) {
		    	if (dispatchQueue.remainingCapacity() > 0) {
			    	logger.debug("Received: " + m);
		    	} else {
		    		logger.error("Queue full for messagge: " + m);
		    	}
	    		try {
					dispatchQueue.put(m);
				} catch (InterruptedException e) {
					logger.error("Interrupted while putting message in dispatch queue");
				}
			}
		}
	}


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
		super.close();
		if (transport != null) {
			transport.close();
			transport = null;
		}
		updatingThread.interrupt();
    	try {
    		updatingThread.join();
		} catch (InterruptedException e) {
			logger.error("Interrupted:",e);
		}
		dispatchingThread.interrupt();
    	try {
    		dispatchingThread.join();
		} catch (InterruptedException e) {
			logger.error("Interrupted:",e);
		}
	}
	
	/**
	 * Questo thread esegue l'aggiornamento delle porte che sono state messe nella apposita coda.
	 * Esegue il metodo DevicePort.update()
	 * @author Sergio
	 */
    private class UpdatingThread extends Thread {
        
    	public void run() {
			logger.debug("Start.");
    		while (isRunning()) {
    			DevicePort p;
				try {
					if (autoupdate > 0 && updateQueue.size() == 0) {
						queueExpiredPorts();
						p = (DevicePort) updateQueue.poll(autoupdate, TimeUnit.MILLISECONDS);
						if (p == null) {
							continue;
						}
					} else {
						p = (DevicePort) updateQueue.take();
					}					
			    	if (!p.isQueuedForUpdate()) {
			    		logger.trace("Already updated port " + p.getAddress());
			    	/*
			    	} else if (!p.isDirty() || !p.isExpired()) {
			    		logger.trace("Not dirty or expired port " + p.getAddress());
			    		//p.resetQueuedForUpdate();
			    	*/
			    	} else {
				    	logger.trace("Updating (+"+updateQueue.size()+"): " + p.getAddress());
				    	try {
				    		p.update();
							//p.resetQueuedForUpdate();
						} catch (Exception e) {
							p.resetQueuedForUpdate();
							throw(e);
						}
			    	}
			    	// TODO SPOSTATO, DA VERIFICARE p.resetQueuedForUpdate();
				} catch (InterruptedException e) {
					logger.debug("Interrotto.");
				} catch (Exception e) {
					logger.error("Errore:",e);
				}
    		}
			logger.debug("Stop.");
    	}
    }

	/**
	 * Questo thread esegue il dispacciamento dei messaggi ricevuti che sono stati messi nella apposita coda
	 * Esegue il metodo Connector.dispatchMessage()
	 * @author Sergio
	 * @since 20100513
	 */
    private class DispatchingThread extends Thread {
        
    	long timeout = DEFAULT_DISPATCH_TIMEOUT;
        
		public DispatchingThread(long dispatchingTimeout) {
			timeout = dispatchingTimeout;
		}

		public void run() {
			logger.debug("Start.");
    		while (isRunning()) {
    			Message m;
				try {
					// tolleranza del 10% in piu
					m = dispatchQueue.poll(Math.round(timeout * 1.1),TimeUnit.SECONDS);
					if (m == null) {
						logger.warn("No message received in "+timeout+" seconds.");
					} else {
						if (DEGUG) logger.trace("Dispatching: " + m);
						dispatchMessage(m);
					}
				} catch (InterruptedException e) {
					logger.debug("Interrotto.");
				} catch (Exception e) {
					logger.fatal("Errore:",e);
				}
    		}
			logger.debug("Stop.");
    	}
    }

	
	protected void queueExpiredPorts() {
		for (Device device : getDevices()) {
			for (DevicePort devicePort : device.getPorts()) {
				if (devicePort.isExpired() && !devicePort.isQueuedForUpdate()) {
					logger.trace("Queuing for update expired port "+devicePort.getAddress());
					queueUpdate(devicePort);
				}
			}
		}
	}

	/**
	 * 
	 * @return La configurazione del modulo
	 */
	public HierarchicalConfiguration getModuleConfiguration() {		
		return getModule().getConfiguration();
	}

	/**
	 * 
	 * @return La configurazione specifica del connettore
	 */
	public HierarchicalConfiguration getConfiguration() {		
		HierarchicalConfiguration config = getModuleConfiguration();
		config.setExpressionEngine(new XPathExpressionEngine());
		return config.configurationAt("connectors/connector[name='" + getName() + "']");
	}	

}

