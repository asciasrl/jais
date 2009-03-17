package it.ascia.eds.msg;

public abstract class PTPResponse extends PTPMessage {
	
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


}
