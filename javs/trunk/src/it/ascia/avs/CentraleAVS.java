package it.ascia.avs;


import java.util.HashMap;
import java.util.Set;

import it.ascia.ais.AISException;
import it.ascia.ais.Device;
import it.ascia.ais.DevicePort;
import it.ascia.ais.port.StatePort;

public abstract class CentraleAVS extends Device {
	
	protected final static int TEMPO_RISPOSTA_HOST = 2500;
	
	public CentraleAVS(String address) throws AISException {
		super(address);
	}

	/**
	 * Gestisce le informazioni ricevute dalla centrale
	 * @param m Messaggio ricevuto dalla centrale
	 */
	abstract void processMessage(AVSMessage m);

	@Override
	public boolean sendPortValue(String portId, Object newValue)
			throws AISException {
		if (portId.startsWith("Sector")) {
			StatePort p = (StatePort) getPort(portId);
			Integer accensione; 
			if (Integer.class.isInstance(newValue)) {
				accensione = (Integer) newValue;
			} else if (String.class.isInstance(newValue)) {
				accensione = p.getTagIndex((String) newValue);
			} else {
				throw(new IllegalArgumentException("Invalid sector status type: "+newValue.getClass().getSimpleName()));
			}
			if (sendSectorValue(new Integer(portId.substring(6)),accensione)) {
				//p.setCachedValue(newValue);
				return true;
			} else {
				return false;
			}
		} else {
			throw new AISException("Not implemented write to port: "+portId);
		}
		
	}

	protected abstract boolean sendSectorValue(Integer integer, Integer accensione);

	@Override
	public long updatePort(String portId) throws AISException {
		return 0;
	}

	/**
	 * This method MUST be called once, just after the connector have added the device 
	 */
	abstract void addZones();

	/**
	 * Set status of zones listed to specified value (true =  alarm / open , false = idle / closed) 
	 * @param hashMap
	 */
	protected void setStatoZoneDig(HashMap<Integer, Integer> hashMap) {
		Set<Integer> zones = hashMap.keySet();
		for (Integer zone : zones) {
			Device d = getConnector().getDevice("Zone" + zone);
			if (AVSZoneDigDevice.class.isInstance(d)) {
				((AVSZoneDigDevice) d).setStato(hashMap.get(zone));
			}			
		}
	}

	/**
	 * Set tamper of zones listed to specified value
	 * @param hashMap
	 */
	protected void setTamperZoneDig(HashMap<Integer, Integer> hashMap) {
		Set<Integer> zones = hashMap.keySet();
		for (Integer zone : zones) {
			Device d = getConnector().getDevice("Zone" + zone);
			if (AVSZoneDigDevice.class.isInstance(d)) {
				((AVSZoneDigDevice) d).setTamper(hashMap.get(zone));
			}			
		}
	}

	/**
	 * Set bypass of zones listed to specified value
	 * @param hashMap
	 */
	protected void setBypassZoneDig(HashMap<Integer, Integer> hashMap) {
		Set<Integer> zones = hashMap.keySet();
		for (Integer zone : zones) {
			Device d = getConnector().getDevice("Zone" + zone);
			if (AVSZoneDigDevice.class.isInstance(d)) {
				((AVSZoneDigDevice) d).setBypass(hashMap.get(zone));
			}			
		}
	}

	protected void setUscitaOcDig(HashMap<Integer, Integer> hashMap) {
		Set<Integer> ocs = hashMap.keySet();
		for (Integer oc : ocs) {
			getPort("Oc"+oc).setValue(hashMap.get(oc) == 1);
		}
	}

	protected void setSectors(int[] data) {
		for (int i = 0; i < data.length; i += 2) {
			int sector = data[i];
			int status = data[i + 1] & 0x07; // i bit 3-7 sono riservati
			DevicePort p = getPort("Sector" + sector);
			p.setValue(status);			
		}		
	}

	protected void setUscitaRele(HashMap<Integer, Integer> hashMap) {
		Set<Integer> keys = hashMap.keySet();
		for (Integer key : keys) {
			getPort("Relay"+key).setValue(hashMap.get(key) == 1);
		}
	}

	protected void setUscitaSirene(HashMap<Integer, Integer> hashMap) {
		Set<Integer> keys = hashMap.keySet();
		for (Integer key : keys) {
			getPort("Siren"+key).setValue(hashMap.get(key) == 1);
		}
	}

	protected void setUscitaTamper(HashMap<Integer, Integer> hashMap) {
		getPort("Tamper").setValue(hashMap.get(1) == 1);
	}

	

}
