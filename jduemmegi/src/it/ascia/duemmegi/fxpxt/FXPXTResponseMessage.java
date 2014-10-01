package it.ascia.duemmegi.fxpxt;


public abstract class FXPXTResponseMessage extends FXPXTMessage {

	public String getSource() {
		return (new Integer(indirizzo)).toString();
	}

}
