package it.ascia.duemmegi.fxpxt;

import java.util.ArrayList;
import java.util.List;

public class WriteEepromRequestMessage extends FXPXTMessage {

	public WriteEepromRequestMessage(long indirizzo, int n, int[] valori) {		
		List<Integer> buff = new ArrayList<Integer>();
		buff.add((int)((indirizzo & 0xFF0000) >> 16));
		buff.add((int)((indirizzo & 0xFF00) >> 8));
		buff.add((int)(indirizzo & 0xFF));
		buff.add(n);
		for (int i = 0; i < valori.length; i++) {
			buff.add(valori[i]);			
		}		
		set(0, WRITE_EEPROM, buff);				
	}

}
