package it.ascia.avs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;

import it.ascia.ais.Connector;
import it.ascia.ais.port.DigitalOutputPort;
import it.ascia.ais.port.StatePort;
import it.ascia.avs.AVSMessage.Code;

public class Advance88 extends CentraleAVS {

	//private int test = 0;
	//private boolean protocolSelected = false;
	//private boolean loginOk = false;

	private static int PROTOCOL_MAJOR = 0x01;
	private static int PROTOCOL_MINOR = 0x01;
	
	private static final int NUM_ZONE_DIG = 88;

	private static final int NUM_OC_DIG = 40;
	private static final int NUM_RELAYS = 5;
	private static final int NUM_SECTORS = 4;
	private static final String[] SectorStatus = new String[]{"off","on","home","area","perimeter"};
	private static final int NUM_SIRENS = 4;
	
	private boolean loginOk = false;
	
	/**
	 * Add the device "Advance88" with ports and add zones to connector
	 */
	public Advance88() {
		super("Advance88");
		for (int z = 1; z <= NUM_OC_DIG; z++) {
			addPort(new DigitalOutputPort("Oc"+z));
		}
		// anche settore 0
		for (int z = 0; z <= NUM_SECTORS; z++) {
			addPort(new StatePort("Sector"+z,SectorStatus));
		}
		for (int z = 1; z <= NUM_RELAYS; z++) {
			addPort(new DigitalOutputPort("Relay"+z));
		}
		for (int z = 1; z <= NUM_SIRENS; z++) {
			addPort(new DigitalOutputPort("Siren"+z));
		}
		addPort(new DigitalOutputPort("Tamper"));
		// TODO: SETT, RELE, SIRENE, TAMPER, ZONE_AN, OC_DIG, OC_AN

	}
	
	void addZones() {
		Connector connector = getConnector();
		for (int z = 1; z <= NUM_ZONE_DIG; z++) {
			connector.addDevice(new AVSZoneDigDevice("Zone" + z));
		}
		/*
		for (int z = 1; z <= NUM_ZONE_AN; z++) {
			connector.addDevice(new AVSZoneAnDevice("Zone" + z));
		}
		*/
	}

	@Override
	void processMessage(AVSMessage m) {
		
		Code c = m.getCode();
		if (AVSMessage.Code.GET_PROT_VERS.equals(c)) {
			int[] data = m.getData();
			int np = (data.length - 2) / 2;
			List<String> p = new ArrayList<String>(np);
			for (int i = 0; i < np; i++) {
				p.add(data[i*2+2]+"."+data[i*2+3]);
			}
			logger.info("Protocols: in use="+data[0]+"."+data[1]+" supported="+p.toString());
			// Il protocollo usato è quello richiesto, al prossimo poll effettua login
			if (data[0] == PROTOCOL_MAJOR && data[1] == PROTOCOL_MINOR) {
				//protocolSelected = true;
				logger.info("Protocol selected: "+data[0]+"."+data[1]);
				doLogin();
			} else {
				selectProtocol();
			}			
		} else if (AVSMessage.Code.GET_LOGIN.equals(c)) {
			AVSGetLoginMessage glm = new AVSGetLoginMessage(m);
			if (glm.isDisconnected()) {
				logger.info("Login: disconnected.");
				loginOk = false;
				sendIdle();
			} else {
				logger.info("Login: OK, User="+glm.getUser()+" Sectors="+glm.getSectors());
				loginOk = true;
				getConnector().sendMessage(new AVSAskStatoZoneDigMessage());
			}
			
		} else if (AVSMessage.Code.GET_ERROR_GENERIC.equals(c)) {
			logger.warn(new AVSGetErrorMessage(m).getErrorDescription());
			sendIdle();
		} else if (AVSMessage.Code.GET_ERROR_LOGIN.equals(c)) {
			logger.error(new AVSGetErrorMessage(m).getErrorDescription());
			sendIdle();			
		} else if (AVSMessage.Code.GET_ERROR.match(c)) {
			logger.error(new AVSGetErrorMessage(m).getErrorDescription());
			doLogout();
		} else if (AVSMessage.Code.GET_STATO_ZONE_DIG.match(c)) {
			setStatoZoneDig(m.decodeData(NUM_ZONE_DIG));
			sendIdle();
		} else if (AVSMessage.Code.GET_TAMPER_ZONE.match(c)) {
			setTamperZoneDig(m.decodeData(NUM_ZONE_DIG));
			sendIdle();
		} else if (AVSMessage.Code.GET_BYPASS_ZONE.match(c)) {
			setBypassZoneDig(m.decodeData(NUM_ZONE_DIG));
			sendIdle();
		} else if (AVSMessage.Code.GET_USCITA_OC_DIG.match(c)) {
			setUscitaOcDig(m.decodeData(NUM_OC_DIG));
			sendIdle();
		} else if (AVSMessage.Code.GET_USCITA_RELE.match(c)) {
			setUscitaRele(m.decodeData(NUM_RELAYS));
			sendIdle();
		} else if (AVSMessage.Code.GET_USCITA_SIRENE.match(c)) {
			setUscitaSirene(m.decodeData(NUM_SIRENS));
			sendIdle();
		} else if (AVSMessage.Code.GET_USCITA_TAMPER.match(c)) {
			setUscitaTamper(m.decodeData(1));
			sendIdle();
		} else if (AVSMessage.Code.GET_STATO_SETT.match(c)) {
			setSectors(m.data);
			sendIdle();
		} else if (AVSMessage.Code.GET_IDLE.match(c)) {
			if (!loginOk) {
				logger.info("Closing pending session.");
				doLogout();
			}
			// do nothing, but is OK
			// TODO aggiornare expiration di tutte le porte con valore non nullo
		} else {
			logger.warn("Unhandled message: "+m);
		}		
		
	}

	private void sendIdle() {
		getConnector().sendMessage(new AVSIdleMessage());		
	}

	/**
	 * Invia messaggio per selezione protocollo
	 */
	private void selectProtocol() {
		logger.info("Select protocol "+PROTOCOL_MAJOR+"."+PROTOCOL_MINOR);
		int[] data = new int[2];
		data[0] = PROTOCOL_MAJOR;
		data[1] = PROTOCOL_MINOR;
		getConnector().sendMessage(new AVSMessage(AVSMessage.Code.SET_PROT_VERS,AVSMessage.FORMAT_0,data));		
	}

	/**
	 * Effettua il login
	 */
	@SuppressWarnings("unchecked")
	private void doLogin() {
		logger.info("Do login");
		Connector connector = getConnector();
		HierarchicalConfiguration config = getConnector().getConfiguration();
		config.setExpressionEngine(new XPathExpressionEngine());
		String pin = config.getString("connectors/connector[name='"+connector.getName()+"']/pin","000010");
		List sectors = config.getList("connectors/connector[name='"+connector.getName()+"']/sectors", Arrays.asList("1","2","3","4"));
		getConnector().sendMessage(new AVSSetLoginMessage(pin,sectors));
	}

	/**
	 * Effettua il logout
	 */
	private void doLogout() {
		logger.info("Do logout");
		getConnector().sendMessage(new AVSSetLoginMessage("",new ArrayList<String>()));
	}

	@Override
	protected boolean sendSectorValue(Integer sector, Integer accensione) {
		int[] data = new int[2];
		data[0] = sector;
		data[1] = accensione;
		return getConnector().sendMessage(new AVSMessage(AVSMessage.Code.SET_STATO_SETT,AVSMessage.FORMAT_0,data));				
	}


}
