/**
 * Copyright (C) 2008 ASCIA S.r.l.
 */
package it.ascia.eds.msg;

/**
 * Messaggio di impostazione parametri (per dimmer o sonda termica).
 * 
 * <p>I valori per alcuni parametri si possono trovare come costanti statiche
 * della classe {@link RichiestaParametroMessage}.</p>
 * 
 * @author sergio, arrigo
 */
public class ImpostaParametroMessage extends PTPMessage {

	/**
	 * Costruttore.
	 * 
	 * @param d destinatario
	 * @param m mittente
	 * @param parametro numero del parametro da impostare
	 * @param valore valore da impostare
	 */
	public ImpostaParametroMessage(int d, int m, int parametro, int valore) {
		Destinatario = d & 0xFF;
		Mittente = m & 0xFF;
		TipoMessaggio = getMessageType();
		Byte1 = parametro & 0xff;
		Byte2 = valore & 0xff;
	}

	public ImpostaParametroMessage(int[] message) {
		load(message);
	}

	public String getMessageDescription() {
		return "Impostazione parametro";
	}
	
	public String toString()	{
		StringBuffer s = new StringBuffer();
		s.append(super.toString());
		s.append(" Parametro:"+getParameter()+" Valore:"+getValue());
		return s.toString();
	}

	/**
	 * Ritorna il parametro impostato con questo messaggio.
	 */
	public int getParameter() {
		return Byte1 & 0xff;
	}
	
	/**
	 * Ritorna il valore del parametro impostato con questo messaggio.
	 */
	public int getValue() {
		return Byte2 & 0xff;
	}

	public int getMessageType() {
		return MSG_IMPOSTA_PARAMETRO;
	}
	
}
