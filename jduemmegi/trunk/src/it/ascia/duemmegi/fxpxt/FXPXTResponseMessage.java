package it.ascia.duemmegi.fxpxt;

import it.ascia.ais.RequestMessage;
import it.ascia.ais.ResponseMessage;


public abstract class FXPXTResponseMessage extends FXPXTMessage implements ResponseMessage {

	private FXPXTRequestMessage requestMessage;

	public String getSource() {
		return (new Integer(indirizzo)).toString();
	}

	@Override
	public void setRequest(RequestMessage requestMessage) {
		this.requestMessage = (FXPXTRequestMessage) requestMessage;				
	}

	@Override
	public RequestMessage getRequest() {
		return requestMessage;
	}

	@Override
	public boolean isResponseTo(RequestMessage requestMessage) {
		return	FXPXTRequestMessage.class.isInstance(requestMessage) &&
				((FXPXTRequestMessage)requestMessage).testChecksum() &&
				((FXPXTRequestMessage)requestMessage).indirizzo == this.indirizzo &&
				((FXPXTRequestMessage)requestMessage).codice == this.codice;				
	}

}
