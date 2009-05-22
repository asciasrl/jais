/**
 * Copyright (C) 2008 ASCIA S.r.l.
 */
package it.ascia.eds.msg;

/**
 * Messaggio di risposta a richiesta di lettura parametri (per dimmer o sonda 
 * termica).
 * 
 * <p>I parametri gestiti sono definiti come costanti statiche della classe
 * {@link RichiestaParametroMessage}</p>
 * 
 * @author arrigo
 */
public class RispostaParametroMessage extends PTPResponse {
	
	public RispostaParametroMessage(int[] message) {
		load(message);
	}

	public String getMessageDescription() {
		return "Risposta a lettura parametro";
	}
	
	/**
	 * Ritorna il parametro inviato con questo messaggio.
	 */
	public int getParameter() {
		return Byte1 & 0xff;
	}
	
	/**
	 * Ritorna il valore del parametro inviato con questo messaggio.
	 */
	public int getValue() {
		return Byte2 & 0xff;
	}

	public String toString()	{
		StringBuffer s = new StringBuffer();
		s.append(super.toString());
		s.append(" Parametro:"+getParameter()+" Valore:"+getValue());
		return s.toString();
	}

	public int getMessageType() {
		return MSG_RISPOSTA_PARAMETRO;
	}
	
}
