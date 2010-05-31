package it.ascia.avs;


import java.util.ArrayList;
import java.util.List;

public class AVSGetLoginMessage extends AVSMessage {

	public AVSGetLoginMessage(AVSMessage m) {
		super(m);
	}

	public AVSGetLoginMessage(int seqNumber, int session, Code code, int format, int[] data) {
		super(seqNumber, session, code, format, data);
	}

	public boolean isDisconnected() {
		return data[1] == 0x00;
	}

	public String getUser() {
		if (isDisconnected()) {
			return null;
		} else {
			return String.valueOf(data[0] + 1);
		}
	}

	public List<Integer> getSectors() {
		ArrayList<Integer> sectors = new ArrayList<Integer>();
		for (int i=0; i < 7; i++) {
			if (((data[1] >>> i) & 0x01) == 0x01) {
				sectors.add(i);
			}
		}
		return sectors;
	}

	public String toString() {
		return super.toString() + " User="+getUser()+" Sectors="+getSectors();
	}
}
