package it.ascia.dxp.device;

import it.ascia.ais.AISException;
import it.ascia.ais.Connector;
import it.ascia.ais.DevicePort;
import it.ascia.ais.Message;
import it.ascia.ais.port.DigitalVirtualPort;
import it.ascia.dxp.DXPMessage;
import it.ascia.dxp.msg.ComandoUsciteMessage;
import it.ascia.dxp.msg.RichiestaStatoUsciteMessage;
import it.ascia.dxp.msg.RispostaStatoUsciteMessage;

public class DF4IV extends DF4I {
	
	protected int getNumVirtuals() {
		return 3;
	}
	
	public DF4IV(Connector connector, String address) throws AISException {
		super(connector,address);
		int intAddress = new Integer(address).intValue();
		for (int j = getNumInputs() ; j < getNumInputs() + getNumVirtuals(); j++) {
			connector.addDeviceAlias((new Integer(intAddress + j)).toString(), this);
			for (int i = 1; i <= 4; i++) {
				addPort(new DigitalVirtualPort("v"+(intAddress+j)+"."+new Integer(i).toString()));
			}
		}
	}

	public long updatePort(String portId) throws AISException {
		int i = portId.indexOf(".");
		if (i > 0) {
			char tipo = portId.substring(0,1).toCharArray()[0];
			if (tipo == 'i') { 
				super.updatePort(portId);
			} else if (tipo == 'v') {
				RichiestaStatoUsciteMessage m = new RichiestaStatoUsciteMessage(portId.substring(1,i));
				getConnector().sendMessage(m);
			} else {
				throw(new AISException("Porta tipo "+tipo+" non valida"));				
			}
		}
		return 100;
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
	public void messageSent(Message m) {
		switch (m.getMessageType()) {
			case DXPMessage.RISPOSTA_STATO_USCITE:
				RispostaStatoUsciteMessage r = (RispostaStatoUsciteMessage) m;
				for (int i = 1; i <= 4; i++) {
					DevicePort p = getPort("v"+m.getSource()+"."+i);
					p.setCacheRetention(1000);
					p.setValue(new Boolean(r.getExitStatus(i)));
					logger.trace("messageSent setValue "+p.getFullAddress());
				}
				break;
			case DXPMessage.RISPOSTA_STATO_INGRESSO:
				super.messageSent(m);
				break;
			default:		
				logger.warn("Messaggio da gestire:"+m.toString());
		}		
	}
}
