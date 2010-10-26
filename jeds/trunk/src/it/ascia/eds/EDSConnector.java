/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.ConfigurationNode;

import it.ascia.ais.AISException;
import it.ascia.ais.Connector;
import it.ascia.ais.Device;
import it.ascia.ais.Message;
import it.ascia.eds.device.*;
import it.ascia.eds.msg.*;

/**
 * Interfaccia verso il bus EDS.
 * 
 * <p>JAIS vede questo come un Connector, i cui Device sono BMC.</p>
 * 
 * @author arrigo
 *
 */
public class EDSConnector extends Connector {

	private Random r = new Random();

	/**
     * Il messaggio che stiamo mandando, per il quale aspettiamo una risposta.
     */
    private PTPMessage messageToBeAnswered;
    
    /**
     * Indirizzo con cui il Connettore invia messaggi sul BUS
     */
    private int myAddress = 0;

    /**
	 * Quanto tempo aspettare la risposta dopo l'invio di un messaggio.
	 * 
	 * <p>Nel caso peggiore (1200 bps), la trasmissione di un messaggio richiede 
	 * 8 / 120 = 660 msec. In quello migliore (9600 bps), la trasmissione 
	 * richiede 82 msec. Questa costante deve tener conto del caso migliore.</p>
	 * 
	 * Questo valore si puo' modificare nel file di configurazione:
	 * /jais/EDS/retrytimeout
	 */
	protected int RETRY_TIMEOUT = 300;

	/**
	 * Quante volte provare a reinviare un messaggio che richiede una risposta.
	 * 
	 * <p>Quando l'attesa supera PING_WAIT * WAIT_RETRIES, questa costante 
	 * decide quanti tentativi di ri-invio effettuare.</p>
	 */
	public static final int ACKMESSAGE_SEND_RETRIES = 3;
	/**
	 * Quante volte provare a reinviare un messaggio di richiesta stato 
	 * senza risposta.
	 * 
	 * <p>Quando l'attesa supera retryTimeout, questa costante 
	 * decide quanti tentativi di ri-invio effettuare.</p>
	 */
	protected static final int STATUSREQ_SEND_RETRIES = 3;
	/**
	 * Quante volte reinviare un messaggio broadcast
	 */
	public static final int BROADCAST_RESENDS = 7;
	
	private int lastBroadcast = -1;

	/**
	 * Indicate until connector should be considerered busy because one conversation between devices is in place
	 */
	private long guardtimeEnd = 0;

    /**
     * Connettore per il BUS EDS.
     * 
     * @param name il nome del Connector, che sara' la parte iniziale degli indirizzi
     * di tutti i Device collegati a questo Connector.
     * @param address 
     * @param edsControllerModule 
     */
    public EDSConnector(long autoupdate, String name, int myAddress) {
    	super(autoupdate, name);
		this.myAddress = myAddress; 		
		mp = new EDSMessageParser();
		for (int i = 1; i <= 31; i++) {
			addDevice(new EDSGroup("Group"+i));
		}
    }

    /**
     * Ritorna l'indirizzo del connettore.
     * 
     * <p>Questo metodo e' utile per i BMC, quando devono richiedere 
     * informazioni sul proprio stato. I messaggi che inviano devono partire 
     * "a nome" del connettore.</p>
     */
    public int getMyAddress() {
    	return myAddress;
    }
        
    public void received(int b) {
    	setGuardtime(messageTransmitTime());
    	super.received(b);
    }

    protected void dispatchMessage(Message m) throws AISException {
		if (EDSMessage.class.isInstance(m)) {
			dispatchMessage((EDSMessage) m);
		}
	}

