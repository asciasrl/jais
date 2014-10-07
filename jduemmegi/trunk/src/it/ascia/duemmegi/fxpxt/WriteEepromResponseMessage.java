package it.ascia.duemmegi.fxpxt;

public class WriteEepromResponseMessage extends FXPXTResponseMessage {

	public WriteEepromResponseMessage(int[] message) {
		load(message);
	}

	public boolean getStatus() {
		return dati[0] == 0xFF;
	}

}
