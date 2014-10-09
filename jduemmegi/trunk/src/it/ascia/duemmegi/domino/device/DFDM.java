/**
 * 
 */
package it.ascia.duemmegi.domino.device;

import org.apache.commons.configuration.HierarchicalConfiguration;

import it.ascia.ais.AISException;
import it.ascia.ais.DevicePort;
import it.ascia.ais.port.DimmerPort;
import it.ascia.duemmegi.domino.DominoDevice;
import it.ascia.duemmegi.dxp.DXPMessage;
import it.ascia.duemmegi.dxp.msg.ComandoUsciteMessage;
import it.ascia.duemmegi.dxp.msg.RichiestaStatoUsciteMessage;

/**
 * @author sergio
 *
 */
public class DFDM extends DominoDevice {

	public DFDM(HierarchicalConfiguration config) throws AISException {
		super(config);
		addPort(new DimmerPort("o"+getDeviceAddress()+".1"));
	}

	public void messageReceived(DXPMessage m) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see it.ascia.dxp.DominoDevice#messageSent(it.ascia.ais.Message)
	 */
	/**
	 * FIXME recuperare DXP 
	public void messageSent(FXPXTMessage m) {
		switch (m.getMessageType()) {
			case DXPMessage.RISPOSTA_STATO_USCITE:
				RispostaStatoUsciteMessage r = (RispostaStatoUsciteMessage) m;
				DevicePort p = getPort("o"+getDeviceAddress()+".1");
				p.setCacheRetention(1000);
				p.setValue(new Integer(r.getExitValue()));
				break;
			default:		
				logger.warn("Messaggio da gestire:"+m.toString());
		}
	}
	*/

	public boolean updatePort(String portId) throws AISException {
		RichiestaStatoUsciteMessage m = new RichiestaStatoUsciteMessage(getDeviceAddress());
		if (getConnector().sendMessage(m)) {
			// FIXME gestire risposta qui invece che in dispatchmessage
			return true;
		} else {
			return false;
		}
	}

	public boolean sendPortValue(String portId, Object newValue)
			throws AISException {
		int valore=0;
		if (String.class.isInstance(newValue)) {
			if (((String)newValue).toLowerCase().equals("toggle")) {
				DevicePort p = getPort(portId);
				if (((Integer)p.getCachedValue()).intValue() > 0) {
					valore = 0;
				} else {
					valore = 124; // Accende all’ultimo livello memorizzato
				}
			} else {
				valore = (new Integer((String)newValue)).intValue();
			}
		} else if (Integer.class.isInstance(newValue)) {
			valore = ((Integer)newValue).intValue();
		}
		ComandoUsciteMessage m = new ComandoUsciteMessage(getDeviceAddress(),valore);
		return getConnector().sendMessage(m);
	}

}
