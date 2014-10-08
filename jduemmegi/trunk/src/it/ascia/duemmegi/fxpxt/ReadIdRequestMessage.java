package it.ascia.duemmegi.fxpxt;

import it.ascia.ais.RequestMessage;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class ReadIdRequestMessage extends FXPXTRequestMessage implements RequestMessage {

	public ReadIdRequestMessage(int indirizzo) {
		List<Integer> buff = new ArrayList<Integer>();
		buff.add(0x49); // 'I'
		buff.add(0x44); // 'D'
		set(indirizzo, READ_ID, buff);				
	}

	protected void appendData(StringBuffer s) {
		s.append(" Dati=");
		byte[] id = new byte[bytes];
		for (int i=0; i<bytes; i++) {
		    id[i] = (byte) dati[i];
		}	
		try {
			s.append("'"+new String(id,"US-ASCII")+"'");
		} catch (UnsupportedEncodingException e) {
			Logger.getLogger(getClass()).error(e);
		}
	}

}
