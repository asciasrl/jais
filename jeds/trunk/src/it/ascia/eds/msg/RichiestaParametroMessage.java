/**
 * Copyright (C) 2008 ASCIA S.r.l.
 */
package it.ascia.eds.msg;

import it.ascia.ais.MessageInterface;

/**
 * Messaggio di richiesta di lettura parametri (per dimmer o sonda 
 * termica).
 * 
 * <p>Codice EDS: 55.</p>
 * 
 * @author arrigo
 */
public class RichiestaParametroMessage extends PTPRequest
	implements MessageInterface {
	/**
	 * Tempo di auto-invio della temperatura, per una sonda termica.
	 */
	public static final int PARAM_TERM_AUTO_SEND_TIME = 1;
	/**
	 * Temperatura di allarme, per una sonda termica.
	 */
	public static final int PARAM_TERM_ALARM_TEMPERATURE = 3;
	
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
		return "Richiesta lettura parametro (dimmer o sonda termica)";
	}
	
	public String toString()	{
		StringBuffer s = new StringBuffer();
		s.append("Mittente: "+Mittente+"\r\n");
		s.append("Destinatario: "+Destinatario+"\r\n");
		s.append("Parametro richiesto: ");
		switch (getParameter()) {
			case PARAM_TERM_AUTO_SEND_TIME:
				s.append("tempo di auto-invio (sonda termica)");		
				break;
			case PARAM_TERM_ALARM_TEMPERATURE:
				s.append("temperatura di allarme (sonda termica) ");
				break;
			default:
				s.append("sconosciuto (" + getParameter() + ")");
				break;
		}
		return s.toString();
	}

	public boolean isAnsweredBy(PTPMessage m) {
		if (m.getMessageType() == EDSMessage.MSG_RISPOSTA_PARAMETRO) {
			RispostaParametroMessage ack = (RispostaParametroMessage) m;
			if ((getSender() == ack.getRecipient()) &&
					(getRecipient() == ack.getSender()) &&
					(ack.getParameter() == getParameter())) {
				answered = true;
			}
		}
		return answered;
	}
	
	public int getMessageType() {
		return MSG_RICHIESTA_PARAMETRO;
	}
}
