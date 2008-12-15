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

	public boolean isBroadcast() {
		return false;
	}
	
	public String getMessageDescription() {
		return "Unknown ("+TipoMessaggio+")";
	}

	public UnknowMessage(int[] message) {
		load(message);
	}
	

	
}
