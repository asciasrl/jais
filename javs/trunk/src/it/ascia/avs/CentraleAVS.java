package it.ascia.avs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;

import it.ascia.ais.AISException;
import it.ascia.ais.Connector;
import it.ascia.ais.Device;
import it.ascia.ais.DevicePort;
import it.ascia.ais.port.DigitalInputPort;
import it.ascia.ais.port.DigitalOutputPort;
import it.ascia.ais.port.StatePort;

public abstract class CentraleAVS extends Device {

	protected final static int TEMPO_RISPOSTA_HOST = 2500;

	private static final String[] SectorStatus = new String[] {"on", "home", "area", "perimeter","off"};

	private boolean loginOk = false;

	public CentraleAVS(String address) throws AISException {
		super(address);
		for (int z = 1; z <= getNumOc(); z++) {
			addPort(new DigitalOutputPort("Oc" + z));
		}
		// anche settore 0
		for (int z = 0; z <= getNumSettori(); z++) {
			addPort(new StatePort("Sector" + z, SectorStatus));
		}
		addPort(new DigitalInputPort("Relay"));		
		addPort(new DigitalInputPort("Tamper"));
		addPort(new DigitalInputPort("TamperCom"));
		addPort(new DigitalInputPort("MancanzaRete"));
		addPort(new DigitalInputPort("BatteriaBassa"));
		addPort(new DigitalInputPort("BatteriaMancante"));
		addPort(new DigitalInputPort("FIRE"));
		addPort(new DigitalInputPort("INTERF"));
		addPort(new DigitalInputPort("AnomaliaPSTN"));
		addPort(new DigitalInputPort("AnomaliaGSM"));

		addPort(new DigitalInputPort("FusibileF2"));
		addPort(new DigitalInputPort("FusibileF3"));
		addPort(new DigitalInputPort("FusibileF4"));
		addPort(new DigitalInputPort("FusibileF5"));
		addPort(new DigitalInputPort("FusibileF6"));
		addPort(new DigitalInputPort("FusibileF7"));

	}

