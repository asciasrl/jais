package it.ascia.duemmegi.fxpxt;

public class ReadRamResponseMessage extends FXPXTMessage {

	public ReadRamResponseMessage(int[] message) {
		load(message);
	}

	public int[] getData() {
		return dati;
	}

}
