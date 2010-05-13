package it.ascia.avs;

import java.util.ArrayList;
import java.util.List;

import it.ascia.avs.AVSMessage.Code;


public class Advance88 extends CentraleAVS {

	private int test = 0;
	private boolean protocolSelected = false;
	private boolean loginOk = false;

	private static int PROTOCOL_MAJOR = 0x01;
	private static int PROTOCOL_MINOR = 0x01;
	
	public Advance88(AVSConnector connector) {
		super(connector);
	}

	/**
	 * Ripristina le variabili di stato per iniziare una nuova comunicazione
	 */
	public void reset() {
		protocolSelected = false;
		loginOk = false;
	}
	
	@Override
	void processMessage(AVSMessage m) {
		
		test++;
		
		if (test > 20) {
			test = 0;
		}

		logger.debug("processMessage test="+test);

		if (test == 8) {
			connector.sendMessage(new AVSMessage(AVSMessage.Code.ASK_STATO_BYPASS_ZONE,AVSMessage.FORMAT_0));
			return;
		}

		if (test == 11) {
			connector.sendMessage(new AVSAskStatoZoneDigMessage());
			return;
		}

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
				protocolSelected = true;
				logger.info("Protocol selected: "+data[0]+"."+data[1]);
				doLogin();
			} else {
				selectProtocol();
			}
			
		} else if (AVSMessage.Code.GET_LOGIN.match(c)) {
			logger.info("Login OK");
			loginOk = true;
		} else if (AVSMessage.Code.GET_ERROR.match(c)) {
			logger.error(((AVSGetErrorMessage) m).getErrorDescription());
			doLogout();
		} else {
			logger.debug("Nothing to do");
		}

		// Richiede alla interfaccia di attivare il protocollo ed effettua il login
		/*
		if (! protocolSelected ) {
			selectProtocol();
		} else if (! loginOk  ) {
			doLogin();
		} else {
			connector.sendMessage(new AVSIdleMessage());
		}
		*/

	}

	/**
	 * Invia messaggio per selezione protocollo
	 */
	private void selectProtocol() {
		logger.info("Select protocol");
		int[] data = new int[2];
		data[0] = PROTOCOL_MAJOR;
		data[1] = PROTOCOL_MINOR;
		connector.sendMessage(new AVSMessage(AVSMessage.Code.SET_PROT_VERS,AVSMessage.FORMAT_0,data));		
	}

	/**
	 * Effettua il login
	 */
	private void doLogin() {
		logger.info("Do login");
		int[] data = new int[7];
		
		// FIXME rendere parametrico (da file di configurazione)
		data[0] = 0x00;
		data[1] = 0x00;
		data[2] = 0x00;
		data[3] = 0x00;
		data[4] = 0x01;
		data[5] = 0x00;
		
		data[6] = 0x1E; // settori (bit0 = settore 0, bit1= settore 1, ecc.)
		
		connector.sendMessage(new AVSMessage(AVSMessage.Code.SET_LOGIN,AVSMessage.FORMAT_0,data));		
	}

	/**
	 * Effettua il logout
	 */
	private void doLogout() {
		logger.info("Do logout");
		int[] data = new int[7];
		
		// Per logout il codice utente non importa
		data[0] = 0x00;
		data[1] = 0x00;
		data[2] = 0x00;
		data[3] = 0x00;
		data[4] = 0x00;
		data[5] = 0x00;
		
		data[6] = 0x00; // tutti i settori a 0
		
		connector.sendMessage(new AVSMessage(AVSMessage.Code.SET_LOGIN,AVSMessage.FORMAT_0,data));		
	}


}
