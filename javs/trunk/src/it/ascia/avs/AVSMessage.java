package it.ascia.avs;

import java.util.HashMap;

import it.ascia.ais.AISException;
import it.ascia.ais.Message;

public class AVSMessage extends Message {

	private static int CCITT_CRC_POLY = 0x1021;
	
	static int SYNC=0x36;	
		
	enum Code {
		/**
		 * Comunicazione stato da centrale ad host
		 */
		GET_INFO(3,-1),
		GET_STATO_ZONE_DIG(1,1), 
		GET_TAMPER_ZONE(1,2),
		GET_BYPASS_ZONE(1,3), 
		GET_ANOMALIA_ZONE_RADIO(1,4),
		GET_STATO_SETT(1,5),
		GET_USCITA_OC_DIG(1,6),
		GET_USCITA_RELE(1,7),
		GET_USCITA_SIRENE(1,8),
		GET_USCITA_TAMPER(1,9),
		GET_STATO_ZONE_AN(1,10),
		GET_USCITA_OC_AN(1,12),
		GET_STATO_TAMPER(1,11),
		GET_STATO_USER(1,13),
		GET_LOGIN(1,20),
		
		/**
		 * Modifica stato da host a centrale
		 */
		 // era 2: errore di Presti o di Lissandrini ?
		SET_BYPASS_ZONE(1,3,true),
		SET_STATO_SETT(1,5,true),
		SET_LOGIN(1,20,true),
		
		/**
		 * Comunicazione errori da centrale ad host
		 */
		GET_ERROR(3,-1),
		GET_ERROR_BYPASS_ZONE(3,3),
		GET_ERROR_STATO_SETT(3,5),
		GET_ERROR_USCITA_OC_DIG(3,6),
		GET_ERROR_USCITA_OC_AN(3,12),
		GET_ERROR_LOGIN(3,20),
		GET_ERROR_GENERIC(3,21),
		
		/**
		 * Richiesta informazioni da host a centrale
		 * (non implementato)
		 */
		//ASK_INFO(5),

		/**
		 * Richiesta stato da host a centrale
		 */
		ASK_STATO_ZONE_DIG(6,1,true),
		ASK_STATO_ZONE_AN(6,10,true),
		ASK_STATO_BYPASS_ZONE(6,3,true),
		
		/**
		 * Polling da centrale ad host
		 */
		GET_IDLE(7,0),
		GET_PROT_VERS(7,1),
		
		/**
		 * Riscontro da host a centrale
		 */
		SET_IDLE(7,0, true),
		SET_PROT_VERS(7,1, true);
		

		private int command;
		private int selector;
		private boolean host;
				
		Code(int command, int selector) {
			this(command,selector,false);
		}

		Code(int command, int selector, boolean host) {
			this.command = command;
			this.selector = selector;
			this.host = host;
		}
		
		public int getCommand() {
			return command;
		}
		
		public int getSelector() {
			return selector;
		}

		private boolean getHost() {
			return host;
		}

		/**
		 * Match if command and host are equal
		 * don't care of selector if supplied selector is < 0
		 * @param command
		 * @param selector
		 * @param host
		 * @return
		 */
		public boolean match(int command, int selector, boolean host) {
			if (selector < 0) {
				return match(command, host);
			} else {
				return this.command == command && this.selector == selector && this.host == host;
			}
		}

		/**
		 * Match if command is equal (don't care of selector), assume not host message
		 * @param command
		 * @return
		 */
		public boolean match(int command) {
			return match(command,false);
		}

		/**
		 * Match if command and host are equal (don't care of selector)
		 * @param command
		 * @param host
		 * @return
		 */
		public boolean match(int command, boolean host) {
			return this.command == command && this.selector == -1 && this.host == host;
		}
		
		public boolean match(Code arg) {
			return match(arg.getCommand(),arg.getSelector(),arg.getHost());
		}

		

		/**
		 * Search for a code, assuming message is generated by the interface
		 * @param command
		 * @param selector
		 * @return
		 */
		public static Code get(int command, int selector) {
			return get(command,selector,false);
		}
		
		/**
		 * Search for a code that match given command and selector
		 * @param command
		 * @param selector
		 * @param host True if message is generated by the host
		 * @return
		 */
		public static Code get(int command, int selector, boolean host) {
			if (selector < 0) {
				for (Code code : values()) {
					if (code.match(command,host)) {
						return code;
					}
				}
			} else {
				for (Code code : values()) {
					if (code.match(command,selector,host)) {
						return code;
					}
				}				
			}
			throw(new AISException("Unknow code: "+command+"/"+selector+"/"+host));
		}

		public boolean match(int command, int selector) {
			return match(command,selector,false);
		}

		public boolean isHost() {
			return host;
		}
	}
		
	static final int FORMAT_0 = 0;   //formato speciale
	static final int FORMAT_1 = 1;   //elenco differ.  istanza/valore
	static final int FORMAT_2 = 2;   //elenco completo valori a partire dalla prima istanza 
	static final int FORMAT_3 = 3;   //elenco completo ist/val, con azzeramento istanze non elencate
	static final int FORMAT_4 = 4;   //elenco completo a partire dalla prima istanza (bitmap pack)
	static final int FORMAT_5 = 5;   //elenco completo istanze aperte
	static final int FORMAT_6 = 6;   //elenco diff. istanze chiuse
	static final int FORMAT_7 = 7;   //elenco diff. istanze aperte/chiuse
	static final int FORMAT_8 = 8;   //elenco diff. istanze aperte
	
	
	private int seqNumber;
	private int session;

	private int format;
	
	protected int[] data;

	private Code code;

