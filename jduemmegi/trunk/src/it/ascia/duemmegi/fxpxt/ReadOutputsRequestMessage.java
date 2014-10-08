package it.ascia.duemmegi.fxpxt;

import java.util.ArrayList;
import java.util.List;

public class ReadOutputsRequestMessage extends FXPXTRequestMessage {

	public ReadOutputsRequestMessage(int addr, int num) {
		if (num > 32) { 
			throw(new IndexOutOfBoundsException("Can read up to 32 modules ("+num+" > 32)"));
		}
		List<Integer> buff = new ArrayList<Integer>();
		buff.add((int)(addr & 0xFF));
		buff.add(num);
		set(0, READ_OUTPUTS, buff);				
	}
	
	public int getAddr() {
		return dati[0];
	}

	public int getNum() {
		return dati[1];
	}

}