    /**
     * Invia un messaggio a tutti i BMC destinatari e al mittente.
     * 
     * <p>Questo metodo deve essere chiamato dalla sottoclasse, per ogni 
     * messaggio che viene ricevuto.</p>
     * 
     * <p>Stampa un messaggio su stderr se il messaggio e' per un BMC che non e'
     * in lista, ne' in fase di ping.</p>
     * 
     * <p>Il BMCComputer riceve tutti i messaggi.</p>
     * 
     * @param m il messaggio da inviare
     * @throws AISException 
     */
    protected void dispatchMessage(EDSMessage m) throws AISException {
    	int rcpt = m.getRecipient();
    	int sender = m.getSender();
    	if (messageToBeAnswered != null 
    			&& PTPMessage.class.isInstance(m) 
    			&& messageToBeAnswered.isAnsweredBy((PTPMessage) m)) {
    		if (PTPResponse.class.isInstance(m) && PTPRequest.class.isInstance(messageToBeAnswered)) {
    			((PTPResponse) m).setRequest((PTPRequest) messageToBeAnswered);
    			((PTPRequest) messageToBeAnswered).setResponse((PTPResponse) m);
    		}
			// sveglia sendPTPMessage
			synchronized (messageToBeAnswered) {
	    		messageToBeAnswered.setAnswered(true);
				messageToBeAnswered.notify(); 						
			}
    	}
    	if (ComandoBroadcastMessage.class.isInstance(m)) {
			ComandoBroadcastMessage bmsg = (ComandoBroadcastMessage) m;
			if (lastBroadcast == bmsg.getRandom()) {
				logger.trace("Messaggio ripetuto: "+m);
			} else {
				lastBroadcast = bmsg.getRandom();
	    		// Mandiamo il messaggio a tutti
	    		Iterator it = getDevices().iterator();
	    		while (it.hasNext()) {
	    			Device d = (Device) it.next();
	    			if (BMC.class.isInstance(d)) {
	    				((BMC)d).messageReceived(m);
	    			}
	    		}
			}
    	} else if (RispostaModelloMessage.class.isInstance(m)) {
			// Aggiungiamo i BMC che si presentano
			RispostaModelloMessage risposta = (RispostaModelloMessage) m;
			BMC bmc = (BMC) getDevice(risposta.getSource()); 
			if (bmc == null) {
				bmc = createBMC(risposta.getSource(), risposta.getModello());
				addDevice(bmc);
				if (bmc != null) {
					logger.info("Creato BMC "+bmc+" Indirizzo:"+bmc.getFullAddress());
					if (isDiscoverNew()) {
						logger.debug("Discover new device: "+bmc.getFullAddress());
						BmcDiscover bmcDiscover = new BmcDiscover(bmc);
						bmcDiscover.start();
					} else {
						logger.debug("Disabled discover of new device in configuration");
					}
				}
			} else {
				bmc.messageSent(m);
			}
    	} else { 
    		BMC bmc;
    		
    		// Al mittente 
    		bmc = (BMC)getDevice((new Integer(sender)).toString());
    		if (bmc != null) {
    			bmc.messageSent(m);
    		}

    		// Al destinatario 
    		bmc = (BMC)getDevice((new Integer(rcpt)).toString());
    		if (bmc != null) {
    			bmc.messageReceived(m);
    		}
    	}
    }

    private BMC createBMC(String source, int modello) throws AISException {
    	return BMC.createBMC(source, modello, null);
	}


	public int getRetryTimeout() {
		return getModuleConfiguration().getInt("retrytimeout", RETRY_TIMEOUT);
	}
	
	private double getRetryTimeout(PTPMessage m) {
		return getRetryTimeout() + m.getRetryTimeout();
	}



    public boolean sendMessage(Message m) {
    	if (EDSMessage.class.isInstance(m)) {
    		return sendMessage((EDSMessage) m);
    	} else {
    		return false;
    	}
    }

    private long messageTransmitTime() {
    	return Math.round(1000 * 80 / transport.getSpeed());
    }
    
    /**
     * Calculate guard time based on transport speed
     */
	private void setGuardtime() {
		setGuardtime(Math.round(messageTransmitTime() * 2.5)); 		
	}

	private void setGuardtime(long dt) {
		setGuardtimeEnd(dt + System.currentTimeMillis());
		if (dt == 0) {
			synchronized (this) {
				notify();				
			}
		}
	}

	/**
	 * @param guardtimeEnd the guardtimeEnd to set: time in milliseconds 
	 */
	private void setGuardtimeEnd(long guardtimeEnd) {
		this.guardtimeEnd = guardtimeEnd;
	}

	/**
     * Connector is in Guard Time when a conversation between devices or controller is in place.
     * Concrete connector must use setGuardtimeEnd
	 * @return the time in millisecond to wait beacuse connector is in guard time
	 */
	private long getGuardtime() {
		long dt = 0;
		if (guardtimeEnd > 0 && guardtimeEnd >= System.currentTimeMillis()) {
			dt = guardtimeEnd - System.currentTimeMillis();
		}
		// windows ho uno scheduler che non garantisce i tempi bassi 
		if (dt > 0 && dt < 30) {
			if (System.getProperty("os.name").startsWith("Windows")) {
			  dt += 30;
			}
		}
		return dt;
	}


