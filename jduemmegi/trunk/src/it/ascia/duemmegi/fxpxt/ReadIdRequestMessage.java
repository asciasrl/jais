package it.ascia.duemmegi.fxpxt;

import it.ascia.ais.RequestMessage;

import java.util.ArrayList;
import java.util.List;

public class ReadIdRequestMessage extends FXPXTRequestMessage implements RequestMessage {

	public ReadIdRequestMessage(long indirizzo) {
		List<Integer> buff = new ArrayList<Integer>();
		buff.add(0x49); // 'I'
		buff.add(0x44); // 'D'
		set(0, READ_ID, buff);				
	}

}
