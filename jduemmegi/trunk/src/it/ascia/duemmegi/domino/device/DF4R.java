package it.ascia.duemmegi.domino.device;

import org.apache.commons.configuration.HierarchicalConfiguration;

import it.ascia.ais.AISException;
import it.ascia.ais.port.DigitalOutputPort;
import it.ascia.duemmegi.domino.DominoDevice;
import it.ascia.duemmegi.dxp.DXPMessage;
import it.ascia.duemmegi.dxp.msg.ComandoUsciteMessage;
import it.ascia.duemmegi.dxp.msg.RichiestaStatoUsciteMessage;

public class DF4R extends DominoDevice {

	public DF4R(HierarchicalConfiguration config) throws AISException {
		super(config);
		for (int i = 1; i <= 4; i++) {
			addPort(new DigitalOutputPort("o"+getDeviceAddress()+"."+new Integer(i).toString()));			
		}
		configDevice(config);
	}

	public boolean updatePort(String portId) throws AISException {
		int i = portId.indexOf(".");
		if (i > 0) {
			RichiestaStatoUsciteMessage m = new RichiestaStatoUsciteMessage(portId.substring(1,i));
			getConnector().sendMessage(m);
			// FIXME gestire risposta qui invece che in dispatchmessage
			return true;
		} else {
			return false;
		}
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

	public void messageReceived(DXPMessage m) {
		// TODO Auto-generated method stub
		
	}
	/**
	 * FIXME recuperare DXP 

	public void messageSent(DXPMessage m) {
		switch (m.getMessageType()) {
			case DXPMessage.RISPOSTA_STATO_USCITE:
				RispostaStatoUsciteMessage r = (RispostaStatoUsciteMessage) m;
				for (int i = 1; i <= 4; i++) {
					DevicePort p = getPort("o"+getDeviceAddress()+"."+i);
					p.setCacheRetention(1000);
					p.setValue(new Boolean(r.getExitStatus(i)));
				}
				break;
			default:		
				logger.warn("Messaggio da gestire:"+m.toString());
		}
	}
	*/
}