    /**
	 * Invia un messaggio sul transport.
	 * 
	 * <p>Se il messaggio richiede una risposta, questa viene attesa seguendo i
	 * timeout stabiliti.</p>
	 * 
	 * @return true se l'invio e' andato a buon fine; nel caso di richieste,
	 * ritorna true se e' arrivata una risposta.
	 */
	public boolean sendMessage(EDSMessage m) {
		if (transport == null) {
			logger.error("Transport not available for connector "+getName()+", cannot send message.");
			return false;
		}
		if (m.isSent()) {
			throw(new AISException("Messaggio gia' inviato"));
		}
		boolean retval = false;
		try {
			long guardtime = getGuardtime();
			while (guardtime > 0) {
				logger.debug("BUS is in guard time, waiting "+guardtime+"mS");
				long start = System.currentTimeMillis();
				synchronized (this) {
					wait(guardtime);							
				}
				logger.trace("End guard time of "+guardtime+"mS (actually " + (System.currentTimeMillis() - start)+")");
				guardtime = getGuardtime();
			}			
			if (!transport.tryAcquire()) {
				logger.trace("Start waiting for transport semaphore ...");
				transport.acquire();
				logger.trace("Done waiting for transport semaphore.");
			}
			if (BroadcastMessage.class.isInstance(m)) {
				sendBroadcastMessage((BroadcastMessage) m);
				retval = true;
			} else if (PTPMessage.class.isInstance(m)){ 
				PTPMessage ptpm = (PTPMessage) m;
				retval = sendPTPMessage(ptpm);
			} else {
				logger.error("Messaggio di tipo sconosciuto:"+m.getClass().getName());
				retval = false;
			}
			m.setSent();
		} catch (InterruptedException e) {
			logger.debug("Interrupted:",e);
		} catch (Exception e) {
			logger.error("Exception:",e);
		}
		transport.release();
		return retval;
	}

	/**
	 * Invia un messaggio broadcast.
	 * 
	 * <p>Gli header0
	 *  vengono ri-generati ad ogni invio.</p>
	 */
	private void sendBroadcastMessage(BroadcastMessage m) {
		int tries = m.getSendTries();
		transport.write(m.getBytesMessage());
		// ripetizioni
		for (int i = 1; i < tries; i++) {
			try {			
				Thread.sleep((long)(getRetryTimeout() * 2 * (1 + 2 * r.nextDouble())));
			} catch (InterruptedException e) {
			}
			transport.write(m.getBytesMessage());
		}
	}
	
	/**
	 * Invia un messaggio point-to-point e attende risposta.
	 *
	 * <p>Se la risposta non arriva dopo un certo tempo, essa viene ri-inviata 
	 * un tot di volte.</p>
     * 
     * <p>Il messaggio di risposta viene riconosciuto da dispatchMessage().</p>
	 */
	private boolean sendPTPMessage(PTPMessage m) {
    	int tries;
    	boolean received = false;
		if (messageToBeAnswered != null) {
			if (messageToBeAnswered.compareTo(m) == 0) {
				logger.debug("Messaggio gia' inviato in attesa di risposta: "+m);				
				return true;
			}
		}
		try {
			if (messageToBeAnswered != null) {
				logger.error("messageToBeAnswered non nullo: "+messageToBeAnswered);
				logger.error("Messaggio da inviare: "+m);
				return false;
			}
    		BMC recipient = (BMC)getDevice((new Integer(m.getRecipient())).toString());
        	messageToBeAnswered = m;
	    	synchronized (messageToBeAnswered) {
	    		for (tries = 1;
	    			tries <= m.getMaxSendTries(); 
	    			tries++) {
	    			while (mp.isBusy()) {
	    				logger.trace("MessageParser busy, delaying 20mS ... "+mp.dumpBuffer());
	    				Thread.sleep(20);
	    			}
	    			logger.trace("Try "+tries+" of "+m.getMaxSendTries()+" : "+m.toString());
	    			setGuardtime();
	    			// FIXME Tentativo di evitare doppio messaggio per richiesta stato
	    			//Thread.sleep(50);
	    			transport.write(m.getBytesMessage());
	    			if (!m.isAnswered()) {
		    			// si mette in attesa, ma se nel frattempo arriva la risposta viene avvisato
		    	    	try {
		    	    		messageToBeAnswered.wait((long)(getRetryTimeout(m) * (1 + 2 * r.nextDouble())));
		    	    	} catch (InterruptedException e) {
		    				logger.trace("sendPTPRequest interrupted");
		    	    	}
	    			}
	    			if (m.isAnswered()) {
		    			setGuardtime(0);
		    			if (tries > 1) {
		    	    		logger.warn("Message aswered after "+tries+" tries: "+m);		    				
		    			}
	    				break;
	    			}
	    		}
		    	received = m.isAnswered();
		    	if (recipient == null) {
		    		// riprova, potrebbe essere stato creato nel frattempo
		    		recipient = (BMC)getDevice((new Integer(m.getRecipient())).toString());
		    	}
	    		if (recipient != null) {
				    if (received) {
						recipient.setReachable();
					} else {
						recipient.setUnreachable();
					}
	    		}
	    	}
		} catch (InterruptedException e) {
			logger.debug("Interrupted:",e);
		} catch (Exception e) {
			logger.error("Exception:",e);
		}
    	messageToBeAnswered = null;
    	if (! received) {
    		logger.error("Messaggio non risposto: "+m);
    	}
		return received;
    }

