package it.ascia.avs;

public class EL88ErrorMessage extends EL88Message {

	public EL88ErrorMessage(int selector, int format, int[] data) {
		super(GET_ERROR, selector, format, data);
	}

	public String toString() {
		return super.toString() + " : " + getErrorDescription();
	}
	
	public String getErrorDescription() {
		String s = "";
		switch(selector) {         
			case(SEL_BYPASS_ZONE):
				s += "ERROR SEL_BYPASS_ZONE";
				break;
			case(SEL_STATO_SETT):
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
				break;
			case(SEL_USCITA_OC_DIG):
				s += "ERROR SEL_USCITA_OC_DIG";
				break;
			case(SEL_LOGIN):
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
						default:
							s += "ERROR  SEL_LOGIN Unknow error code="+b2h(data[0]);
							break;
					} // fine switch(data[0])
				}
				break;
			case(SEL_USCITA_OC_AN):
				s += "ERROR SEL_USCITA_OC_AN";
				break;
			case(SEL_GENERIC):
				if(data.length == 0) {      					
					s += "ERROR SEL_GENERIC";
				} else {
					switch(data[0]) {
						case (ERRORCODE_SEL_GENERIC_BUSY):
							s += "ERROR  SEL_GENERIC_BUSY";
							break;
						case (ERRORCODE_SEL_GENERIC_OVERFLOW):
							s += "ERROR  SEL_GENERIC_OVERFLOW";
							break;
						default:
							s += "ERROR SEL_GENERIC Unknow error code="+b2h(data[0]);
							break;
					} // fine switch(data[0])
				}
				break;
			default:
				s += "Unknow selector="+b2h(selector);
				break;			
		} // fine switch(selector)	
		return s;
	}
}
