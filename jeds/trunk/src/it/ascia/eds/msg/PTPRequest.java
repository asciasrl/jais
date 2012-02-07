/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.msg;

import it.ascia.ais.AISException;
import it.ascia.ais.Message;
import it.ascia.ais.RequestMessage;

/**
 * @author arrigo
 *
 * Messaggi Point-to-point che si aspettano una risposta.
 */
public abstract class PTPRequest extends PTPMessage implements RequestMessage {
	
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
	
	public void setResponse(Message res) {
		if (PTPResponse.class.isInstance(res)) {
			setResponse((PTPResponse)res);
		} else {
			throw(new AISException("A rensponse to a PTPRequest can only be a PTPResponse, not a " + res.getClass()));
		}		
		
	}
	
}
