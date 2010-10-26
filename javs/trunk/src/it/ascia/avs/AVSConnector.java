package it.ascia.avs;

import it.ascia.ais.AISException;
import it.ascia.ais.Connector;
import it.ascia.ais.Message;

class AVSConnector extends Connector {

	private CentraleAVS centrale;

	private int peerSeqNumber;

	private Boolean sentReply = false;

	/**
	 * 
	 * @param name
	 *            Nome del connettore
	 * @param interfaccia
	 *            Interfaccia di comunicazione
	 * @param modello
	 *            Modello della centrale connessa alla interfaccia
	 */
	AVSConnector(String name, String interfaccia, String modello) {
		super(name);
		mp = new AVSMessageParser(interfaccia);
		if (((AVSMessageParser) mp).supports(modello)) {
			if (modello.equals("Advance88")) {
				centrale = new Advance88();
				addDevice(centrale);
			} else if (modello.equals("Xtream640")
					&& interfaccia.equals("XLink")) {
				centrale = new Xtream640(this);
				addDevice(centrale);
			} else {
				throw (new AISException("Unsupported control unit '" + modello + "'"));
			}
		} else {
			throw (new AISException("Control unit '" + modello + "' cannot be connected to interface '" + interfaccia + "'"));
		}
	}

	@Override
	protected long getDispatchingTimeout() {
		return 5;
	}

	@Override
	protected void dispatchMessage(Message m) {
		if (!AVSMessage.class.isInstance(m)) {
			logger.error("Cannot dispatch message of class "
					+ m.getClass().getName());
		} else {
			dispatchMessage((AVSMessage) m);
		}
	}

	/**
	 * Dispatch AVSMessage to the "centrale" connected
	 * 
	 * @param m
	 */
	private void dispatchMessage(AVSMessage m) {
		sentReply = false;
		peerSeqNumber = m.getSeqNumber();
		centrale.processMessage(m);

		synchronized (this) {
			if (!sentReply) {
				try {
					logger.trace("Waiting ...");
					wait((long) (CentraleAVS.TEMPO_RISPOSTA_HOST - 200));
				} catch (InterruptedException e) {
					logger.trace("TEMPO_RISPOSTA_HOST interrupted");
				}
			}
		}

		if (!sentReply && isRunning()) {
			logger.trace("Sending idle ...");
			sendMessage(new AVSIdleMessage());
		}
	}

	@Override
	public boolean sendMessage(Message m) {
		if (AVSMessage.class.isInstance(m)) {
			return sendMessage((AVSMessage) m);
		} else {
			logger.error("Cannot send message of class "
					+ m.getClass().getName());
			return false;
		}
	}

	public boolean sendMessage(AVSMessage m) {
		if (sentReply) {
			logger
					.error("Reply message already sent in this roundtrip, cannot send "
							+ m);
			return false;
		} else {
			synchronized (this) {
				sentReply = true;
				notify();
			}
		}
		m.setSeqNumber(((peerSeqNumber & 0x0F) << 4)
				+ ((peerSeqNumber & 0xF0) >> 4) + 1);
		logger.trace("Sending: " + m.dump());
		transport.write(m.getBytesMessage());
		logger.debug("Sent: " + m);
		return true;
	}

}
