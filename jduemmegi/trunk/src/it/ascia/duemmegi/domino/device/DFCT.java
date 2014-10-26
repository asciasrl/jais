package it.ascia.duemmegi.domino.device;

import org.apache.commons.configuration.HierarchicalConfiguration;

import it.ascia.ais.AISException;
import it.ascia.ais.ConnectorInterface;
import it.ascia.ais.port.TemperaturePort;
import it.ascia.duemmegi.DominoDevice;
import it.ascia.duemmegi.dxp.DXPMessage;
import it.ascia.duemmegi.dxp.msg.RichiestaStatoIngressiMessage;

public class DFCT extends DominoDevice {

	public DFCT(String address, ConnectorInterface connector) throws AISException {
		super(address);
		int intAddress = new Integer(getDeviceAddress()).intValue();
		for (int i = intAddress; i < intAddress + 7; i++) {
			connector.addDevice((new Integer(i)).toString(), this);
		}
		addPort(new TemperaturePort("temp"));
	}

	public boolean updatePort(String portId) throws AISException {
		if (portId.equals("temp")) {
			int d = (new Integer(getDeviceAddress())).intValue() + 1;
			RichiestaStatoIngressiMessage m = new RichiestaStatoIngressiMessage(d);
			getConnector().sendMessage(m);			
			// FIXME gestire risposta qui invece che in dispatchmessage
			return true;
		} else {
			return false;
		}
	}
	
	public boolean sendPortValue(String portId, Object newValue)
			throws AISException {
		// TODO Auto-generated method stub
		return false;
	}

	public void messageReceived(DXPMessage m) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * FIXME recuperare DXP 
	public void messageSent(FXPXTMessage m) {
		switch (m.getMessageType()) {
			case DXPMessage.RISPOSTA_STATO_INGRESSO:
				RispostaStatoIngressiMessage r = (RispostaStatoIngressiMessage) m;
				int intAddress = (new Integer(((DXPResponseMessage) m).getSource())).intValue();
				int myAddress = (new Integer(getDeviceAddress())).intValue();
				if (intAddress == (myAddress + 1)) {
					DevicePort p = getPort("temp");
					p.setCacheRetention(1000);
					Double t = new Double((1.0*r.getShort() - 2730.0)/ 10.0);
					p.setValue(t);
				} else {
					// FIXME gestire altri dati
				}
				break;
			default:		
				logger.warn("Messaggio da gestire:"+m.toString());
		}		
	}
	*/
}
