package it.ascia.dxp;

public abstract class DXPRequestMessage extends DXPMessage {

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

	public String getSource() {
		return null;
	}

	public abstract boolean isAnsweredBy(DXPMessage m);

}
