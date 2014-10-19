package it.ascia.duemmegi.fxpxt;

import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;

public class ReadIdResponseMessage extends FXPXTResponseMessage {

	public ReadIdResponseMessage(int[] message) {
		load(message);
	}

	protected void appendData(StringBuffer s) {
		//s.append(" Firmware=" + ((dati[0] << 8) + dati[1]) + "." + ((dati[2] << 8) + dati[3]));
		s.append(" Firmware=" + dati[2] + "."  + dati[3] + ":" + dati[0] + "." + dati[1]);
		s.append(" Identificativo=");
		int size = 0;
		for (int i=0; i<64; i++) {
			if ((dati[i+4]) == 0) {
				break;
			} else {
				size++;
			}
		}	
		byte[] id = new byte[size];
		for (int i=0; i<size; i++) {
			id[i]=(byte) dati[i+4];
		}	

		try {
			s.append("'"+new String(id,"US-ASCII")+"'");
		} catch (UnsupportedEncodingException e) {
			Logger.getLogger(getClass()).error(e);
		}
	}

}
