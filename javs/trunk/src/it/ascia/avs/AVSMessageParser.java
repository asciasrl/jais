package it.ascia.avs;

import it.ascia.ais.AISException;
import it.ascia.ais.Message;
import it.ascia.ais.MessageParser;
import it.ascia.avs.AVSMessage.Code;

public class AVSMessageParser extends MessageParser {

	private int iBuff = 0;
	private int[] buff = new int[256]; // max message size
	private int messageLength;

	private AVSLink link; 

	public AVSMessageParser(String interfaccia) {
		if (interfaccia.equals("EasyLink")) {
			link = new AVSEasyLink();
		} else if (interfaccia.equals("XLink")) {
			link = new AVSXLink();
		} else {
			throw(new AISException("Unsupported AVS interface: " + interfaccia));
		}
		clear(); 
	}
	
	@Override
	public String dumpBuffer() {
		StringBuffer sb = new StringBuffer();
		sb.append(iBuff + " bytes:");
		for (int i = 0; i < iBuff; i++) {
			sb.append(" "+Message.b2h(buff[i]));
		}
		return sb.toString();
	}

	@Override
	public boolean isBusy() {
		return iBuff > 0;
	}
	
	private void clear() {
		iBuff = 0;
	}

	/**
	 * Shift buffer  
	 * @param n
	 */
	private void shift(int n) {
		if (n > iBuff) {
			clear();
		}
		for (int i = 0; i < n; i++) {
			buff[i] = buff[i+n];
		}
		/*
		for (int i = n; i < iBuff; i++) {
			buff[i] = -1;
		}
		*/
		iBuff -= n;
	}
	
	/**
	 * Shift buffer to skip erroneous bytes
	 */
	private void shift() {
		if (iBuff >= 2) {
			int j;
			for (j = 2; j < iBuff; j++) {
				// cerca un SYNC successivo
				if (buff[j] == AVSMessage.SYNC) {
					break;
				}
			}
			// porta il SYNC in posizione 2
			shift(j - 1);
			logger.trace("After shift: "+dumpBuffer());
		} else {
			clear();
		}
	}

	@Override
	public void push(int b) {
		b = b & 0xFF;
		buff[iBuff++] = b;
		if (valid) {
			valid = false;
		}
		if (iBuff == 2) {
			if (b == AVSMessage.SYNC) {
				messageLength = buff[0] + 1; 
			} else {
				logger.trace("Not sync: " + Integer.toHexString(b));
				shift();
				return;
			}
		} else if (iBuff == messageLength) {
			logger.trace(dumpBuffer());
			int CRC = calculateCRC();
			if ((buff[iBuff - 2] != (CRC & 0xFF00) >>> 8) || (buff[iBuff - 1] != (CRC & 0x00FF))) {
				logger.warn("CRC error: " + Message.b2h(buff[iBuff - 2]) + " "+Message.b2h(buff[iBuff - 1]) + " <> " + Message.b2h((CRC & 0xFF00) >>> 8) + " "+Message.b2h(CRC & 0x00FF));
				shift();
				return;				
			}
			message = createMessage();
			valid = message != null;
			clear();
		}
	}
	
	private int calculateCRC() {
		int CRC = 0xFFFF;
		for (int i = 0; i < iBuff - 2; i++) {
			//CRC = EasyLinkMessage.updateCRC(CRC, buff[i]);
			CRC = AVSMessage.calcCRC(CRC, buff[i]);
		}		
		return CRC;
	}

	/**
	 * Create a message using buff
	 * @return
	 */
	private AVSMessage createMessage() {
		int command = buff[3];
		int selector = buff[5];
		Code code =  null;
		try {
			code = Code.get(command, selector);
		} catch (AISException e) {
			logger.error(e);
			return null;
		}
		//  buff[0] + 1 - 7 - 2 == buff[0] - 8 = messageLength - 9
		int dataLength = messageLength - 9;
		int[] data = new int[dataLength];
		for (int i=0; i < dataLength; i++) {
			data[i] = buff[i+7];
		}
		
		return new AVSMessage(buff[2],buff[4],code, buff[6], data);
		
	}

	boolean supports(String modello) {
		return link.supports(modello);
	}	

}
