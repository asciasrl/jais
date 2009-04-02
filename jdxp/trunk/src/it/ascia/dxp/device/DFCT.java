package it.ascia.dxp.device;

import it.ascia.ais.AISException;
import it.ascia.ais.Connector;
import it.ascia.ais.DevicePort;
import it.ascia.ais.Message;
import it.ascia.dxp.DXPMessage;
import it.ascia.dxp.DominoDevice;
import it.ascia.dxp.msg.RichiestaStatoIngressiMessage;
import it.ascia.dxp.msg.RispostaStatoIngressiMessage;

public class DFCT extends DominoDevice {

	public DFCT(Connector connector, String address) throws AISException {
		super(connector, address);
		int intAddress = new Integer(address).intValue();
		for (int i = intAddress; i < intAddress + 7; i++) {
			connector.addDeviceAlias((new Integer(i)).toString(), this);
		}
		addPort("temp");
	}

	public String getInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	public long updatePort(String portId) throws AISException {
		if (portId.equals("temp")) {
			int d = (new Integer(getAddress())).intValue() + 1;
			RichiestaStatoIngressiMessage m = new RichiestaStatoIngressiMessage(d);
			getConnector().queueMessage(m);			
			return 100;
		}
		return 0;
	}
	
	public boolean writePort(String portId, Object newValue)
			throws AISException {
		// TODO Auto-generated method stub
		return false;
	}

	public void messageReceived(Message m) {
		// TODO Auto-generated method stub
		
	}

	public void messageSent(Message m) {
		switch (m.getMessageType()) {
			case DXPMessage.RISPOSTA_STATO_INGRESSO:
				RispostaStatoIngressiMessage r = (RispostaStatoIngressiMessage) m;
				int intAddress = (new Integer(m.getSource())).intValue();
				int myAddress = (new Integer(getAddress())).intValue();
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
}