	public AVSMessage(int seqNumber, int session, Code code, int format, int[] data) {
		if (code == null) {
			throw(new AISException("Code cannot be null"));
		}
		this.seqNumber = seqNumber;
		this.session = session;
		this.code = code;
		this.format = format;
		this.data = data;
	}

	public AVSMessage(AVSMessage m) {
		this(m.getSeqNumber(),m.getSession(),m.getCode(),m.getFormat(),m.getData());
	}

	int getFormat() {
		return this.format;
	}

	public AVSMessage(Code code, int format) {
		this(0, 0, code, format, new int[0]);
	}

	public AVSMessage(Code code, int format, int[] data) {
		this(0, 0, code, format, data);
	}

	int getSeqNumber() {
		return this.seqNumber;
	}

	/**
	 * @return the command
	 */
	int getCommand() {
		return code.getCommand();
	}

	/**
	 * @return the selector
	 */
	int getSelector() {
		return code.getSelector();
	}
	
	Code getCode() {
		return code;
	}

	/**
	 * @return the data
	 */
	int[] getData() {
		return data;
	}

	/**
	 * Must be called by the Connector before getBytesMessage()
	 * @param seqNumber
	 */
	void setSeqNumber(int seqNumber) {
		this.seqNumber = seqNumber;
	}

	int getSession() {
		return this.session;		
	}

	void setData(int[] data) {
		this.data = data;
	}

	/**
	<pre>
	AVS Electronics S.p.A. - Giugno 2006
	Esempi di implementazione di CRC16 (CCITT) utilizzato nel protocollo EL
	
	Sono riportate tre implementazioni di CRC16
	calc_avs_ccitt_CRC: esegue il calcolo senza utilizzo di tabelle esterne. Occupa meno spazio
	(codice+dati statici) ma richiede piu' risorse di CPU
	CRCCCITTa, CRCCCITTb: equivalenti tra loro, eseguono il calcolo con supporto di una tabelle esterna.
	Occupa piu' spazio (codice+dati statici) ma richiede meno risorse di CPU
	
	Le tre implementazioni danno risultati identici.
	Il polinomio utilizzato e' 0x1021 (g(x) = x16 + x12 + x5 + 1)
	Il valore CRC iniziale e' 0xFFFF. Rispetto al piu' classico 0, questo valore consente di
	differenziare il crc anche se i primi bytes sono nulli.
	
	Per garantire portabilita' in ambienti diversi, si evidenzia che le variabili "unsigned short" sono
	interi a 16 bit senza segno.
	
	------------------------------ implementazione 1: calcolo del CRC bit per bit
	1) CRC iniziale: 0xFFFF
	2) if the CRC left-most bit is equal to 1, shift in the next message bit, and XOR the CRC
	register with the generator polynomial; otherwise, only shift in the next message bit
	3) Repeat step 2 until all bits of the augmented message have been shifted in
	|b15..............b0| |b7.................b0|
	 <------ CRC ------>   <---- input bits---->
	</pre>
	*/
	protected static int calcCRC(int CRC, int d) {		
		for (int i=0; i < 8; i++) {
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

	@Override
	public byte[] getBytesMessage() {
		int l = data.length + 7 + 2;
		int message[] = new int[l];
		
		message[0] = l - 1;		
		message[1] = SYNC;		
		message[2] = seqNumber;
		message[3] = getCommand();
		message[4] = 0x00;
		message[5] = getSelector();
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
		String s = getMessageDescription() 
			+ " Sequence="+((seqNumber >> 4) & 0x0F) + "/" + (seqNumber & 0x0F)  
			+ " Code=" + code + " ("+code.getCommand()+"/"+code.getSelector()+"/"+(code.isHost()?"host":"interface")+")"
            + " Format=" + format;
		if (data.length > 0) {
			s += " Data="+data.length;			
			for (int i=0; i < data.length; i++) {
				s += " " + b2h(data[i]);
			}		
		}
		return s;
	}

	String dump() {
		StringBuffer sb = new StringBuffer();
		byte[] m = getBytesMessage(); 
		sb.append(m.length + " bytes:");
		for (int i = 0; i < m.length; i++) {
			sb.append(" "+Message.b2h(m[i]));
		}
		return sb.toString();
	}
	
	/**
	 * 
	 * If full == true, create a full list with the specified number of items, all items not specified in data have status negated
	 * 
	 * @param full Data contains full list of items
	 * @param status Status of listed items
	 * @param numItems Number of items
	 * @param bitmap Data is bitmap 
	 * @param data 
	 * @return
	 */
	HashMap<Integer,Integer> decodeData(int numItems) {
		HashMap<Integer,Integer> res = new HashMap<Integer,Integer>(numItems);
		
		if (format == 2 || format == 3 || format == 4 || format == 5) {
			// elenco completo
			for (int z = 1; z <= numItems; z++) {
				res.put(z,0);
			}
		}
		for (int i = 0; i < data.length; i++) {
			if (format == 1 || format == 3) {
				// coppia istanza-stato
				res.put(data[i] + 1, data[i + 1]);
				i++;
			} else if (format == 2) {
				// lista ordinata stato
				res.put(i + 1, data[i]);
			} else if (format == 4) {
				// codifica bitmap istanze aperte
				for (int j = 0; j < 8; j++) {
					if ((data[i] & ( 1 << j)) > 0) {
						res.put(i * 8 + j + 1, 1);
					}
				}
			} else if (format == 5 || format == 8) {
				// lista numeri istanze aperte
				res.put(data[i] + 1, 1);
			} else if (format == 6) {
				// lista differenziale istanze chiuse
				res.put(data[i] + 1, 0);
			} else if (format == 7) {
				// lista differenziale combinata: terzo stato (switch)
				res.put(data[i] + 1, -1);
			}
		}
		return res;
	}



}
