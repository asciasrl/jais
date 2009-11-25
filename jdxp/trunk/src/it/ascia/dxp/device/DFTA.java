package it.ascia.dxp.device;

import it.ascia.ais.AISException;
import it.ascia.ais.DevicePort;
import it.ascia.ais.Message;
import it.ascia.ais.port.TemperaturePort;
import it.ascia.dxp.DXPMessage;
import it.ascia.dxp.DominoDevice;
import it.ascia.dxp.msg.RichiestaStatoIngressiMessage;
import it.ascia.dxp.msg.RispostaStatoIngressiMessage;

public class DFTA extends DominoDevice {

	public DFTA(String address) throws AISException {
		super(address);
		addPort(new TemperaturePort("temp"));
	}

	public String getInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	public long updatePort(String portId) throws AISException {
		RichiestaStatoIngressiMessage m = new RichiestaStatoIngressiMessage(getSimpleAddress());
		getConnector().sendMessage(m);
		return 100;
	}

	public boolean sendPortValue(String portId, Object newValue)
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
				DevicePort p = getPort("temp");
				p.setCacheRetention(1000);
				Double t = new Double((1.0*r.getShort() - 2730.0)/ 10.0);
				p.setValue(t);
				break;
			default:		
				logger.warn("Messaggio da gestire:"+m.toString());
		}		
	}

}
