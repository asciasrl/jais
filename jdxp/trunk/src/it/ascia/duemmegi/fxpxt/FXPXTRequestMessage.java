package it.ascia.duemmegi.fxpxt;


public abstract class FXPXTRequestMessage extends FXPXTMessage {

	private boolean isAnswered;
	
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

	public abstract boolean isAnsweredBy(FXPXTMessage m);

}
