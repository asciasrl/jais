package it.ascia.dxp;

import it.ascia.dxp.msg.*;

import org.apache.log4j.Logger;

public class DXPMessageParser {

	private int[] buff;

	private int ibuff;

	private boolean valid;

	private DXPMessage message;

	/**
	 * Il nostro logger.
	 */
	private Logger logger;
	
	public DXPMessageParser() {
		logger = Logger.getLogger(getClass());
		clear();
	}

	public String dumpBuffer() {
		StringBuffer s = new StringBuffer();
		s.append("i="+ibuff+" ");
		s.append("STX:0x"+Integer.toHexString(buff[0])+" ");
		s.append("FUN:0x"+Integer.toHexString(buff[1])+" ");
		s.append("TYP:0x"+Integer.toHexString(buff[2])+" ");
		s.append("ADD:0x"+Integer.toHexString(buff[3])+" ");
		s.append("BY1:0x"+Integer.toHexString(buff[4])+" ");
		s.append("BY2:0x"+Integer.toHexString(buff[5])+" ");
		s.append("CHK:0x"+Integer.toHexString(buff[6])+" ");
		return s.toString();
	}

	/**
	 * Metodo factory per creare messaggi.
	 * 
	 * @param message sequenza di byte che compone il messaggio
	 * @return messaggio decodificato
	 */
	private DXPMessage createMessage(int[] message) {
		switch (message[2]) {
			case DXPMessage.ERRORE : 
				return new ErroreMessage(message);
			case DXPMessage.COMANDO_USCITE : 
				return new ComandoUsciteMessage(message);
			case DXPMessage.RICHIESTA_STATO_INGRESSO : 
				return new RichiestaStatoIngressiMessage(message);
			case DXPMessage.RISPOSTA_STATO_INGRESSO : 
				return new RispostaStatoIngressiMessage(message);
			case DXPMessage.RICHIESTA_STATO_USCITE : 
				return new RichiestaStatoUsciteMessage(message);
			case DXPMessage.RISPOSTA_STATO_USCITE : 
				return new RispostaStatoUsciteMessage(message);
			default: 
				logger.error("Messaggio sconosciuto:"+dumpBuffer());
				return null;
		}
	}
		
	private void clear() {
		ibuff = 0;
		buff = new int[7];
		valid = false;
		message = null;		
	}


	public void push(int b) {
		b = b & 0xFF;  // FIXME
		if (ibuff >= 7) {
			clear();
		}
		buff[ibuff++] = b;
		// verifica che il primo byte sia Start, altrimenti lo scarta
		if (ibuff == 1) {
			if (b != DXPMessage.Start) {
				ibuff = 0;
				logger.warn("Non Sart:" + b);
			}
		} else if (ibuff == 7) {
			// verifica che il settimo byte sia checksum valido
			int chk = 0;
			for (int i = 0; i < 6; i++) {
				chk = (chk + buff[i] & 0xFF) & 0xFF;
			}
			if (chk != b) {
				logger.warn("Errore checksum");
				logger.debug(dumpBuffer());
				clear();
			} else {
				message = createMessage(buff);
				if (message == null) {
					clear();
				} else {
					valid = true;
				}
			}
		}
	}

	public boolean isValid() {
		return valid;
	}

	public DXPMessage getMessage() {
		return message;
	}


}
