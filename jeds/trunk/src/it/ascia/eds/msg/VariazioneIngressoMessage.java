/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.msg;

import it.ascia.eds.device.BMCChronoTerm;

/**
 * Un ingresso e' cambiato, deve cambiare un'uscita.
 * 
 * <p>Questo messaggio viene generato, ad es., quando un interruttore viene 
 * premuto.</p>
 * 
 * <p>Il cronotermostato rilegge questo messaggio dando ai campi del primo byte 
 * un valore diverso.</p>
 * 
 * @author sergio, arrigo
 */
public class VariazioneIngressoMessage extends PTPCommand
	{

	/**
	 * Costruttore per messaggio diretto a BMC.
	 * 
	 * @param d destinatario del messaggio.
	 * @param m mittente del messaggio.
	 * @param Attivazione true per attivazione/incremento, false per disattivazione/decremento.
	 * @param Uscita numero dell'uscita da cambiare (da 0 a 7).
	 * @param Variazione 0: circuito aperto, 1: contatto (cortocircuito a 
	 * massa).
	 */
	public VariazioneIngressoMessage(int d, int m, boolean Attivazione, 
			int Uscita, boolean Variazione) {
		this(d, m, Attivazione, Uscita, Variazione, 0, false, 0);
	}
	
	/**
	 * Costruttore per messaggio diretto a modulo motori.
	 * 
	 * @param d destinatario del messaggio.
	 * @param m mittente del messaggio.
	 * @param Attivazione true per attivazione/incremento, false per disattivazione/decremento.
	 * @param Uscita numero dell'uscita da cambiare (da 0 a 7).
	 * @param Variazione 0: circuito aperto, 1: contatto (cortocircuito a 
	 * massa).
	 * @param Ingresso
	 * @param Toggle
	 * @param ReTx
	 * 
	 * @since 20100713
	 */
	public VariazioneIngressoMessage(int d, int m, boolean Attivazione, 
			int Uscita, boolean Variazione, int Ingresso, boolean Toggle, int ReTx) {
		Destinatario = d & 0xFF;
		Mittente = m & 0xFF;
		TipoMessaggio = getMessageType();
		Byte1 = Uscita & 0x07;
		if (!Attivazione) {
			Byte1 |= 0x01 << 3;
		}
		Byte2 = (Variazione ? 0x01 : 0x00) + (((Ingresso-1) & 0x07) << 1);
	}

	/**
	 * Costruttore per messaggio diretto a cronotermostato o centralina scenari
	 * 
	 * @param d destinatario.
	 * @param m mittente.
	 * @param stato stato da impostare o scenario da attivare
	 * 
	 * @see BMCChronoTerm
	 */
	public VariazioneIngressoMessage(int d, int m, int stato) {
		Destinatario = d & 0xFF;
		Mittente = m & 0xFF;
		TipoMessaggio = getMessageType();
		Byte1 = stato & 0x0f;
		Byte2 = 0;
	}

	public VariazioneIngressoMessage(int[] message) {
		load(message);
	}

	public String getMessageDescription() {
		return "Variazione ingresso";
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
	 * Indica la modalita' di variazione dell'ingresso verso massa 
	 * (0 = apertura verso GND, 1 = chiusura verso GND)
	 * 
	 * @return true Ingresso chiuso
	 */
	public boolean isClose() {
		return ((Byte2 & 0x01) == 1);
	}
	
	
	
	/**
	 * Ritorna il numero dell'uscita interessata.
	 */
	public int getOutputNumber() {
		return (Byte1 & 0x07);
	}
	
	/**
	 * Ritorna il numero dell'ingresso.
	 * @since 20100713
	 */
	public int getInputNumber() {
		return (Byte2 & 0x0E) >> 1;
	}
	
	/**
	 * Ritorna il numero di scena attivata dal comando.
	 * 
	 * <p>Questo metodo ha senso solo  se questo messaggio e' diretto a una 
	 * centralina scenari.</p>
	 */
	public int getScenePortNumber() {
		return (Byte1 & 0x0F);
	}
	
	/**
	 * Ritorna il numero dello stato indicato per il cronotermostato.
	 * 
	 * <p>Questo metodo ha senso solo  se questo messaggio e' diretto a un 
	 * cronotermostato.</p>
	 */
	public int getChronoTermState() {
		return Byte1 & 0x0f;
	}
	
	public String toString()	{
		StringBuffer s = new StringBuffer();
		s.append(super.toString());
		s.append(" Inp" + (getInputNumber()+1));
		if (isClose()) {
			s.append(" Chiuso");
		} else {
			s.append(" Aperto");
		}
		s.append(" Out"+ (getOutputNumber()+1));
		s.append(" Scene" + (getScenePortNumber()+1));
		if (isActivation()) {
			s.append(" Attiva");
		} else {
			s.append(" Disattiva");
		}
		s.append(" CT: " + BMCChronoTerm.getStateAsString(getChronoTermState()));
		return s.toString();
	}

	public int getMessageType() {
		return MSG_VARIAZIONE_INGRESSO;
	}

}