	/**
     * "Scopre" il BMC indicato inviandogli un messaggio di richiesta modello.
     * 
     * <p>Se il BMC non era gia' in lista, allora verra' inserito dal metodo 
     * messageReceived().
     * 
     * @param address l'indirizzo del BMC da "scoprire".
     * 
     */
    public void discoverBMC(int address) {
    	// TODO Rendere sincrono il discover
    	sendMessage(new RichiestaModelloMessage(address,getMyAddress()));
    }

    /**
     * Load configuration from EDSConfig
     * @param EDSConfigFileName
     * @return
     */
	public boolean loadConfig(String EDSConfigFileName) {
		XMLConfiguration EDSConfig;
		try {
			EDSConfig = new XMLConfiguration(EDSConfigFileName);
		} catch (ConfigurationException e) {
			logger.error("Error reading configuration file: "+e);
			return false;
		}
		List dispositivi = EDSConfig.configurationsAt("dispositivo");
		for(Iterator id = dispositivi.iterator(); id.hasNext();)
		{
		    HierarchicalConfiguration dispositivo = (HierarchicalConfiguration) id.next();
		    String name = (String) dispositivo.getProperty("[@nome]");
		    String address = (String) dispositivo.getString("indirizzo");
		    int model = dispositivo.getInt("modello");
		    String revision = (String) dispositivo.getString("revisione");
		    BMC bmc = BMC.createBMC(address, model, name);
		    if (bmc != null) {
		    	logger.debug(bmc.getClass().getSimpleName()+" address="+address+" model="+model+" revision="+revision);		    	
				List inputs = dispositivo.configurationsAt("ingresso");
				int iInput = 0;
				for(Iterator ii = inputs.iterator(); ii.hasNext();)
				{
				    HierarchicalConfiguration input = (HierarchicalConfiguration) ii.next();
				    String inputName = input.getString("[@nome]",null);
				    if (inputName != null && !inputName.equals("")) {
				    	bmc.setInputName(iInput, inputName);
				    	logger.debug("BMC "+address+" input:"+iInput+" name:"+inputName);
				    }
				    iInput++;
				}
				List outputs = dispositivo.configurationsAt("uscita");
				int iOutput = 0;
				for(Iterator io = outputs.iterator(); io.hasNext();)
				{
				    HierarchicalConfiguration output = (HierarchicalConfiguration) io.next();
				    String outputName = output.getString("[@nome]",null);
				    if (outputName != null  && !outputName.equals("")) {			    
				    	bmc.setOutputName(iOutput, outputName);
				    	logger.debug("BMC "+address+" output:"+iOutput+" name:"+outputName);
				    }
				    if (BMCStandardIO.class.isInstance(bmc)) {
				    	int tipo = output.getInt("tipo");
				    	// FIXME Verificare codici !
				    	if (tipo == 4) {
				    		((BMCStandardIO) bmc).addBlindPort(iOutput);
				    	}
				    }
				    
				    bmc.setOutputTimer(iOutput, 1000 * output.getInt("timer",0));
				    List groups = output.configurationAt("gruppiAppartenenza").getRootNode().getChildren();
				    for(Iterator ig = groups.iterator(); ig.hasNext();)
				    {
				    	SubnodeConfiguration group = output.configurationAt("gruppiAppartenenza." + ((ConfigurationNode) ig.next()).getName());
				    	int groupNumber = group.getInt("numero");
				    	if (groupNumber > 0) {
				    		bmc.bindOutput(groupNumber, iOutput);
				    	}
				    }
				    iOutput++;
				}
				addDevice(bmc);
				// TODO verificare che non si abbiano duplicazioni sulle associazioni uscite
				bmc.discover();
		    }
		}    		
		return true;
	}

	public BMC addBmc(String modelName, String address) throws AISException {
		if (modelName == null || address == null) {
			throw(new AISException("Must specify model and address"));
		}
		int model;
		if (modelName.startsWith("REG-T-22")) {
			model = 122; 
		} else {
			try {
			    model = new Integer(modelName).intValue();			
			} catch (NumberFormatException e) {
				throw(new AISException("Invalid model for addBMC: "+modelName));
			}			
		}
	    BMC bmc = BMC.createBMC(address, model);
	    addDevice(bmc);
	    return bmc;
	}

	public boolean isDiscoverNew() {
		return getModuleConfiguration().getBoolean("discovernew", true);
	}
	
	private class BmcDiscover extends Thread {
		
		private BMC bmc;
		
		public BmcDiscover(BMC bmc) {
			this.bmc = bmc;
			setName("Discover-"+bmc.getFullAddress());
		}
		
		public void run() {
			bmc.discover();
		}
	}
}
