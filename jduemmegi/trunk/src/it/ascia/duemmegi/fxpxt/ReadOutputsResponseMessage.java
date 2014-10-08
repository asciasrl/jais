package it.ascia.duemmegi.fxpxt;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class ReadOutputsResponseMessage extends FXPXTResponseMessage {

	public ReadOutputsResponseMessage(int[] message) {
		load(message);
	}

	public int[] getData() {
		return dati;
	}
	
	private final int NUMPORTS = 8;
	
	public HashMap<Integer, Integer[]> getOutputs() {
		HashMap<Integer,Integer[]> buff = new LinkedHashMap<Integer,Integer[]>();
		int base = ((ReadOutputsRequestMessage)getRequest()).getAddr();
		int num = ((ReadOutputsRequestMessage)getRequest()).getNum();
		for (int i = 0; i < num; i++) {
			Integer[] ports = new Integer[NUMPORTS];
			for (int j = 0; j < NUMPORTS; j++) {
				ports[j] = dati[i*8+j];
			}
			buff.put(i+base,ports);
		}
		return buff;
	}
	
	protected void appendData(StringBuffer s) {
		HashMap<Integer,Integer[]> outputs = getOutputs();
		for (Integer addr : outputs.keySet()) {
			Integer[] values = outputs.get(addr);
			s.append(" "+addr+"=");
			for (int i = 0; i < values.length; i++) {
				if (i>0) {
					s.append(",");
				}
				s.append(values[i]);
			}
		}
	}

}
