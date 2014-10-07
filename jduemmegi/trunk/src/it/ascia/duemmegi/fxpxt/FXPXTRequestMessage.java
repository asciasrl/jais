package it.ascia.duemmegi.fxpxt;

import it.ascia.ais.RequestMessage;
import it.ascia.ais.ResponseMessage;


public abstract class FXPXTRequestMessage extends FXPXTMessage implements RequestMessage {

	private boolean isAnswered;
	
	private FXPXTResponseMessage responseMessage;
	
	/**
	 * @param isAnswered the isAnswered to set
	 */
	public void setAnswered(boolean isAnswered) {
		this.isAnswered = isAnswered;
	}

	/**
	 * @return the isAnswered
	 */
	public boolean isAnswered() {
		return isAnswered;
	}

	public String getDestination() {
		return (new Integer(indirizzo)).toString();
	}

	@Override
	public void setResponse(ResponseMessage res) {
		responseMessage = (FXPXTResponseMessage) res;		
	}

	@Override
	public ResponseMessage getResponse() {
		return responseMessage;
	}

	@Override
	public boolean isAnsweredBy(ResponseMessage m) {
		return	FXPXTResponseMessage.class.isInstance(m) &&
				((FXPXTResponseMessage)m).testChecksum() &&
				((FXPXTResponseMessage)m).indirizzo == this.indirizzo &&
				((FXPXTResponseMessage)m).codice == this.codice;				
	}

}
