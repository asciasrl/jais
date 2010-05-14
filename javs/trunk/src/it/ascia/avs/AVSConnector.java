package it.ascia.avs;

import it.ascia.ais.AISException;
import it.ascia.ais.Connector;
import it.ascia.ais.Message;

class AVSConnector extends Connector {

	private CentraleAVS centrale;

	private int peerSeqNumber;

	//private boolean sentReply;	

	/**
	 * 
	 * @param name Nome del connettore
	 * @param interfaccia Interfaccia di comunicazione
	 * @param modello Modello della centrale connessa alla interfaccia
	 */
	AVSConnector(String name, String interfaccia, String modello) {
		super(name);		
		mp = new AVSMessageParser();
 		if (interfaccia.equals("EasyLink")) {
 			if (modello.equals("Advance88")) {
 				centrale = new Advance88(this);
 			} else {
 				throw(new AISException("Control unit '"+modello+"' cannot be connected to interface '"+interfaccia+"'"));
 			}
 		} else if (interfaccia.equals("XLink")) {
 			if (modello.equals("Xtream640")) {
 				centrale = new Xtream640(this);
 			} else {
 				throw(new AISException("Control unit '"+modello+"' cannot be connected to interface '"+interfaccia+"'"));
 			}
 		} else {
			throw(new AISException("Unsupported control unit '"+modello+"'"));
 		}
	}

	@Override
	protected void dispatchMessage(Message m) {
		if (!AVSMessage.class.isInstance(m)) {
			logger.error("Cannot dispatch message of class "+m.getClass().getName());
		} else {
			dispatchMessage((AVSMessage) m);
		}
	}
		
	/**
	 * Dispatch AVSMessage to the "centrale" connected
	 * @param m
	 */
	private void dispatchMessage(AVSMessage m) {
		peerSeqNumber = m.getSeqNumber();
		centrale.processMessage(m);		
	}

	@Override
	public boolean sendMessage(Message m) {
		if (AVSMessage.class.isInstance(m)) {
			return sendMessage((AVSMessage)m);
		} else {
			logger.error("Cannot send message of class "+m.getClass().getName());
			return false;
		}
	}
	
	public boolean sendMessage(AVSMessage m) {
		/*
		if (sentReply) {
			logger.error("Reply message already sent in this roundtrip, cannot send "+m);
			return false;
		}
		*/
		m.setSeqNumber(((peerSeqNumber & 0x0F) << 4 ) + ((peerSeqNumber & 0xF0) >> 4) + 1);
		logger.trace("Sending: "+m.dump());
		transport.write(m.getBytesMessage());
		logger.debug("Sent message: "+m);
		//this.sentReply = true;
		return true;
	}
	
}
