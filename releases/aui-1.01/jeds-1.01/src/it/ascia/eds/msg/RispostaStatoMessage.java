/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.msg;

/**
 * Risposta lettura stato dispositivo.
 */
public class RispostaStatoMessage extends PTPResponse {

	/**
	 * Costruttore.
	 * 
	 * @param m messaggio a cui si risponde.
	 * @param Uscite stato delle uscite.
	 * @param Entrate stato degli ingressi.
	 */
	public RispostaStatoMessage(RichiestaStatoMessage m, boolean[] Uscite, 
			boolean[] Entrate) {
		Destinatario = m.getSender();
		Mittente = m.getRecipient();
		TipoMessaggio = getMessageType();
		Byte1 = Byte2 = 0;
		for (int i = 0; i < 8; i++) {
			if ((i < Uscite.length) && Uscite[i]) {
				Byte2 |= 1 << i;
			}
		}
		for (int i = 0; i < 8; i++) {
			if ((i < Entrate.length) && Entrate[i]) {
				Byte2 |= 1 << i;
			}
		}
	}

	public RispostaStatoMessage(int[] message) {
		load(message);
	}

	public String getMessageDescription() {
		return "Risposta a richiesta Stato";
	}

	public String toString()	{
		StringBuffer s = new StringBuffer();
		s.append(super.toString());
		s.append(" Entrate:");
		for (int i = 0; i <= 7; i++) {
			s.append(((Byte2 >> i) & 0x01));
		} 
		s.append(" Uscite:");
		for (int i = 0; i <= 7; i++) {
			s.append(((Byte1 >> i) & 0x01));
		} 
		return s.toString();
	}
	
	/**
	 * Ritorna lo stato degli ingressi.
	 * 
	 * @return un'array di 8 booleani, anche se il BMC ha meno porte. Gli 
	 * elementi true sono attivi. 
	 */
	public boolean[] getInputs() {
		boolean retval[];
		int i;
		retval = new boolean[8];
		for (i = 0; i < 8; i++) {
			int set = Byte2 & (1 << i);
			retval[i] = (set != 0);
		}
		return retval;
	}
	
	/**
	 * Ritorna lo stato delle uscite.
	 * 
	 * @return un'array di 8 booleani, anche se il BMC ha meno porte. Gli 
	 * elementi true sono attivi. 
	 */
	public boolean[] getOutputs() {
		boolean retval[];
		int i;
		retval = new boolean[8];
		for (i = 0; i < 8; i++) {
			int set = Byte1 & (1 << i);
			retval[i] = (set != 0);
		}
		return retval;
	}

	public int getMessageType() {
		return MSG_RISPOSTA_STATO;
	}
}
