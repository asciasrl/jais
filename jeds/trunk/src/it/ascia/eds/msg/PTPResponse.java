package it.ascia.eds.msg;

import it.ascia.ais.AISException;
import it.ascia.ais.RequestMessage;
import it.ascia.ais.ResponseMessage;

public abstract class PTPResponse extends PTPMessage implements ResponseMessage {
	
	private PTPRequest req = null;

	/**
	 * @param req the req to set
	 */
	public void setRequest(PTPRequest req) {
		this.req = req;
	}

	/**
	 * @return the req
	 */
	public PTPRequest getRequest() {
		return req;
	}
	
	@Override
	public void setRequest(RequestMessage m) {
		if (PTPRequest.class.isInstance(m)) {
			setRequest((PTPRequest)m);
		} else {
			throw(new AISException("A request for a PTPResponse can only be a PTPRequest, not a " + m.getClass()));
		}				
	}

	public boolean isResponseTo(RequestMessage m) {
		return m.isAnsweredBy(this);
	}



}
