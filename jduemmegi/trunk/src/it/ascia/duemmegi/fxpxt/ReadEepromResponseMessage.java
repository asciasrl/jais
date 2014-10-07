package it.ascia.duemmegi.fxpxt;

public class ReadEepromResponseMessage extends FXPXTResponseMessage {

	public ReadEepromResponseMessage(int[] message) {
		load(message);
	}

	public int[] getData() {
		return dati;
	}
}
