/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.msg;

/**
 * Messaggio di richiesta set point del cronotermostato.
 * 
 * Lo stesso codice di messaggio viene utilizzato sia dal cronotermostato Home Innovation che da quello di World Data Bus, ma con un formato diverso
 * 
 * <p>Codice EDS: 204.</p>
 */
public class RichiestaSetPointMessage extends PTPRequest {

	/**
	 * Richiede set point attuale del cronotermostato
	 * @param d Destinatario
	 * @param m Mittente
	 * @param attuale Da impostare a true per sonda termica / cronotermostato WDB, false per Cronotermostato Home Innovation
	 */
	public RichiestaSetPointMessage(int d, int m, boolean attuale) {		
		this(d, m, 0, 0, attuale ? 31 : 0);
	}

	/**
	 * Richiede set point per stagione/giorno/ora
	 * @param d Destinatario
	 * @param m Mittente
	 * @param giorno (0-6)
	 * @param ora Fascia oraria (0-23)
	 * @param stagione (0=estate,1=inverno)
	 */
	public RichiestaSetPointMessage(int d, int m, int stagione, int giorno, int ora) {
		Destinatario = d & 0xFF;
		Mittente = m & 0xFF;
		TipoMessaggio = getMessageType();
		Byte1 = (giorno & 0x07) + ((ora & 0x1F) << 3);
		Byte2 = stagione & 0x01;
	}
	
	public RichiestaSetPointMessage(int d, int m,
			String sStagione, String sGiorno, int ora) {
		this(d,m,ImpostaSetPointMessage.stagione(sStagione),ImpostaSetPointMessage.giorno(sGiorno),ora);
	}
		
	public RichiestaSetPointMessage(int[] message) {
		load(message);
	}

	public String getMessageDescription() {
		return "Richiesta set point cronotermostato";
	}
	
	public boolean isAnsweredBy(PTPMessage m) {
		if (m.getMessageType() == EDSMessage.MSG_LETTURA_SET_POINT
				|| m.getMessageType() == EDSMessage.MSG_RISPOSTA_SET_POINT) {
			if ((getSender() == m.getRecipient()) &&
					(getRecipient() == m.getSender())) {
				return true;
			}
		}
		return false;
	}
	
	public int getMessageType() {
		return MSG_RICHIESTA_SET_POINT;
	}
	
	public int getOra() {
		return (Byte1 & 0xF8) >> 3;
	}
	
	public int getGiorno() {
		return Byte1 & 0x07;
	}

	public int getStagione() {
		return Byte2 & 0x01;
	}
	
	public String toString()	{
		StringBuffer s = new StringBuffer();
		s.append(super.toString());
		if (getOra() == 31) {
			s.append(" Attuale");
		} else {
			s.append(" Stagione:"+(getStagione() == 1 ? "Estate":"Inverno"));
			s.append(" Giorno:"+ImpostaSetPointMessage.giorno(getGiorno()));
			s.append(" Orario:"+ImpostaSetPointMessage.fasciaOraria(getOra()));
		}
		return s.toString();
	}

}
