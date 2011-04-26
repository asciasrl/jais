package it.ascia.avs;

public class AVSSetUscitaOcDigMessage extends AVSMessage {

	public AVSSetUscitaOcDigMessage(Code code, int format, int[] data) {
		super(code, format, data);
	}

	public static AVSMessage create(int oc, boolean value) {
		int[] data = new int[1];
		data[0] = oc - 1;
		if (value) {
			return new AVSBypassZoneMessage(AVSMessage.Code.SET_USCITA_OC_DIG, AVSMessage.FORMAT_8, data);
		} else {
			return new AVSBypassZoneMessage(AVSMessage.Code.SET_USCITA_OC_DIG, AVSMessage.FORMAT_6, data);
		}
	}
}
