package it.ascia.duemmegi.domino.device;

import it.ascia.ais.AISException;
import it.ascia.ais.ConnectorInterface;
import it.ascia.ais.DevicePort;
import it.ascia.ais.port.DigitalInputPort;
import it.ascia.duemmegi.domino.DominoDevice;
import it.ascia.duemmegi.dxp.DXPMessage;
import it.ascia.duemmegi.dxp.DXPResponseMessage;
import it.ascia.duemmegi.dxp.msg.RichiestaStatoIngressiMessage;
import it.ascia.duemmegi.dxp.msg.RispostaStatoIngressiMessage;

public class DF4I extends DominoDevice {

	protected int getNumInputs() {
		return 1;
	}

	public DF4I(String address, ConnectorInterface connector) throws AISException {
		super(address);
		for (int j = 0; j < getNumInputs(); j++) {
			if (j > 0) {
				connector.addDevice("i" + (intAddress + j), this);
			}
			for (int i = 1; i <= 4; i++) {
				addPort(new DigitalInputPort("i"+(intAddress+j)+"."+new Integer(i).toString()));
			}
		}		
	}

	public boolean updatePort(String portId) throws AISException {
		int i = portId.indexOf(".");
		if (i > 0) {
			RichiestaStatoIngressiMessage m = new RichiestaStatoIngressiMessage(portId.substring(1,i));
			getConnector().sendMessage(m);
			// FIXME gestire risposta qui invece che in dispatchmessage
			return true;
		} else {
			return false;
		}
	}

	public boolean sendPortValue(String portId, Object newValue)
			throws AISException {
		throw(new AISException("Gli ingressi non possono essere variati"));
	}

	public void messageSent(DXPMessage m) {
		switch (m.getMessageType()) {
			case DXPMessage.RISPOSTA_STATO_INGRESSO:
				RispostaStatoIngressiMessage r = (RispostaStatoIngressiMessage) m;
				for (int i = 1; i <= 4; i++) {
					DevicePort p = getPort("i"+((DXPResponseMessage)m).getSource()+"."+i);
					p.setCacheRetention(1000);
					p.setValue(new Boolean(r.getInputStatus(i)));
				}
				break;
			default:		
				logger.warn("Messaggio da gestire:"+m.toString());
		}
	}

}
