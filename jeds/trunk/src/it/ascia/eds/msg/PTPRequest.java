/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.msg;

import it.ascia.ais.AISException;
import it.ascia.ais.RequestMessage;
import it.ascia.ais.ResponseMessage;

/**
 * @author arrigo
 *
 * Messaggi Point-to-point che si aspettano una risposta.
 */
public abstract class PTPRequest extends PTPMessage implements RequestMessage {
	
	private PTPResponse response = null;

	/**
	 * True se il messaggio ha ricevuto una risposta.
	 */
	private boolean isAnswered = false;
	
	/**
	 * @param isAnswered the isAnswered to set
	 */
	public void setAnswered(boolean isAnswered) {
		this.isAnswered = isAnswered;
	}

	/**
	 * @return the isAnswered status
	 */
	public boolean isAnswered() {
		return isAnswered;
	}


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
	
	public void setResponse(ResponseMessage res) {
		if (PTPResponse.class.isInstance(res)) {
			setResponse((PTPResponse)res);
		} else {
			throw(new AISException("A rensponse to a PTPRequest can only be a PTPResponse, not a " + res.getClass()));
		}		
		
	}
	
	public abstract boolean isAnsweredBy(PTPMessage m);
	
	public boolean isAnsweredBy(ResponseMessage m) {
		if (PTPMessage.class.isInstance(m)) {
			return isAnsweredBy((PTPMessage)m);
		} else {
			return false;
		}
	}	
	
}
