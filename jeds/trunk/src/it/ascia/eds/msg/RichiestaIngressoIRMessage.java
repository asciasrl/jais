package it.ascia.eds.msg;

public class RichiestaIngressoIRMessage extends PTPRequest {

	public RichiestaIngressoIRMessage(int d, int m, int Uscita) {
		Destinatario = d & 0xFF;
		Mittente = m & 0xFF;
		TipoMessaggio = 39;
		Byte1 = Uscita;
		Byte2 = 0;
	}
	
	public RichiestaIngressoIRMessage(int[] message) {
		load(message);
	}

	public String getMessageDescription() {
		return "Richiesta codice ingresso IR";
	}

	public String toString()	{
		StringBuffer s = new StringBuffer();
		s.append(super.toString());
		s.append(" Ingresso IR: "+(Byte1 & 0xFF));
		return s.toString();
	}

	public boolean isAnsweredBy(PTPMessage m) {
		if (m.getMessageType() == EDSMessage.MSG_RISPOSTA_INGRESSO_IR) {
			if ((getSender() == m.getRecipient()) &&
					(getRecipient() == m.getSender())) {
				return true;
			}
		}
		return false;
	}

	public int getMessageType() {
		return MSG_RICHIESTA_INGRESSO_IR;
	}
}