	private AbstractConfiguration getConfig() {
		return getConnector().getConfiguration();
	}
	
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
				throw (new IllegalArgumentException(
						"Invalid sector status type: "
								+ newValue.getClass().getSimpleName()));
			}
			if (sendSectorValue(new Integer(portId.substring(6)), accensione)) {
				// p.setCachedValue(newValue);
				return true;
			} else {
				return false;
			}
		} else if (portId.startsWith("Oc")) {
			DigitalOutputPort p = (DigitalOutputPort) getPort(portId);
			if (Boolean.class.isInstance(newValue)) {
				int uscita = new Integer(portId.substring(2));
				return getConnector().sendMessage(AVSSetUscitaOcDigMessage.create(uscita,(Boolean)newValue));
			} else {
				throw(new IllegalArgumentException("Invalid Digital Open Collector status type: "+newValue.getClass().getSimpleName()));				
			}
		} else {
			throw new AISException("Not implemented write to port: " + portId);
		}

	}

	// protected abstract boolean sendSectorValue(Integer integer, Integer
	// accensione);

	@Override
	public long updatePort(String portId) throws AISException {
		return 0;
	}

		
	/**
	 * 
	 * @return numero dei settori (oltre al settore 0) 
	 */
	protected int getNumSettori() {
		return getConfig().getInt("numSettori", 4);
	}

	protected int getNumOc() {
		return getConfig().getInt("numOc", 8);
	}

	/**
	 * @return Numero zone gestite dalla centrale
	 */
	protected int getNumZone() {
		return getConfig().getInt("numZone", 10);
	}
	
	private int getNumTastiere() {
		return getConfig().getInt("numTastiere", 1);
	}

	private int getNumSatelliti() {
		return getConfig().getInt("numSatelliti", 0);
	}
	
	private int getNumInseritori() {
		return getConfig().getInt("numInseritori", 0);
	}

	public void setConnector(Connector connector) {
		super.setConnector(connector);
		for (int n = 1; n <= getNumZone(); n++) {
			connector.addDevice(new AVSZoneDigDevice("Zone" + n));
		}
		for (int n = 1; n <= getNumTastiere(); n++) {
			connector.addDevice(new AVSTastieraLCDDevice("Tastiera" + n));
		}
		for (int n = 1; n <= getNumSatelliti(); n++) {
			connector.addDevice(new AVSSatelliteDevice("Satellite" + n));
		}
		for (int n = 1; n <= getNumInseritori(); n++) {
			connector.addDevice(new AVSInseritoreDevice("Inseritore" + n));
		}
	}

	/**
	 * Set status of zones listed to specified value (true = alarm / open ,
	 * false = idle / closed)
	 * 
	 * @param data
	 */
	private void setZone(int[] data) {
		for (int i = 0; i < data.length; i += 3) {
			int zone = data[i] << 8 + data[i + 1];
			AVSZoneDigDevice d = (AVSZoneDigDevice) getConnector().getDevice(
					"Zone" + zone);
			int bitmap = data[i + 2];
			d.setStato((bitmap & AVSMessage.g1_STATO) > 0);
			d.setTamper((bitmap & AVSMessage.g1_TAMPER) > 0);
			d.setBypass((bitmap & AVSMessage.g1_ESCL) > 0);
			d.setBatteria((bitmap & AVSMessage.g1_BATT_RADIO) > 0);
			d.setSopravvivenza((bitmap & AVSMessage.g1_SOPRAVV) > 0);
			d.setAntimask((bitmap & AVSMessage.g1_ANTIMASK) > 0);
			d.setAllarmiAvvenuti((bitmap & AVSMessage.g1_ALLARMI_AVVENUTI) > 0);
		}
	}

	private void setOc(int[] data) {
		for (int i = 0; i < data.length; i += 3) {
			int oc = data[i] << 8 + data[i + 1];
			getPort("Oc" + oc).setValue(data[i + 2] == 1);
		}
	}

	private void setSectors(int[] data) {
		for (int i = 0; i < data.length; i += 2) {
			int sector = data[i];
			int status = data[i + 1] & 0x07; // i bit 3-7 sono riservati
			DevicePort p = getPort("Sector" + sector);
			p.setValue(status);
		}
	}

	private void setSystem(int[] data) {
		int lunghezzaPacchetto = data.length;
		for (int i = 0; i < data.length; i += lunghezzaPacchetto) {
			lunghezzaPacchetto = data[i] & 0xF0 >> 4;
			int tipoPeriferica = data[i] & 0x0F;
			switch (tipoPeriferica) {
			case AVSMessage.PERIFERICA_CENTRALE:				
				int assorbimento = data[i + 5] << 8 + data[i + 6];
				setAssorbimento(assorbimento);
				int tensioneLineaTelefonica = data[i + 7] << 8 + data[i + 8];
				setTensioneLineaTelefonica(tensioneLineaTelefonica);
				Double credito = null;
				if (data[i+10] != 0xFF) {
					credito = data[i+10] / 100.0 + data[i+9];  
				}
				setCredito(credito);
				break;
			case AVSMessage.PERIFERICA_TAST_LCD:
				int numTastiera = data[i+1];
				// TODO Gestione stati tastiere LCD
				break;
			case AVSMessage.PERIFERICA_SATELLITE:
				int numSatellite = data[i+1];
				// TODO Gestione stati satelliti
				break;
			case AVSMessage.PERIFERICA_INSWCPU:
				int numInseritore = data[i+1];
				// TODO Gestione stati inseritori				
				break;
			default:
				logger.error("Periferica di tipo sconosciuto: " + tipoPeriferica);
				break;
			}
		}
	}

	/*
	 * protected void setUscitaRele(HashMap<Integer, Integer> hashMap) {
	 * Set<Integer> keys = hashMap.keySet(); for (Integer key : keys) {
	 * getPort("Relay"+key).setValue(hashMap.get(key) == 1); } }
	 */

	/*
	 * protected void setUscitaSirene(HashMap<Integer, Integer> hashMap) {
	 * Set<Integer> keys = hashMap.keySet(); for (Integer key : keys) {
	 * getPort("Siren"+key).setValue(hashMap.get(key) == 1); } }
	 */

	/*
	 * protected void setUscitaTamper(HashMap<Integer, Integer> hashMap) {
	 * getPort("Tamper").setValue(hashMap.get(1) == 1); }
	 */

	private void setCredito(Double credito) {
		// TODO Auto-generated method stub
		
	}

	private void setTensioneLineaTelefonica(int tensioneLineaTelefonica) {
		// TODO Auto-generated method stub
		
	}

	private void setAssorbimento(int assorbimento) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Gestisce i messaggi ricevuti dalla interfaccia
	 * 
	 * @param m
	 *            Messaggio ricevuto dalla centrale
	 */
	void processMessage(AVSMessage m) {
		int c = m.getCommand();
		if (AVSMessage.CMD_END_OP == c) {
			logger.trace("Cmd End OP");
		} else if (AVSMessage.SET_LOGIN == c) {
			logger.trace("Set Login");
		} else if (AVSMessage.GET_ERR_LOGIN == c) {
			AVSGetLoginMessage glm = new AVSGetLoginMessage(m);
			if (glm.isLogoutOk()) {
				logger.info("Login: disconnected.");
				loginOk = false;
			} else if (glm.isLoginOk()) {
				logger.info("Login: OK, User=" + glm.getUser());
				loginOk = true;
			} else if (glm.isLogoutTimeout()) {
				logger.info("Login: timeout.");
				loginOk = false;
			} else if (glm.isLogoutReset()) {
				logger.info("Login: reset.");
				loginOk = false;
			}
		} else if (AVSMessage.AGE_ZONE == c) {
			setZone(m.getData());
		} else if (AVSMessage.GET_ERR_ZONE_ESCL == c) {
			logger.warn("Errore esclusione zone");
		} else if (AVSMessage.AGE_SETT == c) {
			setSectors(m.getData());
		} else if (AVSMessage.GET_ERR_SETTORI == c) {
			logger.warn("Errore modifica stato settori");
		} else if (AVSMessage.AGE_OC == c) {
			setOc(m.getData());
		} else if (AVSMessage.GET_ERR_OC == c) {
			logger.warn("Errore impostazione Open Collector");
		} else if (AVSMessage.AGE_SYSTEM == c) {
			setSystem(m.getData());
		} else {
			logger.warn("Unhandled message: " + m);
		}
	}

	private void sendIdle() {
		getConnector().sendMessage(new AVSIdleMessage());
	}

	/**
	 * Effettua il login
	 */
	@SuppressWarnings("unchecked")
	private void doLogin() {
		logger.info("Do login");
		HierarchicalConfiguration config = getConnector().getConfiguration();
		String pin = config.getString("pin", "000010");
		List sectors = config.getList("sectors", Arrays.asList("1", "2", "3", "4"));
		getConnector().sendMessage(new AVSSetLoginMessage(pin, sectors));
	}

	/**
	 * Effettua il logout
	 */
	private void doLogout() {
		logger.info("Do logout");
		getConnector().sendMessage(
				new AVSSetLoginMessage("", new ArrayList<String>()));
	}

	@Override
	protected boolean sendSectorValue(Integer sector, Integer accensione) {
		int[] data = new int[2];
		data[0] = sector;
		data[1] = accensione;
		return getConnector().sendMessage(
				new AVSMessage(AVSMessage.Code.SET_STATO_SETT,
						AVSMessage.FORMAT_0, data));
	}

}
