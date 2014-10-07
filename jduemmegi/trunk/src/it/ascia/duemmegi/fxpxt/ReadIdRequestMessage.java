package it.ascia.duemmegi.fxpxt;

import java.util.ArrayList;
import java.util.List;

public class ReadIdRequestMessage extends FXPXTMessage {

	public ReadIdRequestMessage(long indirizzo, int n) {
		List<Integer> buff = new ArrayList<Integer>();
		buff.add(0x49); // 'I'
		buff.add(0x44); // 'D'
		set(0, READ_ID, buff);				
	}

}
