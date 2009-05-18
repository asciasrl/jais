/**
 * Copyright (C) 2008 ASCIA S.r.l.
 */
package it.ascia.eds.msg;

/**
 * Messaggio di richiesta di lettura parametri (per dimmer o sonda 
 * termica).
 * 
 * <p>Codice EDS: 55.</p>
 * 
 * @author arrigo
 */
public class RichiestaParametroMessage extends PTPRequest {
	/**
	 * Costruttore.
	 * 
	 * @param d destinatario
	 * @param m mittente
	 * @param parametro numero del parametro da richiedere (vedi le costanti
	 * PARAM_* di questa classe).
	 */
	public RichiestaParametroMessage(int d, int m, int parametro) {
		Destinatario = d & 0xFF;
		Mittente = m & 0xFF;
		TipoMessaggio = getMessageType();
		Byte1 = parametro & 0xff;
		Byte2 = 0;
	}
	
	public RichiestaParametroMessage(int[] message) {
		load(message);
	}
	
	/**
	 * Ritorna il parametro richiesto da questo messaggio.
	 */
	public int getParameter() {
		return Byte1 & 0xff;
	}

	public String getMessageDescription() {
		return "Richiesta lettura parametro";
	}
	
	public String toString()	{
		StringBuffer s = new StringBuffer();
		s.append(super.toString());
		s.append(" Parametro richiesto: "+ getParameter());
		return s.toString();
	}

	public boolean isAnsweredBy(PTPMessage m) {
		if (m.getMessageType() == EDSMessage.MSG_RISPOSTA_PARAMETRO) {
			RispostaParametroMessage ack = (RispostaParametroMessage) m;
			if ((getSender() == ack.getRecipient()) &&
					(getRecipient() == ack.getSender()) &&
					(ack.getParameter() == getParameter())) {
				return true;
			}
		}
		return false;
	}
	
	public int getMessageType() {
		return MSG_RICHIESTA_PARAMETRO;
	}
}
