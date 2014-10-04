package it.ascia.duemmegi.domino.device;

import it.ascia.ais.AISException;
import it.ascia.ais.DevicePort;
import it.ascia.ais.Message;
import it.ascia.ais.port.TemperaturePort;
import it.ascia.duemmegi.domino.DominoDevice;
import it.ascia.duemmegi.fxpxt.FXPXTMessage;
import it.ascia.duemmegi.fxpxt.msg.RichiestaStatoIngressiMessage;
import it.ascia.duemmegi.fxpxt.msg.RispostaStatoIngressiMessage;

public class DFTA extends DominoDevice {

	public DFTA(String address) throws AISException {
		super(address);
		addPort(new TemperaturePort("temp"));
	}

	public String getInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean updatePort(String portId) throws AISException {
		RichiestaStatoIngressiMessage m = new RichiestaStatoIngressiMessage(getSimpleAddress());
		if (getConnector().sendMessage(m)) {
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
