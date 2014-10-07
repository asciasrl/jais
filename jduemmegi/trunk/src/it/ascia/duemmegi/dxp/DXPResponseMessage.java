package it.ascia.duemmegi.dxp;

public abstract class DXPResponseMessage extends DXPMessage {

	public String getSource() {
		return (new Integer(indirizzo)).toString();
	}

}
