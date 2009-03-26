package it.ascia.dxp;

public abstract class DXPResponseMessage extends DXPMessage {

	public String getDestination() {
		return null;
	}

	public String getSource() {
		return (new Integer(indirizzo)).toString();
	}

}
