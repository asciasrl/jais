package it.ascia.duemmegi.fxpxt;

import java.util.HashMap;

public class ReadOutputsResponseMessage extends FXPXTResponseMessage {

	public ReadOutputsResponseMessage(int[] message) {
		load(message);
	}

	public int[] getData() {
		return dati;
	}
	
	public HashMap<Integer, Integer[]> getOutputs() {
		HashMap<Integer,Integer[]> buff = new HashMap<Integer,Integer[]>();
		int base = ((ReadOutputsRequestMessage)getRequest()).getAddr();
		int num = ((ReadOutputsRequestMessage)getRequest()).getNum();
		for (int i = 0; i < num; i++) {
			buff.put(i+base,new Integer[]{dati[i*8],dati[i*8+1]});
		}
		return buff;
	}
}
