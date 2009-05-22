/**
 * Copyright (C) 2008 ASCIA S.r.l.
 */
package it.ascia.eds.msg;

/**
 * Messaggio di risposta a richiesta opzioni ingresso.
 * 
 * @author sergio
 */
public class RispostaOpzioniIngressoMessage extends PTPResponse {
	
	public RispostaOpzioniIngressoMessage(int[] message) {
		load(message);
	}

	public String getMessageDescription() {
		return "Risposta opzioni ingresso";
	}
	
	public String toString()	{
		StringBuffer s = new StringBuffer();
		s.append(super.toString());
		s.append(" Opzioni Inp"+(Byte1+1));
		if ((Byte2 & 0x08) != 0) {
			s.append(" Invertito");
		}
		if ((Byte2 & 0x04) != 0) {
			s.append(" Sincronizzato");
		}
		if ((Byte2 & 0x02) != 0) {
			s.append(" Monostabile");
		}
		if ((Byte2 & 0x01) != 0) {
			s.append(" Comando sicuro");
		}
		return s.toString();
	}

	public int getMessageType() {
		return MSG_RISPOSTA_OPZIONI_INGRESSO;
	}
	
}
