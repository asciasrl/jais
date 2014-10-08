package it.ascia.duemmegi;

import it.ascia.ais.AISException;
import it.ascia.ais.ConnectorInterface;
import it.ascia.ais.ControllerModule;
import it.ascia.ais.Message;
import it.ascia.ais.PollingConnectorImpl;
import it.ascia.ais.ResponseMessage;
import it.ascia.duemmegi.domino.DominoDevice;
import it.ascia.duemmegi.domino.device.*;
import it.ascia.duemmegi.fxpxt.FXPXTMessageParser;
import it.ascia.duemmegi.fxpxt.FXPXTRequestMessage;
import it.ascia.duemmegi.fxpxt.ReadIdRequestMessage;
import it.ascia.duemmegi.fxpxt.ReadInputsRequestMessage;
import it.ascia.duemmegi.fxpxt.ReadOutputsRequestMessage;

public class DFCPConnector extends PollingConnectorImpl implements ConnectorInterface {

	/**
	 * MessageParser per la lettura dei messaggi in ingresso.
	 */
	protected FXPXTMessageParser mp;

	protected FXPXTRequestMessage request;

	public DFCPConnector(long autoupdate, String name, ControllerModule module) {
		super(autoupdate,name);
		mp = new FXPXTMessageParser();
	}
	
	private int indirizzo = 1;


	/*
	public boolean sendMessage(Message m) {
		if (FXPXTRequestMessage.class.isInstance(m)) {
			return sendRequestMessage((FXPXTRequestMessage)m);
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

	public boolean sendRequestMessage(FXPXTRequestMessage m) {
    	boolean received = false;
		try {
			if (!transport.tryAcquire()) {
				logger.trace("Start waiting for transport semaphore ...");
				transport.acquire();
				logger.trace("Done waiting for transport semaphore.");
			}
			if (request != null && FXPXTRequestMessage.class.isInstance(m)) {
				logger.debug("Messaggio gia' inviato in attesa di risposta: "+m);				
				return true;
			}
			request = m;
	    	synchronized (request) {
    			transport.write(m.getBytesMessage());
    			// si mette in attesa, ma se nel frattempo arriva la risposta viene avvisato
    	    	try {
    	    		request.wait((long)(100 * (1 + 0.2 * Math.random())));
    	    	} catch (InterruptedException e) {
    				logger.trace("sendRequestMessage wait:2");
    	    	}
		    	received = m.isAnswered();
		    	request = null;
	    	}
		} catch (InterruptedException e) {
			logger.error("Interrupted:",e);
		}
    	if (! received) {
    		logger.error("Messaggio non risposto: "+m);
    	}
		transport.release();
		return received;
	}
	*/


	public void addDevice(String model, String address) throws AISException {
		DominoDevice d = null;
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
		} else if (model.equals("DFDI")) {
			d = new DFDI(address);
		} else if (model.equals("DFDV")) {
			d = new DFDV(address);
		} else if (model.equals("DFTA")) {
			d = new DFTA(address);
		} else {
			logger.error("Modello sconosciuto: "+model);
		}
		if (d != null) {
			super.addDevice(d);
			logger.info("Aggiunto modulo "+model+" "+d.getDeviceAddress());
		}
	}


	@Override
	public void doUpdate() {
		logger.debug("Start polling cycle");
		long startTimeMillis = System.currentTimeMillis();
		request = new ReadIdRequestMessage(indirizzo);
		sendMessage();
		int step = 31;
		for (int i = 1; i < 256; i+=step) {
			request = new ReadOutputsRequestMessage(indirizzo,i, step);
			sendMessage();			
		}
		for (int i = 1; i < 256; i+=step) {
			request = new ReadInputsRequestMessage(indirizzo,i, step);
			sendMessage();			
		}
		logger.trace("Completed polling cycle in "+(System.currentTimeMillis() - startTimeMillis) + "mS");
	}
	
	private boolean sendMessage() {
		boolean res = false;
		try {
			if (!transport.tryAcquire()) {
				logger.trace("Start waiting for transport semaphore ...");
				transport.acquire();
				logger.trace("Done waiting for transport semaphore.");
			}
	    	synchronized (request) {
	    		long startTimeMillis = System.currentTimeMillis();
				transport.write(request.getBytesMessage());
				logger.trace("Request:" + request);
				if (!request.isAnswered()) {
	    			// si mette in attesa, ma se nel frattempo arriva la risposta viene avvisato
	    	    	try {
	    	    		request.wait(autoupdate);
	    	    	} catch (InterruptedException e) {
	    				logger.trace("interrupted!");
	    	    	}
				}
				if (request.isAnswered()) {
					res=true;
					logger.trace("Response received in "+(System.currentTimeMillis() - startTimeMillis) + "mS: " + request.getResponse());
				} else {
					logger.error("Response not received to: "+request);
				}
	    	}
		} catch (InterruptedException e) {
			logger.debug("Interrupted:",e);
		} catch (Exception e) {
			logger.error("Exception:",e);
		}
		transport.release();
		return res;
	}

	public void received(int b) {
		if (mp == null) {
			return;
		}
		mp.push(b);
		if (mp.isValid()) {
			Message m = mp.getMessage();
			//logger.debug("Ricevuto " + m);
	    	if (request != null 
	    			&& ResponseMessage.class.isInstance(m) 
	    			&& request.isAnsweredBy((ResponseMessage)m)) {
				// sveglia sendMessage
				synchronized (request) {
					request.setResponse((ResponseMessage)m);
					((ResponseMessage)m).setRequest(request);
		    		request.setAnswered(true);
					request.notify(); 						
				}
	    	}
		}
	}

}
