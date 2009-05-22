/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.msg;

/**
 * @author Sergio
 *
 */
public class UnknowMessage extends EDSMessage {

	public int getMessageType() {
		return TipoMessaggio;
	}
	
	public String getMessageDescription() {
		return "Unknown";
	}

	public UnknowMessage(int[] message) {
		load(message);
	}
	

	
}
