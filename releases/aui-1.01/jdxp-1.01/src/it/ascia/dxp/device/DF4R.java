package it.ascia.dxp.device;

import it.ascia.ais.AISException;
import it.ascia.ais.Connector;
import it.ascia.ais.DevicePort;
import it.ascia.ais.Message;
import it.ascia.dxp.DXPMessage;
import it.ascia.dxp.DominoDevice;
import it.ascia.dxp.msg.ComandoUsciteMessage;
import it.ascia.dxp.msg.RichiestaStatoUsciteMessage;
import it.ascia.dxp.msg.RispostaStatoUsciteMessage;

public class DF4R extends DominoDevice {

	public DF4R(Connector connector, String address) throws AISException {
		super(connector, address);
		for (int i = 1; i <= 4; i++) {
			addPort("o"+address+"."+new Integer(i).toString());			
		}		
	}

	public String getInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	public long updatePort(String portId) throws AISException {
		int i = portId.indexOf(".");
		if (i > 0) {
			RichiestaStatoUsciteMessage m = new RichiestaStatoUsciteMessage(portId.substring(1,i));
			getConnector().queueMessage(m);
		}
		return 100;   // TODO calcolare
	}

	public boolean sendPortValue(String portId, Object newValue)
			throws AISException {
		int i = portId.indexOf(".");
		if (i > 0) {
			int d = (new Integer(portId.substring(1,i))).intValue();
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
		}
		return false;
	}

	public void messageReceived(Message m) {
		// TODO Auto-generated method stub
		
	}

	public void messageSent(Message m) {
		switch (m.getMessageType()) {
			case DXPMessage.RISPOSTA_STATO_USCITE:
				RispostaStatoUsciteMessage r = (RispostaStatoUsciteMessage) m;
				for (int i = 1; i <= 4; i++) {
					DevicePort p = getPort("o"+getAddress()+"."+i);
					p.setCacheRetention(1000);
					p.setValue(new Boolean(r.getExitStatus(i)));
				}
				break;
			default:		
				logger.warn("Messaggio da gestire:"+m.toString());
		}
	}

}
