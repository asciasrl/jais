package it.ascia.duemmegi.fxpxt;

import java.util.HashMap;

public class ReadInputsResponseMessage extends FXPXTResponseMessage {

	public ReadInputsResponseMessage(int[] message) {
		load(message);
	}

	public int[] getData() {
		return dati;
	}
	
	public HashMap<Integer, Integer[]> getInputs() {
		HashMap<Integer,Integer[]> buff = new HashMap<Integer,Integer[]>();
		int base = ((ReadInputsRequestMessage)getRequest()).getAddr();
		int num = ((ReadInputsRequestMessage)getRequest()).getNum();
		for (int i = 0; i < num; i++) {
			buff.put(i+base,new Integer[]{dati[i*8],dati[i*8+1]});
		}
		return buff;
	}
}
