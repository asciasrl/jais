package it.ascia.eds.msg;

public class RispostaModelloMessage extends PTPResponse {

	public RispostaModelloMessage(int d, int m, int Modello, int Versione) {
		Destinatario = d & 0xFF;
		Mittente = m & 0xFF;
		TipoMessaggio = 1;
		Byte1 = Modello & 0xFF;
		Byte2 = Versione & 0xFF;
	}

	public RispostaModelloMessage(int[] message) {
		load(message);
	}

	public String getMessageDescription() {
		return "Risposta Modello e Revisione";
	}
	
	public int getModello() {
		return Byte1;
	}

	public String toString()	{
		StringBuffer s = new StringBuffer();
		s.append(super.toString()+" ");
		s.append(" Model:"+Byte1);
		s.append(" Revision:"+Byte2);
		return s.toString();
	}

	public int getMessageType() {
		return MSG_RISPOSTA_MODELLO;
	}
}
