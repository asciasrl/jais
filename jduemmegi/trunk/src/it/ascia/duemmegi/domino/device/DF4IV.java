package it.ascia.duemmegi.domino.device;

import it.ascia.ais.AISException;
import it.ascia.ais.ConnectorInterface;
import it.ascia.ais.port.DigitalVirtualPort;
import it.ascia.duemmegi.dxp.msg.ComandoUsciteMessage;
import it.ascia.duemmegi.dxp.msg.RichiestaStatoUsciteMessage;

public class DF4IV extends DF4I {
	
	protected int getNumVirtuals() {
		return 3;
	}
	
	public DF4IV(String address, ConnectorInterface connector) throws AISException {
		super(address,connector);
		int intAddress = new Integer(getDeviceAddress().substring(1)).intValue();
		for (int j = getNumInputs() ; j < (getNumInputs() + getNumVirtuals()); j++) {
			connector.addDevice("i" + (intAddress + j), this);
			for (int i = 1; i <= 4; i++) {
				addPort(new DigitalVirtualPort("i"+(intAddress+j)+"."+new Integer(i).toString()));
			}
			connector.addDevice("v" + (intAddress + j), this);
			for (int i = 1; i <= 4; i++) {
				addPort(new DigitalVirtualPort("v"+(intAddress+j)+"."+new Integer(i).toString()));
			}
		}
	}

	public boolean updatePort(String portId) throws AISException {
		int i = portId.indexOf(".");
		if (i > 0) {
			char tipo = portId.substring(0,1).toCharArray()[0];
			if (tipo == 'i') { 
				return super.updatePort(portId);
			} else if (tipo == 'v') {
				RichiestaStatoUsciteMessage m = new RichiestaStatoUsciteMessage(portId.substring(1,i));
				if (getConnector().sendMessage(m)) {
					// FIXME gestire risposta qui invece che in dispatchmessage
					return true;
				} else {
					return false;
				}				
			} else {
				throw(new AISException("Porta tipo "+tipo+" non valida"));				
			}
		} else {
			return false;
		}
	}
	
	public boolean sendPortValue(String portId, Object newValue)
			throws AISException {
		int i = portId.indexOf(".");
		if (i > 0) {
			int d = (new Integer(portId.substring(1,i))).intValue();
			char tipo = portId.substring(0,1).toCharArray()[0];
			if (tipo == 'v') {
				int uscita = (new Integer(portId.substring(i+1))).intValue();
				boolean attiva=false;
				if (String.class.isInstance(newValue)) {
					attiva = (new Boolean((String)newValue)).booleanValue();
				} else if (Boolean.class.isInstance(newValue)) {
					attiva = ((Boolean)newValue).booleanValue();
				}
				ComandoUsciteMessage m = new ComandoUsciteMessage(d,uscita,attiva);
				// TODO logger.trace("Writeport "+portId+" "+m.toString());
				return getConnector().sendMessage(m);
			} else {
				throw(new AISException("Porta tipo "+tipo+" non valida"));
			}
		}
		return false;
	}
	
	/*
	public void messageReceived(Message m) {
		// TODO Auto-generated method stub
		
	}
	*/
	/**
	 * FIXME recuperare DXP 

	public void messageSent(FXPXTMessage m) {
		switch (m.getMessageType()) {
			case DXPMessage.RISPOSTA_STATO_USCITE:
				RispostaStatoUsciteMessage r = (RispostaStatoUsciteMessage) m;
				for (int i = 1; i <= 4; i++) {
					DevicePort p = getPort("v"+((DXPResponseMessage) m).getSource()+"."+i);
					p.setCacheRetention(1000);
					p.setValue(new Boolean(r.getExitStatus(i)));
					logger.trace("messageSent setValue "+p.getAddress());
				}
				break;
			case DXPMessage.RISPOSTA_STATO_INGRESSO:
				super.messageSent(m);
				break;
			default:		
				logger.warn("Messaggio da gestire:"+m.toString());
		}		
	}
	*/
}
