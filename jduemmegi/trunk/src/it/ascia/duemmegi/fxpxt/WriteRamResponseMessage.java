package it.ascia.duemmegi.fxpxt;

public class WriteRamResponseMessage extends FXPXTResponseMessage {

	public WriteRamResponseMessage(int[] message) {
		load(message);
	}

	public boolean getStatus() {
		return dati[0] == 0xFF;
	}

}
