package it.ascia.duemmegi.domino.device;

import it.ascia.ais.AISException;
import it.ascia.ais.Connector;
import it.ascia.ais.DevicePort;
import it.ascia.ais.Message;
import it.ascia.ais.port.TemperaturePort;
import it.ascia.duemmegi.domino.DominoDevice;
import it.ascia.duemmegi.fxpxt.FXPXTMessage;
import it.ascia.duemmegi.fxpxt.FXPXTResponseMessage;
import it.ascia.duemmegi.fxpxt.msg.RichiestaStatoIngressiMessage;
import it.ascia.duemmegi.fxpxt.msg.RispostaStatoIngressiMessage;

public class DFCT extends DominoDevice {

	public DFCT(Connector connector, String address) throws AISException {
		super(address);
		int intAddress = new Integer(address).intValue();
		for (int i = intAddress; i < intAddress + 7; i++) {
			connector.addDeviceAlias((new Integer(i)).toString(), this);
		}
		addPort(new TemperaturePort("temp"));
	}

	public String getInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean updatePort(String portId) throws AISException {
		if (portId.equals("temp")) {
			int d = (new Integer(getSimpleAddress())).intValue() + 1;
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

	public void messageReceived(FXPXTMessage m) {
		// TODO Auto-generated method stub
		
	}

	public void messageSent(FXPXTMessage m) {
		switch (m.getMessageType()) {
			case FXPXTMessage.RISPOSTA_STATO_INGRESSO:
				RispostaStatoIngressiMessage r = (RispostaStatoIngressiMessage) m;
				int intAddress = (new Integer(((FXPXTResponseMessage) m).getSource())).intValue();
				int myAddress = (new Integer(getSimpleAddress())).intValue();
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
