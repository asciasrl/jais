package it.ascia.ais;

public abstract class PollingConnectorImpl extends ConnectorImpl {

	private Thread updatingThread;
	
	protected long autoupdate = 1000;

	/**
     * Variabile del watchdog
     */
	protected boolean isalive = true;
	
	/**
	 * Transport con il quale il Connector comunica con il sistema
	 */
	protected Transport transport;


	public PollingConnectorImpl(String name, long autoupdate) {
		super(name);
		this.autoupdate = autoupdate;
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

	public boolean isAlive() {
		if (isalive) {
			isalive = false;
			return true;
		} else {
			return false;
		}
	}

    private class UpdatingThread extends Thread {
        
    	public void run() {
			logger.debug("Start.");
    		while (isRunning()) {    			
				try {
					isalive = true;
					if (autoupdate > 0) {
						Thread.sleep(autoupdate);
					}
	    			doUpdate();
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
     * Perform periodic update task
     */
	public abstract void doUpdate();

	/**
	 * Avvia il thread di aggiornamento
	 */
	@Override
	public void start() {
		super.start();
		updatingThread = new UpdatingThread();
		updatingThread.setName("Updating-"+getClass().getSimpleName()+"-"+getConnectorName());
		updatingThread.setDaemon(true);
		updatingThread.start();		
	}



}
