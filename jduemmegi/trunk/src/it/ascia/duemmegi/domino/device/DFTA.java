package it.ascia.duemmegi.domino.device;

import it.ascia.ais.AISException;
import it.ascia.ais.port.TemperaturePort;
import it.ascia.duemmegi.DominoDevice;
import it.ascia.duemmegi.dxp.msg.RichiestaStatoIngressiMessage;

public class DFTA extends DominoDevice {

	public DFTA(String address) throws AISException {
		super(address);
		addPort(new TemperaturePort("temp"));
	}

	public boolean updatePort(String portId) throws AISException {
		RichiestaStatoIngressiMessage m = new RichiestaStatoIngressiMessage(getDeviceAddress());
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

	/**
	 * FIXME recuperare DXP 
	public void messageSent(FXPXTMessage m) {
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
	*/

}
