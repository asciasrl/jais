package it.ascia.dmx;

import it.ascia.ais.AISException;
import it.ascia.ais.Address;
import it.ascia.ais.Connector;
import it.ascia.ais.Message;

public class DMXConnector extends Connector {
	
	private DMXStageProfiMK3 dmxinterface;
	
	private Message message;
	
	public DMXConnector(String name) {
		super(name);
		//TODO rendere specificabile nel file di configurazione
		dmxinterface = new DMXStageProfiMK3();
		mp = dmxinterface;
	}

	@Override
	protected void dispatchMessage(Message m) throws AISException {
		dmxinterface.dispatchMessage(m);
	}

	public void addChannel(int i) {
		addDevice(new DMXChannel(i));		
	}

	public void addRGB(int i, int r, int g, int b) {
		addDevice(new DMXRGB(i,
				(DMXChannel)getDevice(new Address(getName(), "channel"+r, null)),
				(DMXChannel)getDevice(new Address(getName(), "channel"+g, null)),
				(DMXChannel)getDevice(new Address(getName(), "channel"+b, null))));
						
	}
	
	@Override
	public boolean sendMessage(Message m) {
		boolean ret = false;
		try {	
			if (transport.tryAcquire()) {
				logger.trace("Transport semaphore is free");
			} else {
				logger.trace("Start waiting for transport semaphore ...");
				transport.acquire();
				logger.trace("Done waiting for transport semaphore.");
			}
			message = m;
			synchronized (message) {
				ret = dmxinterface.sendMessage(message, transport);
    	    	try {
    	    		message.wait(1000); // TODO parametro
    	    	} catch (InterruptedException e) {
    				logger.trace("sendMessage interrupted");
    	    	}
    	    	if (message.isSent()) {
    	    		logger.trace("ack");
    	    		ret = true;
    	    	} else {
    	    		logger.error("nak: " + m);
    	    		ret = false;
    	    	}
			}
		} catch (InterruptedException e) {
			logger.debug("Interrupted:",e);
		} catch (Exception e) {
			logger.error("Exception:",e);
		}
		transport.release();
		return ret;
	}

	@Override
	public void received(int b) {
		char c = (char)b;
		logger.info("Received: "+c);
		synchronized (message) {
			if (b == 71) {
				message.setSent();				
	    		logger.trace("ack received");
				message.notify();
			}
		}
		super.received(b);
	}
	
}
