package it.ascia.avs;

import java.util.List;

import it.ascia.ais.AISException;

public class AVSSetLoginMessage extends AVSMessage {

	public AVSSetLoginMessage(String pin, List<String> sectors) {
		super(SET_LOGIN);
		int[] data = new int[7];
		for (int i=1; i <= 6; i++) {
			if (i > pin.length()) {
				data[i-1] = 0x00;
			} else {
				int x = pin.charAt(i-1) - 0x30;
				if (x > 9 || x < 0) {
					throw(new AISException("PIN digit out of range: "+pin.charAt(i-1))); 
				}
				data[i-1] = x;
			}
		}
		for (int i=0; i < sectors.size(); i++) {
			int sec = new Integer(sectors.get(i));
			if (sec > 7 || sec < 0) {
				throw(new AISException("Sector out of range: "+sec)); 
			}
			data[6] = data[6] | (0x01 << sec);
		}
		setData(data);
	}


}
