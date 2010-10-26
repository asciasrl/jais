package it.ascia.avs;

import java.util.HashMap;

import it.ascia.ais.Message;

public class AVSMessage extends Message {

	private final static int CCITT_CRC_POLY = 0x1021;
	
	static int SYNC=0x36;	
		
	private int seqNumber;
	
	private int session;

	protected int[] data;

	private int command;
	
	final static int BIT0 = 1; 
	final static int BIT1 = 1 << 1; 
	final static int BIT2 = 1 << 2; 
	final static int BIT3 = 1 << 3; 
	final static int BIT4 = 1 << 4; 
	final static int BIT5 = 1 << 5; 
	final static int BIT6 = 1 << 6; 
	final static int BIT7 = 1 << 7;
	
	final static int CMD_ASK = 0x61;
	final static int CMD_END_OP = 0x54;
	//PC -> centrale
	final static int SET_ZONE_ESCL = 0x44;
	final static int SET_USC_OC = 0x46;
	final static int SET_SETTORI = 0x45;
	final static int SET_LOGIN = 0x51;
	final static int SET_LOGOUT = 0x52;

	//AGE = Ask e Get, formattazione Estesa (cfr. download.c telefonico XTREAM)
	final static int AGE_ZONE = 0x2E;
		final static int g1_STATO = BIT0;
		final static int g1_TAMPER = BIT1;
		final static int g1_ESCL = BIT2;
		final static int g1_BATT_RADIO = BIT4;
		final static int g1_SOPRAVV = BIT5;
		final static int g1_ANTIMASK = BIT6;
		final static int g1_ALLARMI_AVVENUTI = BIT7;
	final static int AGE_SETT = 0x2D;
		final static int SETT_NON_PERM = 0x07;
	final static int AGE_OC = 0x29;
	final static int AGE_MEMORY_DUMP = 0x89;
	final static int AGE_ALFACODE = 0x8A;
	final static int AGE_SYSVERSION = 0x8B;
	
	/*
	#byte
	1 > Lunghezza pacchetto (4 bit [4567]); TipoPeriferica (4 bit [0123])
	2 > Numero periferica (per la centrale è sempre 0)
	--- lunghezza si calcola da qua in giù
	3 > Stato Tamper e Alimentatori
	4 > Stato Rele e Telef
	5 > Stato Fusibili
	6 - 7 > 2byte Assorbimento(corrente mA)
	8 - 9 > 2byte tensione linea telefonica
	10 - 11 > 2byte credito SIM GSM
	12 > 1byte GSM signal quality
	*/
	final static int AGE_SYSTEM = 0x80;
		final static int PERIFERICA_CENTRALE = 0;
		final static int PERIFERICA_TAST_LCD = 1;
		final static int PERIFERICA_SATELLITE = 2;
		final static int PERIFERICA_INSWCPU = 5;
		//
		final static int bit_TAMPER_SW = BIT0;
		final static int bit_TAMPER_COM = BIT1;
		final static int bit_MANC_RETE = BIT2;
		final static int bit_BATT_BASSA = BIT3;
		final static int bit_BATT_MANC = BIT4;
		final static int bit_FIRE = BIT5;
		final static int bit_INTERF = BIT6;
		//
		final static int bit_ANOMALIA_PSTN = BIT0;
		final static int bit_ANOMALIA_GSM = BIT1;
		final static int bit_RELE_ON = BIT2;
		final static int bit_SIR_ON = BIT3;
		final static int bit_OCTA_ON = BIT4;
	final static int GET_ERR_ZONE_ESCL = 0x64;
	final static int GET_ERR_OC = 0x66;
	final static int GET_ERR_SETTORI = 0x65;
	final static int GET_ERR_LOGIN = 0x67;
		final static int INFO_LOGIN_OK = 0x00;
		final static int INFO_LOGOUT_OK = 0x01;
		final static int INFO_LOGOUT_TO = 0x02; //forzato per timeout
		final static int INFO_LOGOUT_RST = 0x03; //forzato per reset (o menu install)
	final static int LOGOUT_ALL_SESSIONS = 0xFF; //#sessione speciale, indica "tutte le sessioni"
	
	public AVSMessage(int seqNumber, int session, int command, int[] data) {
		this.seqNumber = seqNumber;
		this.session = session;
		this.command = command;
		this.data = data;
	}

	public AVSMessage(AVSMessage m) {
		this(m.getSeqNumber(),m.getSession(),m.getCommand(),m.getData());
	}

	public AVSMessage(int command) {
		this(0, 0, command, new int[0]);
	}

	public AVSMessage(int command, int[] data) {
		this(0, 0, command, data);
	}

	int getSeqNumber() {
		return this.seqNumber;
	}

	/**
	 * @return the command
	 */
	int getCommand() {
		return command;
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
		message[4] = getSession();
		message[5] = 0x00;  // selettore
		message[6] = 0x00;  // codifica
		
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
			+ " Command=" + getCommand() + " Session=" + getSession();
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
			sb.append(" " + b2h(m[i]));
		}
		return sb.toString();
	}
	
	/**
	 * 
	 * @param numItems Number of items
	 * @param data Payload of message 
	 * @return
	 */
	HashMap<Integer,Integer> decodeData(int numItems) {
		HashMap<Integer,Integer> res = new HashMap<Integer,Integer>(numItems);
		
		for (int i = 0; i < data.length; i++) {
			// coppia istanza-stato
			res.put(data[i] << 8 + data[i+1], data[i + 2]);
			i += 3;
		}
		return res;
	}



}
