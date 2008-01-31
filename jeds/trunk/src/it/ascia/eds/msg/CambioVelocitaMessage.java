/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.msg;

import it.ascia.eds.EDSException;

import java.util.Random;

/**
 * Il bus deve cambiare velocita'.
 * 
 * @author sergio, arrigo
 */
public class CambioVelocitaMessage extends BroadcastMessage
	implements MessageInterface
	{

	/**
	 * 
	 * @throws EDSException
	 */
	public CambioVelocitaMessage(int Velocita)
	  throws EDSException {
		randomizeHeaders();
		TipoMessaggio = 27;
		Byte1 = (Velocita & 0x03); 
		Byte2 = 0;
	}

	public CambioVelocitaMessage(int[] message) {
		parseMessage(message);
	}

	public String getTipoMessaggio() {
		return "Cambio velocita'";
	}
	
	public String getInformazioni()	{
		StringBuffer s = new StringBuffer();
		s.append("Timestamp: "+((Mittente & 0xFF) * 0x100 + (Destinatario & 0xFF)) +"\r\n");
		s.append("Baudrate: ");
		switch (Byte1 & 0x03) {
		case 1:
			s.append("1200");
			break;
		case 2:
			s.append("2400");
			break;
		case 3:
			s.append("9600");
			break;
		}
		s.append(" bit/sec \r\n");
		return s.toString();
	}

	public int getMessageType() {
		return MSG_CAMBIO_VELOCITA;
	}
}
