package it.ascia.duemmegi.fxpxt;

import java.util.ArrayList;
import java.util.List;

public class ReadRamRequestMessage extends FXPXTRequestMessage {

	public ReadRamRequestMessage(long indirizzo, int n) {
		List<Integer> buff = new ArrayList<Integer>();
		buff.add((int)((indirizzo & 0xFF0000) >> 16));
		buff.add((int)((indirizzo & 0xFF00) >> 8));
		buff.add((int)(indirizzo & 0xFF));
		buff.add(n);
		set(0, READ_RAM, buff);				
	}

}
