package it.ascia.avs;

import it.ascia.ais.Message;

public class EL88Message extends Message {

	private static int CCITT_CRC_POLY = 0x1021;
	
	static int SYNC=0x36;
	
	static final int GET_STATO = 1;
	static final int SET_STATO = 2;
	static final int GET_ERROR = 3;
	static final int ASK_STATO = 6;
	static final int GET_INFO = 7;
	static final int SET_INFO = 7;
	
	static final String[] commands = {null,"GET_STATO","SET_STATO","GET_ERROR",null,null,"ASK_STATO","GET_INFO"}; 
	
	static final int SEL_STATO_ZONE_DIG = 1;
	static final int SEL_TAMPER_ZONE = 2;
	static final int SEL_BYPASS_ZONE = 3;
	static final int SEL_STATO_SETT = 5;
	static final int SEL_USCITA_OC_DIG = 6;
	static final int SEL_USCITA_RELE = 7;
	static final int SEL_USCITA_SIRENE = 8;
	static final int SEL_USCITA_TAMPER = 9;
	static final int SEL_STATO_ZONE_AN = 10;
	static final int SEL_USCITA_OC_AN = 12;
	static final int SEL_LOGIN = 20;
	static final int SEL_GENERIC = 21;
	
	static final String[] selectors_stato = {null,"SEL_STATO_ZONE_DIG","SEL_TAMPER_ZONE","SEL_BYPASS_ZONE",null,"SEL_STATO_SETT","SEL_USCITA_OC_DIG","SEL_USCITA_RELE","SEL_USCITA_SIRENE","SEL_USCITA_TAMPER",
				"SEL_STATO_ZONE_AN",null,"SEL_USCITA_OC_AN",null,null,null,null,null,null,null,
				"SEL_LOGIN","SEL_GENERIC"};	

	static final int SEL_STATO_SPENTO = 0x00;
	static final int SEL_STATO_ACCESO = 0x01;
	static final int SEL_STATO_ACCESO_HOME = 0x02;
	static final int SEL_STATO_ACCESO_AREA = 0x03;
	static final int SEL_STATO_ACCESO_PERIMETER = 0x04;

	static final int SEL_IDLE  = 0x00;
	static final int SEL_PROT_VERS = 0x01;
	
	static final String[] selectors_info = {"SEL_IDLE","SEL_PROT_VERS"};
	
	static final String[][] selectors = {null,selectors_stato, selectors_stato, selectors_stato, null, null, selectors_stato, selectors_info};

	static final int ERRORCODE_SELSTATO_UTENTE_NON_ABILIT = 0x80;
	static final int ERRORCODE_SELSTATO_ACCENSIONE_FORZATA = 0x81;
	static final int ERRORCODE_SELSTATO_ERR_AUTOTEST = 0x82;
	static final int ERRORCODE_SELSTATO_PROGR_ORARIO_ATTIVO = 0x83;

	static final int ERRORCODE_SEL_LOGIN_ERR_VALIDAZ = 0x80;
	static final int ERRORCODE_SEL_LOGIN_CADUTA_CONN_OVERFLOW = 0x81;
	static final int ERRORCODE_SEL_LOGIN_CADUTA_CONN_MANCATA_RISP = 0x82;
	static final int ERRORCODE_SEL_LOGIN_CADUTA_CONN_ERR_HNDS = 0x83;
	static final int ERRORCODE_SEL_LOGIN_CADUTA_CONN_PROT_NON_DEF = 0x84;

	static final int ERRORCODE_SEL_GENERIC_BUSY = 0x90;
	static final int ERRORCODE_SEL_GENERIC_OVERFLOW = 0x91;

	//static final int ERROR_DISPLAY_TIMEOUT = 500;                                          
	
	static final int FORMAT_0 = 0;   //formato speciale
	static final int FORMAT_1 = 1;   //elenco differ.  istanza/valore
	static final int FORMAT_2 = 2;   //elenco completo valori a partire dalla prima istanza 
	static final int FORMAT_3 = 3;   //elenco completo ist/val, con azzeramento istanze non elencate
	static final int FORMAT_4 = 4;   //elenco completo a partire dalla prima istanza (bitmap pack)
	static final int FORMAT_5 = 5;   //elenco completo istanze aperte
	static final int FORMAT_6 = 6;   //elenco diff. istanze chiuse
	static final int FORMAT_7 = 7;   //elenco diff. istanze aperte/chiuse
	static final int FORMAT_8 = 8;   //elenco diff. istanze aperte
	
	
	protected int command;
	protected int selector;
	protected int format;
	
	protected int[] data;

	// FIXME capire come va usato
	private int SeqNumber = 0;

	public EL88Message(int command, int selector, int format, int[] data) {
		this.command = command;
		this.selector = selector;
		this.format = format;
		this.data = data;
	}

	public EL88Message(int command, int selector, int format) {
		this(command, selector, format, new int[0]);
	}

	static int calcCRC(int CRC, int d) {		
		for (int i=1; i <= 8; i++) {
			if ((CRC & 0x8000) == 0x8000) { 
				CRC = (CRC << 1) & 0xFFFF;  //shift in the next message bit
				if ((d & 0x80) == 0x80)
					CRC = (CRC | 0x0001) & 0xFFFF;
				d = (d << 1) & 0xFFFF;
				 //if the CRC left-most bit is equal to 1 XOR the CRC register with the generator polynomial
				CRC = CRC ^ CCITT_CRC_POLY;
			} else {
				CRC = (CRC << 1) & 0xFFFF;  //shift in the next message bit
				if ((d & 0x80) == 0x80)
					CRC = (CRC | 0x0001) & 0xFFFF;
				
				d = (d << 1) & 0xFFFF;				
			}
		}
		return CRC;
	}

	/**
	static int updateCRC(int crc, int b) {
		return updateCRC(crc,b,CCITT_CRC_POLY);
	}
	
	static int updateCRC(int crc, int b, int polynomial) {
		 for (int i = 0; i < 8; i++) {
             boolean bit = ((b   >> (7-i) & 1) == 1);
             boolean c15 = ((crc >> 15    & 1) == 1);
             crc <<= 1;
             if (c15 ^ bit) crc ^= polynomial;
         }
		 return crc & 0xFFFF;
	}
	**/
	
	@Override
	public byte[] getBytesMessage() {
		
		int l = data.length + 7 + 2;
		int message[] = new int[l];
		
		message[0] = l - 1;		
		message[1] = SYNC;		
		message[2] = SeqNumber;
		message[3] = command;
		message[4] = 0x00;
		message[5] = selector;
		message[6] = format;
		
		for (int i = 0; i < data.length; i++) {
			message[i + 7] = data[i];
		}
		
		int CRC = 0xFFFF;
		for (int i = 0; i < message.length - 2; i++) {
			CRC = calcCRC(CRC, message[i]);
		}

		message[l - 2] = (CRC & 0xFF00) >>> 8;		
		message[l - 1] = (CRC & 0x00FF);
			
		byte[] bytesMessage = new byte[message.length];
		for (int i = 0; i < message.length; i++) {
			bytesMessage[i] = (new Integer(message[i])).byteValue();
		}
		return bytesMessage;
	}

	@Override
	public String toString() {
		String s = getMessageDescription() + " Command="+command+","+commands[command]+" Selector="+selector+","+selectors[command][selector] + " Format="+format;
		if (data.length > 0) {
			s += " Data=";
			for (int i=0; i < data.length; i++) {
				s += " " + b2h(data[i]);
			}		
		}
		return s;
	}

}
