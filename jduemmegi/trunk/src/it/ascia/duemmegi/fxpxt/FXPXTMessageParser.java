package it.ascia.duemmegi.fxpxt;

import it.ascia.ais.Message;
import it.ascia.ais.MessageParser;

public class FXPXTMessageParser extends MessageParser {

	private int[] buff;

	private int ibuff;

	/**
	 * Lunghezza del messaggio atteso
	 */
	private int length;

	private boolean valid;

	private FXPXTMessage message;

	private long lastReceived;
	
	private static final long TIMEOUT = 1000;
		
	public FXPXTMessageParser() {
		super();
		clear();
	}

	public String dumpBuffer() {
		StringBuffer s = new StringBuffer();
		s.append("i="+ibuff+" ");
		s.append("ADDR:0x"+Integer.toHexString(buff[0])+" ");
		s.append("CODE:0x"+Integer.toHexString(buff[1])+" ");
		s.append("LENG:0x"+Integer.toHexString(buff[2])+" ");
		for (int i = 3; i < (ibuff - 2); i++) {
			s.append("DATA:0x"+Integer.toHexString(buff[i])+" ");			
		}
		s.append("CHKH:0x"+Integer.toHexString(buff[ibuff-2])+" ");
		s.append("CHKL:0x"+Integer.toHexString(buff[ibuff-1])+" ");
		return s.toString();
	}

	/**
	 * Metodo factory per creare oggetti messagge in base ai byte ricevuti.
	 * 
	 * @param message sequenza di byte che compone il messaggio
	 * @return messaggio decodificato
	 */
	private FXPXTMessage createMessage(int[] message) {
		switch (message[1]) {
			case FXPXTMessage.READ_RAM : 
				return new ReadRamResponseMessage(message);
			case FXPXTMessage.WRITE_RAM : 
				return new WriteRamResponseMessage(message);
			case FXPXTMessage.READ_EEPROM : 
				return new ReadEepromResponseMessage(message);
			case FXPXTMessage.WRITE_EEPROM : 
				return new WriteEepromResponseMessage(message);
			case FXPXTMessage.READ_OUTPUTS : 
				return new ReadOutputsResponseMessage(message);
			case FXPXTMessage.READ_INPUTS : 
				return new ReadInputsResponseMessage(message);
			case FXPXTMessage.WRITE_OUTPUT : 
				return new WriteOutputResponseMessage(message);
			case FXPXTMessage.WRITE_VIRTUAL : 
				return new WriteVirtualResponseMessage(message);
			case FXPXTMessage.READ_ID : 
				return new ReadIdResponseMessage(message);
			default: 
				logger.error("Messaggio sconosciuto:"+dumpBuffer());
				return null;
		}
	}
		
	private void clear() {
		ibuff = 0;
		length = 5;
		buff = new int[260];
		valid = false;
		message = null;		
	}


	public void push(int b) {
		b = b & 0xFF;  // FIXME
		if (ibuff >= length) {
			clear();
		}
		if (ibuff > 0 && ! valid && (System.currentTimeMillis() - lastReceived) >= TIMEOUT ) {
			logger.warn("Timeout in ricezione");
			clear();
		}
		lastReceived = System.currentTimeMillis();		
		buff[ibuff++] = b;
		if (ibuff == 3) {
			// imposta la nuova lunghezza attesa del messaggio
			length = b + 5;
		} else if (ibuff == length) {
			message = createMessage(buff);
			if (message != null) {
				if (message.testChecksum()) {
					valid = true;
				} else {
					logger.warn("Errore checksum ("+message.calculateChecksum()+") "+dumpBuffer());
					clear();
				}
			}
		}
	}

	public boolean isValid() {
		return valid;
	}

	public Message getMessage() {
		return message;
	}

	public boolean isBusy() {
		return  (ibuff > 0 && ! valid && (System.currentTimeMillis() - lastReceived) < TIMEOUT);
	}


}
