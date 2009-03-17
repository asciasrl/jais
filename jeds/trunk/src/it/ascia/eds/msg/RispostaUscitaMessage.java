/**
 * Copyright (C) 2008 ASCIA S.r.l.
 */
package it.ascia.eds.msg;

/**
 * Messaggio di risposta a richiesta opzioni uscita.
 * 
 * @author sergio
 */
public class RispostaUscitaMessage extends PTPResponse {
	
	public RispostaUscitaMessage(int[] message) {
		load(message);
	}

	public String getMessageDescription() {
		return "Risposta opzioni uscita";
	}
	
	public String toString()	{
		StringBuffer s = new StringBuffer();
		s.append(super.toString());
		s.append(" Uscita ");
		s.append(isOn() ? "ON ": "OFF");
		switch (getTipoUscita()) {
		  case 0:
			  s.append(" (D) Diretta");
			  break;
		  case 1:
			  s.append(" (D) Diretta non volatile ritardata "+getTimer());
			  break;
		  case 2:
			  s.append(" (D) Diretta non volatile");
			  break;
		  case 3:
			  s.append(" (D) Diretta non volatile ritardata "+getTimer());
			  break;
		  case 4:
			  s.append(" (T) Temporizzata "+getTimer());
			  break;
		  case 6:
			  s.append(" (S) passo passo");
			  break;
		  case 8:
			  s.append(" (S) passo passo Non volatile");
			  break;
		  case 10:
			  s.append(" (R) Set/Reset");
			  break;
		  case 12:
			  s.append(" (R) Set/Reset Non volatile");
			  break;
		  case 14:
			  s.append(" (L) Tapparella "+getTimer());
			  break;
		  case 16:
			  s.append(" (D) Diretta Invertita");
			  break;
		  case 18:
			  s.append(" (D) Diretta Invertita Non volatile");
			  break;
		  case 20:
			  s.append(" (T) Temporizzata Invertita "+getTimer());
			  break;
		  case 22:
			  s.append(" (S) passo passo Due Fronti");
			  break;
		  case 24:
			  s.append(" (S) passo passo Due Fronti Non Volatile");
			  break;
		  case 26:
			  s.append(" (R) Set/Reset Invertita");
			  break;
		  case 28:
			  s.append(" (R) Set/Reset Non volatile Invertita");
			  break;
		  case 29:
			  s.append(" (N) Dimmer normale");
			  break;
		  case 30:
			  s.append(" (U) Dimmer sali/scendi");
			  break;
		  default:
			  s.append(" (?) "+getTipoUscita());  
			  break;
		}
		return s.toString();
	}

	public int getMessageType() {
		return MSG_RISPOSTA_USCITA;
	}

	/**
	 * Ritorna il codice del tipo di uscita.
	 * 
	 * @return un codice numerico strano, si veda la documentazione del 
	 * messaggio 8.
	 */
	public int getTipoUscita() {
		return ((Byte1 >> 3) & 0x1f);
	}
	
	/**
	 * Ritorna lo stato di uscita digitale.
	 * 
	 */
	public boolean isOn() {
		return (Byte1 & 0x01) == 1;		
	}
	
	public long getMillisecTimer() {
		if (Byte2 > 0x7f) {
			return 60000L * (Byte2 & 0x7f);
		} else {
			return 1000L * Byte2;
		}		
	}
	
	public String getTimer() {
		if (Byte2 > 0x7f) {
			return (Byte2 & 0x7f) + " min";
		} else {
			return Byte2 + " sec";
		}
	}

}
