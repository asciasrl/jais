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
		switch (Byte1) {
			case 8:			
				s.append("BMC Standard I/O, 0 IN - 8 OUT");
				break;
			case 40:			
				s.append("BMC Standard I/O, 4 IN - 0 OUT");
				break;
			case 44:			
				s.append("BMC Standard I/O, 4 IN - 4 OUT");
				break;
			case 60:			
				s.append("BMC Standard I/O, 6 IN - 0 OUT");
				break;
			case 88:			
				s.append("BMC Standard I/O, 8 IN - 8 OUT");
				break;
			case 102:			
				s.append("DIMMER Evolution low power (800W, 2 canali)");
				break;
			case 106:			
				s.append("DIMMER 0-10 V con 1 out SPERIMENTALE ?");
				break;
			case 111:			
				s.append("Dimmer 0-10 V con 1 out");
				break;
			case 127:			
				s.append("Cronotermostato");
				break;
			case 131:			
				s.append("INT-IR Receiver");
				break;
			case 152:			
				s.append("Centralina scenari 2 ingressi");
				break;
			case 154:			
				s.append("Centralina scenari 4 ingressi");
				break;
			case 156:			
				s.append("Centralina scenari 6 ingressi");
				break;
			case 158:			
				s.append("Centralina scenari 8 ingressi");
				break;
			default:
				s.append("sconosciuto");
				break;
		}
		s.append(" Revisione:"+Byte2);
		return s.toString();
	}

	public int getMessageType() {
		return MSG_RISPOSTA_MODELLO;
	}
}
