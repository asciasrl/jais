package it.ascia.avs;

import it.ascia.ais.AISException;
import it.ascia.ais.Connector;
import it.ascia.ais.Message;

public class EasyLinkConnector extends Connector {

	private EL88Message messageToSend = null;

	private CentraleAVS centrale;

	public EasyLinkConnector(String name, String modello) {
		super(name);
		mp = new EL88MessageParser(this);
		if (modello.equals("Advance88")) {
			centrale = new Advance88(this);
		} else {
			throw(new AISException("Unsupported control unit: "+modello));
		}
	}

	@Override
	protected void dispatchMessage(Message m) throws AISException {
		if (!EL88Message.class.isInstance(m)) {
			logger.error("Cannot dispatch message of class "+m.getClass().getName());
			return;
		}
		if (messageToSend == null) {
			//logger.trace("Nessun messaggio da inviare, invio IDLE.");
			//messageToSend = new EL88IdleMessage();
		}
		
		if (messageToSend != null) {
			transport.write(messageToSend.getBytesMessage());
			logger.trace("Inviato messaggio: "+messageToSend.toString());
			messageToSend = null;
		}
		
		centrale.processMessage((EL88Message) m);
	}

	@Override
	public boolean sendMessage(Message m) {
		if (!EL88Message.class.isInstance(m)) {
			logger.error("Cannot send message of class "+m.getClass().getName());
			return false;
		}
		if (messageToSend == null) {
			messageToSend = (EL88Message) m;
			logger.trace("Messaggio da inviare: "+m);
			return true;
		} else {
			logger.error("Already have a message to send: message discarded.");
			return false;
		}
	}

}
