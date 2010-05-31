package it.ascia.avs;


public class AVSGetErrorMessage extends AVSMessage {

	static final int ERRORCODE_SELSTATO_UTENTE_NON_ABILIT = 0x80;
	static final int ERRORCODE_SELSTATO_ACCENSIONE_FORZATA = 0x81;
	static final int ERRORCODE_SELSTATO_ERR_AUTOTEST = 0x82;
	static final int ERRORCODE_SELSTATO_PROGR_ORARIO_ATTIVO = 0x83;

	static final int ERRORCODE_SEL_LOGIN_ERR_VALIDAZ = 0x80;
	static final int ERRORCODE_SEL_LOGIN_CADUTA_CONN_OVERFLOW = 0x81;
	/**
	 * caduta connessione per mancata risposta host
	 */
	static final int ERRORCODE_SEL_LOGIN_CADUTA_CONN_MANCATA_RISP = 0x82;
	/**
	 * Caduta connessione per errore handshake (DTR)
	 */
	static final int ERRORCODE_SEL_LOGIN_CADUTA_CONN_ERR_HNDS = 0x83;
	static final int ERRORCODE_SEL_LOGIN_CADUTA_CONN_PROT_NON_DEF = 0x84;

	/**
	 * busy (sistema impegnato)
	 */
	static final int ERRORCODE_SEL_GENERIC_BUSY = 0x90;
	/**
	 * overflow (coda verso host piena)
	 */
	static final int ERRORCODE_SEL_GENERIC_OVERFLOW = 0x91;

	//static final int ERROR_DISPLAY_TIMEOUT = 500;                                          

	public AVSGetErrorMessage(int seqNumber, int session, Code code, int format, int[] data) {
		super(seqNumber, session, code, format, data);
	}

	public AVSGetErrorMessage(AVSMessage m) {
		super(m);
	}

	public String toString() {
		return super.toString() + " : " + getErrorDescription();
	}
	
	public String getErrorDescription() {
		String s = "";
		Code code = getCode();
		if (Code.GET_ERROR_BYPASS_ZONE.match(code)) {
			s += "ERROR SEL_BYPASS_ZONE";
		} else if (Code.GET_ERROR_STATO_SETT.match(code)) {
			if(data.length == 0) {      					
				s += "ERROR SEL_STATO_SETT";
			} else {
				switch(data[0]) {
					case (ERRORCODE_SELSTATO_UTENTE_NON_ABILIT):
						s += "ERROR  SELSTATO_UTENTE_NON_ABILIT";
						break;
					case (ERRORCODE_SELSTATO_ACCENSIONE_FORZATA):
						s += "ERROR  SELSTATO_ACCENSIONE_FORZATA";
						break;
					case (ERRORCODE_SELSTATO_ERR_AUTOTEST):
						s += "ERROR  SELSTATO_ERR_AUTOTEST";
						break;
					case (ERRORCODE_SELSTATO_PROGR_ORARIO_ATTIVO):
						s += "ERROR  SELSTATO_PROGR_ORARIO_ATTIVO";
						break;
					default:
						s += "ERROR  SEL_STATO Unknow error code="+b2h(data[0]);
						break;
				} // fine switch(data[0])
			}
		} else if (Code.GET_ERROR_USCITA_OC_DIG.match(code)) {
			s += "ERROR SEL_USCITA_OC_DIG";
		} else if (Code.GET_ERROR_LOGIN.match(code)) {
			if(data.length == 0) {      					
				s += "ERROR SEL_LOGIN";
			} else {
				switch(data[0]) {
					case(ERRORCODE_SEL_LOGIN_ERR_VALIDAZ):
						s += "ERROR  SEL_LOGIN_ERR_VALIDAZ";
						break;
					case (ERRORCODE_SEL_LOGIN_CADUTA_CONN_OVERFLOW):
						s += "ERROR  SEL_LOGIN_CADUTA_CONN_OVERFLOW";
						break;
					case (ERRORCODE_SEL_LOGIN_CADUTA_CONN_MANCATA_RISP):
						s += "ERROR  SEL_LOGIN_CADUTA_CONN_MANCATA_RISP";
						break;
					case (ERRORCODE_SEL_LOGIN_CADUTA_CONN_ERR_HNDS):
						s += "ERROR  SEL_LOGIN_CADUTA_CONN_ERR_HNDS";
						break;
					case (ERRORCODE_SEL_LOGIN_CADUTA_CONN_PROT_NON_DEF):
						s += "ERROR  SEL_LOGIN_CADUTA_CONN_PROT_NON_DEF";
						break;
					case (0x90):
						s += "Mancata connessione lato centrale (errore generico)";
						break;
					case (0x91):
						s += "disconnessione richiesta da centrale (menù installatore)";
						break;
					default:
						s += "ERROR  SEL_LOGIN Unknow error code="+b2h(data[0]);
						break;
				} // fine switch(data[0])
			}
		} else if (Code.GET_ERROR_USCITA_OC_AN.match(code)) {
			s += "ERROR SEL_USCITA_OC_AN";
		} else if (Code.GET_ERROR_GENERIC.match(code)) {
			if(data.length == 0) {      					
				s += "ERROR SEL_GENERIC";
			} else {
				switch(data[0]) {
					case (ERRORCODE_SEL_GENERIC_BUSY):
						s += "Busy (sistema impegnato)";
						break;
					case (ERRORCODE_SEL_GENERIC_OVERFLOW):
						s += "Overflow (coda verso host piena)";
						break;
					case (0x82):
						s += "Caduta connessione per mancata risposta host nella sessione precedente";
						break;						
					case (0x83):
						s += "Caduta connessione per errore handshake (DTR) nella sessione precedente";
						break;						
					default:
						s += "ERROR SEL_GENERIC Unknow error code="+b2h(data[0]);
						break;
				} // fine switch(data[0])
			}
		} else {
			s += "Unknow selector="+b2h(getSelector());
		}	
		return s;
	}
}
