/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.msg;

/**
 * Un ingresso e' cambiato, deve cambiare un'uscita.
 * 
 * Questo messaggio viene generato, ad es., quando un interruttore viene 
 * premuto.
 * 
 * @author sergio, arrigo
 * TODO Distinguere fra dispositivi di input e termostato
 */
public class VariazioneIngressoMessage extends PTPMessage
	implements MessageInterface
	{

	public VariazioneIngressoMessage(int d, int m, int Attivazione, int Uscita, int Variazione) {
		Destinatario = d & 0xFF;
		Mittente = m & 0xFF;
		TipoMessaggio = 4;
		Byte1 = Uscita & 0x07 + ((Attivazione & 0x01) << 3);
		Byte2 = Variazione & 0x01;
	}

	public VariazioneIngressoMessage(int[] message) {
		parseMessage(message);
	}

	public String getTipoMessaggio() {
		return "Variazione di un ingresso";
	}
	
	/**
	 * Verifica se questo messaggio richiede l'attivazione di un'uscita.
	 * 
	 * @return true se si richiede l'attivazione dell'uscita.
	 */
	public boolean isActivation() {
		return ((Byte1 & 0x08) == 0);
	}
	
	/**
	 * Ritorna il numero dell'uscita interessata.
	 */
	public int getOutputNumber() {
		return (Byte1 & 0x07);
	}
		
	public String getInformazioni()	{
		StringBuffer s = new StringBuffer();
		s.append("Mittente: "+ Mittente +"\r\n");
		s.append("Destinatario: "+ Destinatario +"\r\n");
		if (isActivation()) {
			s.append("Attivazione/Incremento\r\n");
		} else {
			s.append("Disattivazione/Decremento\r\n");
		}
		s.append("Numero uscita: "+ getOutputNumber() +"\r\n");
		s.append("Variazione: "+ (Byte2 & 0x01) +"\r\n");
		return s.toString();
	}

	public int getMessageType() {
		return MSG_VARIAZIONE_INGRESSO;
	}
}
