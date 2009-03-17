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
	
/*	public ImpostaParametroDimmerMessage(int d, int m, int Tempo, int Uscita, int Percentuale, int Attivazione) {
		Destinatario = d & 0xFF;
		Mittente = m & 0xFF;
		TipoMessaggio = 54;
		//Byte1 = (Uscita & 0x07) + ((Tempo & 0x0F) << 3);
		//Byte2 = (Attivazione & 0x01) + ((Percentuale & 0x7F) << 1);
	}
*/	
	public RispostaParametroMessage(int[] message) {
		load(message);
	}

	public String getMessageDescription() {
		return "Risposta a lettura parametro (dimmer o sonda termica)";
	}
	
	/**
	 * Controlla se il messaggio contiene il tempo di auto invio per una
	 * sonda termica.
	 * 
	 * @return true se questo messaggio contiene tale campo.
	 * 
	 * @see #getAutoSendTime()
	 */
	public boolean hasAutoSendTime() {
		return (Byte1 == ImpostaParametroMessage.PARAM_TERM_AUTO_SEND_TIME);
	}
	
	/**
	 * Ritorna il valore del campo "tempo di auto invio" [sec].
	 * 
	 * <p>Questo valore ha senso se il messaggio e' inviato a una sonda
	 * termica.</p>
	 * 
	 * @return il valore in secondi del parametro.
	 */
	public int getAutoSendTime() {
		if ((Byte2 & 0x80) == 0) { // Minuti
			return Byte2 * 60;
		} else { // Secondi
			return Byte2;
		}
	}
	
	/**
	 * Controlla se il messaggio contiene la temperatura di allarme per una
	 * sonda termica.
	 * 
	 * @return true se questo messaggio contiene tale campo.
	 * 
	 * @see #getAlarmTemperature()
	 */
	public boolean hasAlarmTemperature() {
		return (Byte1 == 
			ImpostaParametroMessage.PARAM_TERM_ALARM_TEMPERATURE);
	}
	
	/**
	 * Ritorna la temperatura di allarme [gradi C].
	 * 
	 * @return la temperatura di allarme.
	 */
	public int getAlarmTemperature() {
		int temp = (Byte2 & 0x7f);
		if ((Byte2 & 0x80) == 0) {
			return temp;
		} else {
			return -temp;
		}
	}

	/**
	 * Ritorna il parametro richiesto da questo messaggio.
	 */
	public int getParameter() {
		return Byte1 & 0xff;
	}
	
	public String toString()	{
		StringBuffer s = new StringBuffer();
		s.append(super.toString());
		switch (getParameter()) {
			case 1:
				s.append(" Dimmer Soft time: "+Byte2);
				s.append(" Tempo di auto-invio temperatura: " + getAutoSendTime() + "s");		
				break;
			case 3:
				s.append(" Dimmer: Ritardo variazione: "+Byte2);
				s.append(" Temperatura di allarme: " + getAlarmTemperature() +	"gradi C");
				break;
			case 11:
				s.append(" Dimmer: Minimo: "+Byte2);
				break;
			case 12:
				s.append(" Dimmer: Massimo: "+Byte2);
				break;
			case 22:
				s.append(" Dimmer: Sensibilita: "+Byte2);
				break;
			default:
				s.append(" Parametro sconosciuto (" + getParameter() + 
						") con valore: " + Byte2);
				break;
		}
		return s.toString();
	}

	public int getMessageType() {
		return MSG_RISPOSTA_PARAMETRO;
	}
	
}
