package it.ascia.avs;

public class AVSBypassZoneMessage extends AVSMessage {

	public AVSBypassZoneMessage(Code code, int format, int[] data) {
		super(code, format, data);
	}

	public static AVSMessage create(int zone, boolean value) {
		int[] data = new int[1];
		data[0] = zone - 1;
		if (value) {
			return new AVSBypassZoneMessage(AVSMessage.Code.SET_BYPASS_ZONE, AVSMessage.FORMAT_8, data);
		} else {
			return new AVSBypassZoneMessage(AVSMessage.Code.SET_BYPASS_ZONE, AVSMessage.FORMAT_6, data);
		}
	}
}
