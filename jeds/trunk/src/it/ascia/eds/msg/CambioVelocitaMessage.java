/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.msg;

import it.ascia.ais.AISException;

/**
 * Il transport deve cambiare velocita'.
 * 
 * @author sergio, arrigo
 */
public class CambioVelocitaMessage extends BroadcastMessage 
	{

	/**
	 * 
	 * @throws AISException
	 */
	public CambioVelocitaMessage(int Velocita)
	  throws AISException {
		TipoMessaggio = 27;
		Byte1 = (Velocita & 0x07); 
		Byte2 = 0;
	}

	public CambioVelocitaMessage(int[] message) {
		load(message);
	}

	public String getMessageDescription() {
		return "Cambio velocita'";
	}
	
	public String toString()	{
		StringBuffer s = new StringBuffer();
		s.append(super.toString());
		switch (getSpeed()) {
		case 1:
			s.append(" 1200");
			break;
		case 2:
			s.append(" 2400");
			break;
		case 3:
			s.append(" 9600");
			break;
		case 4:
			s.append(" 19200");
			break;
		default:
			s.append(" "+getSpeed() + "?");			
		}
		s.append(" baud");
		return s.toString();
	}
	
	public int getSpeed() {
		return Byte1 & 0x07;
	}

	public int getMessageType() {
		return MSG_CAMBIO_VELOCITA;
	}
}
