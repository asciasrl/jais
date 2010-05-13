package it.ascia.avs;

import it.ascia.ais.AISException;
import it.ascia.ais.Message;
import it.ascia.ais.MessageParser;
import it.ascia.avs.AVSMessage.Code;

public class EL88MessageParser extends MessageParser {

	private int iBuff = 0;
	private int[] buff = new int[256]; // max message size
	private int messageLength;

	//EL88Message m = null;
		
	//int test = 0;
	
	private AVSConnector connector;
	
	public EL88MessageParser(AVSConnector connector) {
		this.connector = connector;
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
		// TODO Auto-generated method stub
		return false;
	}
	
	private void clear() {
		/*
		for (int i = 1; i <= iBuff; i++) {
			buff[i - 1] = -1;
		}	
		*/			
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
	
	private void shift() {
		if (iBuff >= 2) {
			int j;
			for (j = 2; j < iBuff; j++) {
				// cerca un SYNC successivo
				if (buff[j] == AVSMessage.SYNC) {
					break;
				}
			}			
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
		//logger.trace(iBuff+"="+Message.b2h(b));
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

	private AVSMessage createMessage() {
		int messageLength = buff[0] + 1;
		int seqNumber =  buff[2];
		connector.setSeqNumber(seqNumber);
		int command = buff[3];
		@SuppressWarnings("unused")
		int session = buff[4]; // Non usato
		int selector = buff[5];
		Code code =  null;
		try {
			code = Code.get(command, selector);
		} catch (AISException e) {
			logger.error(e);
		}
		int format = buff[6];
		int dataLength = messageLength - 7 - 2;
		int[] data = new int[dataLength];
		//String dataString = new String();
		StringBuffer sb = new StringBuffer();
		//StringBuffer sbS = new StringBuffer();
		for (int i=0; i < dataLength; i++) {
			data[i] = buff[i+7];
			sb.append(" "+Message.b2h(buff[i+7]));
			//sbS.append((char)data[i]);
		}
		//dataString = sbS.toString();
		//String sn = ((seqNumber >> 4) & 0x0F) + "/" + (seqNumber & 0x0F);  
		//logger.debug("Length="+messageLength+" Seq="+sn+" Command="+command+","+AVSMessage.commands[command]+" Selector="+selector+","+AVSMessage.selectors[command][selector]+" Format="+format+" Data="+dataLength+","+sb.toString());
		
		if (AVSMessage.Code.GET_ERROR.match(command)) { 
			return new AVSGetErrorMessage(seqNumber,code, format, data);
		} else {
			return new AVSMessage(seqNumber,code, format, data);
		}
		
		
	}

}
