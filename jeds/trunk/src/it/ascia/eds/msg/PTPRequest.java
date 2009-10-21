/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.msg;

/**
 * @author arrigo
 *
 * Messaggi Point-to-point che si aspettano una risposta.
 */
public abstract class PTPRequest extends PTPMessage {
	
	public abstract boolean isAnsweredBy(PTPMessage m);
	
	private PTPResponse response = null;

	/**
	 * @param response the response to set
	 */
	public void setResponse(PTPResponse response) {
		this.response = response;
	}

	/**
	 * @return the response
	 */
	public PTPResponse getResponse() {
		return response;
	}
	
}
