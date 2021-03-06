package it.ascia.duemmegi;

import it.ascia.ais.AISException;
import it.ascia.ais.Connector;
import it.ascia.ais.ControllerModule;
import it.ascia.ais.Message;
import it.ascia.duemmegi.domino.device.*;
import it.ascia.duemmegi.dxp.DXPMessage;
import it.ascia.duemmegi.dxp.DXPMessageParser;
import it.ascia.duemmegi.dxp.DXPRequestMessage;
import it.ascia.duemmegi.dxp.DXPResponseMessage;

public class DXPConnector extends Connector {

	/**
	 * MessageParser per la lettura dei messaggi in ingresso.
	 */
	protected DXPMessageParser mp;
	private DXPRequestMessage messageToBeAnswered;
	
	public DXPConnector(long autoupdate, String name, ControllerModule module) {
		super(autoupdate,name);
		mp = new DXPMessageParser();
	}

	public void received(int b) {
		mp.push(b);
		if (mp.isValid()) {
			DXPMessage m = (DXPMessage) mp.getMessage();
			if (m != null) {
				dispatchMessage(m);
			}
		}    	
	}

	public boolean sendMessage(Message m) {
		if (DXPRequestMessage.class.isInstance(m)) {
			return sendRequestMessage((DXPRequestMessage)m);
		} else {
			try {
				transport.acquire();
				transport.write(m.getBytesMessage());
				transport.release();
				return true;
			} catch (InterruptedException e) {
				logger.error("Interrupted:",e);
				return false;
			}
		}
	}

	public boolean sendRequestMessage(DXPRequestMessage m) {
    	boolean received = false;
		if (messageToBeAnswered != null) {
			// FIXME
		}
		try {
			transport.acquire();
			if (messageToBeAnswered != null) {
				logger.error("messageToBeAnswered non nullo: "+messageToBeAnswered);
				logger.error("Messaggio in attesa :"+m);
				return false;
			}
        	messageToBeAnswered = m;
	    	synchronized (messageToBeAnswered) {
    			transport.write(m.getBytesMessage());
    			// si mette in attesa, ma se nel frattempo arriva la risposta viene avvisato
    	    	try {
    	    		messageToBeAnswered.wait((long)(100 * (1 + 0.2 * Math.random())));
    	    	} catch (InterruptedException e) {
    				logger.trace("sendRequestMessage wait:2");
    	    	}
		    	received = m.isAnswered();
		    	messageToBeAnswered = null;
	    	}
			transport.release();
		} catch (InterruptedException e) {
			logger.error("Interrupted:",e);
		}
    	if (! received) {
    		logger.error("Messaggio non risposto: "+m);
    	}
		return received;
	}

	protected void dispatchMessage(Message m) throws AISException {
    	if (messageToBeAnswered != null 
    			&& DXPResponseMessage.class.isInstance(m) 
    			&& messageToBeAnswered.isAnsweredBy((DXPMessage) m)) {
			// sveglia sendPTPRequest
			synchronized (messageToBeAnswered) {
	    		messageToBeAnswered.setAnswered(true);
				messageToBeAnswered.notify(); 						
			}
    	}
    	if (DXPResponseMessage.class.isInstance(m)) {
			// Al mittente 
			DominoDevice d = (DominoDevice)getDevice(((DXPResponseMessage)m).getSource());
			if (d != null) {
				/**
				 * FIXME recuperare DXP
				d.messageSent((DXPRequestMessage)m);
				 */
			} else {
				logger.warn("Non trovato device sender "+((DXPResponseMessage)m).getSource());
			}
    	}
    	if (DXPRequestMessage.class.isInstance(m)) {
			// Al destinatario 
    		DominoDevice d = (DominoDevice)getDevice(((DXPRequestMessage)m).getDestination());
			if (d != null) {
				/**
				 * FIXME recuperare DXP
				d.messageReceived((DXPRequestMessage)m);
				*/
			} else {
				logger.warn("Non trovato device receiver "+((DXPRequestMessage)m).getDestination());
			}
    	}
		
	}

	public void addDevice(String model, String address) throws AISException {
		DominoDevice d = null;
		/* FIXME modificare costruttori
		if (model.equals("DF4I")) {
			d = new DF4I(this, address);
		} else if (model.equals("DF4IV")) {
			d = new DF4IV(this, address);
		} else if (model.equals("DF8IL")) {
			d = new DF8IL(this, address);
		} else if (model.equals("DFIR")) {
			d = new DFIR(address);
		} else if (model.equals("DF4R")) {
			d = new DF4R(address);
		} else if (model.equals("DFTA")) {
			d = new DFTA(address);
		} else if (model.equals("DFTP")) {
			d = new DFTP(address);
		} else if (model.equals("DFCT")) {
			d = new DFCT(this, address);
		} else if (model.equals("DFGSM2")) {
			d = new DFGSM2(address);
		} else if (model.equals("DFDM")) {
			d = new DFDM(address);
		} else if (model.equals("DFTA")) {
			d = new DFTA(address);
		} else {
			logger.error("Modello sconosciuto: "+model);
		}
		*/
		if (d != null) {
			super.addDevice(d);
			logger.info("Aggiunto modulo "+model+" "+d.getAddress());
		}
	}

}
