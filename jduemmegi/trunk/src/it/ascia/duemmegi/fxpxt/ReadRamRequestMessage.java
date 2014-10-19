package it.ascia.duemmegi.fxpxt;

import java.util.ArrayList;
import java.util.List;

public class ReadRamRequestMessage extends FXPXTRequestMessage {

	public ReadRamRequestMessage(int indirizzo, int addr, int n) {
		List<Integer> buff = new ArrayList<Integer>();
		buff.add((int)((addr & 0xFF0000) >> 16));
		buff.add((int)((addr & 0xFF00) >> 8));
		buff.add((int)(addr & 0xFF));
		buff.add(n);
		set(indirizzo, READ_RAM, buff);				
	}

}
