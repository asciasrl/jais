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
public class ImpostaParametroMessage extends PTPRequest {

	/**
	 * Temperatura. Puo' variare da -127 a 127.
	 * 
	 * <p>Il bit piu' significativo viene posto a 1 se il valore e' negativo.
	 * </p>
	 */
	public final static int PARM_TEMPERATURE = 0;

	/**
	 * Tempo di auto-invio della temperatura, per una sonda termica.
	 */
	public static final int PARAM_TERM_AUTO_SEND_TIME = 1;

	/**
	 * Tempo. Puo' variare da 1 secondo a 127 minuti.
	 * 
	 * <p>Il bit piu' significativo viene posto a 1 se il tempo e' in secondi,
	 * a 0 se il tempo e' in minuti.</p>
	 */
	public final static int PARM_TIME = 1;
	
	/**
	 * Temperatura di allarme, per una sonda termica.
	 */
	public static final int PARAM_TERM_ALARM_TEMPERATURE = 3;
	
	
		
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
		switch (parametro) {
		case PARM_TEMPERATURE:
			Byte2 = Math.abs(valore) & 0x7f;
			if (valore < 0) {
				// Bit di segno
				Byte2 |= 0x80;
			}
			break;
		case PARM_TIME:
			if ((valore & 0xff) <= 127) {
				// Secondi: impostiamo il MSB
				Byte2 = valore | 0x80;
			} else {
				// Minuti: LSB vale 0
				Byte2 = (valore / 60) & 0x7f;
			}
			break;
		default:
			Byte2 = valore & 0xff;
			break;
		}
	}

	public ImpostaParametroMessage(int[] message) {
		load(message);
	}

	public String getMessageDescription() {
		return "Impostazione parametro";
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
	 * Ritorna il valore del campo "tempo di auto invio". [sec]
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
		return (Byte1 == ImpostaParametroMessage.PARAM_TERM_ALARM_TEMPERATURE);
	}
	
	/**
	 * Ritorna la temperatura di allarme [gradi C]
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

	public String toString()	{
		StringBuffer s = new StringBuffer();
		s.append(super.toString());
		switch (Byte1) {
			case PARM_TIME:
				s.append(" Dimmer: Soft time = "+Byte2);
				s.append(" Sonda termica: Tempo di auto-invio = " + getAutoSendTime() + "sec");		
				break;
			case PARAM_TERM_ALARM_TEMPERATURE:
				s.append(" Dimmer: Ritardo variazione = "+Byte2);
				s.append(" Sonda termica: Temperatura di allarme = " + getAlarmTemperature() +	"gradi C");
				break;
			case 11:
				s.append(" Dimmer: Minimo = "+Byte2+"%");
				break;
			case 12:
				s.append(" Dimmer: Massimo: "+Byte2+"%");
				break;
			case 22:
				s.append(" Dimmer: Sensibilita: "+Byte2+" 1/10 sec");
				break;
			default:
				s.append(" Parametro sconosciuto (" + Byte1 + ") = " + Byte2);
				break;
		}
		return s.toString();
	}

	public int getMessageType() {
		return MSG_IMPOSTA_PARAMETRO;
	}
	
}
