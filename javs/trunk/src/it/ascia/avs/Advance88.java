package it.ascia.avs;

import it.ascia.ais.Connector;

public class Advance88 extends CentraleAVS {

	private int test = 0;

	public Advance88(Connector connector) {
		super(connector);
	}

	@Override
	void processMessage(EL88Message m) {
		
		logger.debug("processMessage test="+test);
		
		switch (m.command) {
			case EL88Message.GET_INFO:
				switch (m.selector) {
					case EL88Message.SEL_PROT_VERS:
						int[] protVersion = new int[2];
						if (m.data[4] != 0xFF && m.data[5] != 0xFF) {
							protVersion[0] = m.data[4];
							protVersion[1] = m.data[5];
						} else {
							protVersion[0] = m.data[2];
							protVersion[1] = m.data[3];
						}
						logger.info("Versione protocollo= "+protVersion[0]+"."+protVersion[1]);
												
					default:
						break;
				}				
				break;
	
			case EL88Message.GET_ERROR:
				logger.error(((EL88ErrorMessage) m).getErrorDescription());
				break;
				
			default:
				break;
		} // FINE switch (command)

		if (test == 1) {
			int[] data = new int[2];
			data[0] = 0x01;
			data[1] = 0x01;
			connector.sendMessage(new EL88Message(EL88Message.SET_INFO,EL88Message.SEL_PROT_VERS,EL88Message.FORMAT_0,data));
		}
		
		if (test == 4) {
			
			int[] data = new int[7];
			data[0] = 0x00;
			data[1] = 0x00;
			data[2] = 0x00;
			data[3] = 0x00;
			data[4] = 0x01;
			data[5] = 0x00;
			
			data[6] = 0x01; // settori
			
			connector.sendMessage(new EL88Message(EL88Message.SET_STATO,EL88Message.SEL_LOGIN,EL88Message.FORMAT_0,data));
		}

		if (test == 8) {
			connector.sendMessage(new EL88Message(EL88Message.ASK_STATO,EL88Message.SEL_BYPASS_ZONE,EL88Message.FORMAT_0));
		}

		if (test == 11) {
			connector.sendMessage(new EL88AskStatoZoneDigMessage());
		}

		test++;
		
		if (test > 20) {
			test = 0;
		}
	}

}
