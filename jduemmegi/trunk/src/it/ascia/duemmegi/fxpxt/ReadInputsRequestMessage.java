package it.ascia.duemmegi.fxpxt;

import java.util.ArrayList;
import java.util.List;

public class ReadInputsRequestMessage extends FXPXTRequestMessage {

	public ReadInputsRequestMessage(int addr, int num) {
		if (num > 32) { 
			throw(new IndexOutOfBoundsException("Can read up to 32 modules ("+num+" > 32)"));
		}
		List<Integer> buff = new ArrayList<Integer>();
		buff.add((int)(addr & 0xFF));
		buff.add(num);
		set(0, READ_INPUTS, buff);				
	}
	
	public int getAddr() {
		return dati[0];
	}

	public int getNum() {
		return dati[1];
	}

}
