package it.ascia.avs;

import java.util.concurrent.LinkedBlockingQueue;

import it.ascia.ais.AISException;
import it.ascia.ais.Connector;
import it.ascia.ais.Message;

class AVSConnector extends Connector {

	private CentraleAVS centrale;

	private int peerSeqNumber;

	private boolean sentReply;	

	//private int mySeqNumber = 0;

	//private LinkedBlockingQueue<Message> sendQueue = new LinkedBlockingQueue<Message>();
	
	/**
	 * 
	 * @param name Nome del connettore
	 * @param interfaccia Interfaccia di comunicazione
	 * @param modello Modello della centrale connessa alla interfaccia
	 */
	AVSConnector(String name, String interfaccia, String modello) {
		super(name);		
 		if (interfaccia.equals("EasyLink")) {
 			mp = new EL88MessageParser(this);
 			if (modello.equals("Advance88")) {
 				centrale = new Advance88(this);
 			} else {
 				throw(new AISException("Control unit '"+modello+"' cannot be connected to interface '"+interfaccia+"'"));
 			}
 		} else if (interfaccia.equals("XLink")) {
 			mp = new X640MessageParser(this);
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
	protected void dispatchMessage(Message m) throws AISException {
		if (!AVSMessage.class.isInstance(m)) {
			logger.error("Cannot dispatch message of class "+m.getClass().getName());
			return;
		}
		
		sentReply = false;
		
		centrale.processMessage((AVSMessage) m);
		
		if (! sentReply) {
			sendMessage(new AVSIdleMessage());
		}
		
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
		if (sentReply) {
			logger.error("Reply message already sent in this roundtrip, cannot send "+m);
			return false;
		}
		m.setSeqNumber(getSeqNumber());
		logger.trace("Sending: "+m.dump());
		transport.write(m.getBytesMessage());
		logger.debug("Sent message: "+m);
		this.sentReply = true;
		return true;
	}
	
	/*
	public boolean queueMessage(Message m) {
		return sendQueue.offer(m);
	}
	*/
	

	/**
	 * Set sequence number received from interface
	 * @param seqNumber
	 */
	void setSeqNumber(int seqNumber) {
		this.peerSeqNumber = seqNumber;		
	}
	
	/**
	 * Calculate sequence number to be used for message to send to interface
	 * @return
	 */
	private int getSeqNumber() {
		//mySeqNumber = (mySeqNumber + 1 ) & 0x0F; 
		int n = ((peerSeqNumber & 0x0F) << 4 ) + ((peerSeqNumber & 0xF0) >> 4) + 1;  
		logger.trace("SeqNumber: peer="+Message.b2h(peerSeqNumber)+" new="+Message.b2h(n));
		return n;
	}

}
