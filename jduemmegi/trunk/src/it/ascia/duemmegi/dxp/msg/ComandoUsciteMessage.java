package it.ascia.duemmegi.dxp.msg;

import it.ascia.duemmegi.dxp.DXPMessage;
import it.ascia.duemmegi.dxp.DXPRequestMessage;
import it.ascia.duemmegi.dxp.DXPResponseMessage;

public class ComandoUsciteMessage extends DXPRequestMessage {

	public ComandoUsciteMessage(int[] message) {
		load(message);
	}	

	/**
//	 * Comando di una uscita di un modulo digitale
	 * @param d
	 * @param uscita numero uscita (1-4)
	 * @param attiva
	 */
	public ComandoUsciteMessage(int d, int uscita, boolean attiva) {
		funzione = 0x82;
		tipo = COMANDO_USCITE;
		indirizzo = d;
		if ((uscita >= 1) && (uscita <= 4)) {
			dato1 = 0x00; 
			dato0 = (0x01 << (uscita - 1 + 4)) + ((attiva ? 1 : 0 ) << (uscita - 1));
		} else {
			throw(new IndexOutOfBoundsException("Uscita deve essere nell'intervallo 1-4"));
		}
	}

	/**
	 * Comando di piu' uscite di un modulo digitale
	 *  - stati[0] = uscita 1 
	 *  - stati[1] = uscita 2 
	 *  - stati[2] = uscita 3 
	 *  - stati[3] = uscita 4
	 *  Se il valore è null l'uscita non viene variata 
	 * @param d
	 * @param stati
	 */
	public ComandoUsciteMessage(int d, Boolean[] stati) {
		funzione = 0x82;
		tipo = COMANDO_USCITE;
		indirizzo = d;
		dato1 = 0;
		dato0 = 0;
		for (int uscita = 0; (uscita <4) && (uscita < stati.length); uscita++) {
			Boolean attiva = stati[uscita];
			if (attiva != null) {
				dato0 += (0x01 << (uscita - 1 + 4)) + ((attiva.booleanValue() ? 1 : 0 ) << (uscita - 1));
			}
		}
	}

	/**
	 * Comando della uscita di un modulo dimmer
	 * @param d
	 * @param percentuale
	 */
	public ComandoUsciteMessage(int d, int percentuale) {
		funzione = 0x82;
		tipo = COMANDO_USCITE;
		indirizzo = d;
		dato1 = 0;
		dato0 = percentuale & 0xff;
	}
	
	/**
	 * Scrittura moduli di uscita analogici
	 * @param d
	 * @param valore valore analogico (con segno) 
	 */
	public ComandoUsciteMessage(int d, short valore) {
		funzione = 0x82;
		tipo = COMANDO_USCITE;
		indirizzo = d;
		dato1 = (valore & 0xFF00) >> 8;
		dato0 = (valore & 0x00FF);		
	}

	public ComandoUsciteMessage(String address, int percentuale) {
		this((new Integer(address)).intValue(),percentuale);
	}

	public boolean isAnsweredBy(DXPMessage m) {
		if (DXPResponseMessage.class.isInstance(m)
				&& m.getMessageType() == RISPOSTA_STATO_USCITE
				&& ((DXPResponseMessage)m).getSource().equals(getDestination())) {
			return true;
		} else {
			return false;
		}
	}

}
