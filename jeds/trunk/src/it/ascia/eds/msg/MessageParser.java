package it.ascia.eds.msg;

import java.util.Date;

/**
 * Decodifica i messaggi del protocollo EDS
 *  
 * (C) 2007,2008 Ascia S.r.l.
 * @author Sergio Strampelli, arrigo
 */
public class MessageParser {

	protected static int Stx = 2;

	protected static int Etx = 3;

	private int[] buff = new int[8];

	private int ibuff = 0;

	private boolean valid = false;

	private Message message;

	public MessageParser() {
		clear();
	}

	public void dumpBuffer() {
		StringBuffer s = new StringBuffer();
		s.append("STX:0x"+Integer.toHexString(buff[0])+" ");
		s.append("DST:0x"+Integer.toHexString(buff[1])+" ");
		s.append("MIT:0x"+Integer.toHexString(buff[2])+" ");
		s.append("TIP:0x"+Integer.toHexString(buff[3])+" ");
		s.append("BY1:0x"+Integer.toHexString(buff[4])+" ");
		s.append("BY2:0x"+Integer.toHexString(buff[5])+" ");
		s.append("CHK:0x"+Integer.toHexString(buff[6])+" ");
		s.append("ETX:0x"+Integer.toHexString(buff[7])+" ");
		System.out.println((new Date()).toString() + " : " + s);
	}

	/**
	 * Accoda i byte ricevuti dalla seriale fino ad ottenere una sequenza valida di 8 byte.
	 * Questa implementazione e' semplificata 
	 * @param b Byte letto dalla seriale
	 */
	public void push(int b) {
		b = b & 0xFF;
		if (ibuff >= 8) {
			clear();
		}
		//System.out.println((new Date()).toString() + " : " + " ibuff="+ibuff+ " b = "+b);
		buff[ibuff++] = b;
		// verifica che il primo byte sia Stx, altrimenti lo scarta
		if (ibuff == 1) {
			if (b != Stx) {
				ibuff = 0;
				System.out.println((new Date()).toString() + " : " + "Non Stx:"+b);
			}
			return;		 
		}
		// verifica che il settimo byte sia checksum valido
		if (ibuff == 7) {
			int chk = 0;
			for (int i = 0; i < 6; i++) {
				chk = (chk + buff[i] & 0xFF) & 0xFF;
			}
			if (chk != b) {
				System.out.println((new Date()).toString() + " : " + "Errore checksum");
				dumpBuffer();
				clear();
				return;
			}
		}
		// verifica che l'ottavo byte sia Etx
		if (ibuff == 8) {
			if (b != Etx) {
				clear();
				return;
			}
			message = createMessage(buff);
			valid = true;
			return;
		}
	}

	/**
	 * Metodo factory per creare messaggi.
	 * 
	 * @param message sequenza di byte che compone il messaggio
	 * @return messaggio decodificato
	 */
	private Message createMessage(int[] message) {
		switch (message[3]) {
		case Message.MSG_RICHIESTA_MODELLO : 
			return new RichiestaModelloMessage(message);
		case Message.MSG_RISPOSTA_MODELLO: 
			return new RispostaModelloMessage(message);
		case Message.MSG_VARIAZIONE_INGRESSO:
			// Non consideriamo MSG_IMPOSTAZIONE_STATO_TERMOSTATO, che ha lo
			// stesso valore, perche' non riceveremo mai messaggi di quel tipo,
			// ma li genereremo soltanto.
			return new VariazioneIngressoMessage(message);
		case Message.MSG_ACKNOWLEDGE: 
			return new AcknowledgeMessage(message);
		case Message.MSG_RICHIESTA_ASSOCIAZIONE_BROADCAST: 
			return new RichiestaAssociazioneUscitaMessage(message);
		case Message.MSG_RISPOSTA_ASSOCIAZIONE_BROADCAST: 
			return new RispostaAssociazioneUscitaMessage(message);
		case Message.MSG_COMANDO_BROADCAST: 
			return new ComandoBroadcastMessage(message);
		case Message.MSG_COMANDO_USCITA: 
			return new ComandoUscitaMessage(message);
		case Message.MSG_RICHIESTA_STATO: 
			return new RichiestaStatoMessage(message);
		case Message.MSG_RISPOSTA_STATO: 
			return new RispostaStatoMessage(message);
		case Message.MSG_CAMBIO_VELOCITA: 
			return new CambioVelocitaMessage(message);
		case Message.MSG_RICHIESTA_INGRESSO_IR: 
			return new RichiestaIngressoIRMessage(message);
		case Message.MSG_RISPOSTA_INGRESSO_IR: 
			return new RispostaIngressoIRMessage(message);
		case Message.MSG_COMANDO_USCITA_DIMMER: 
			return new ComandoUscitaDimmerMessage(message);
		case Message.MSG_RISPOSTA_STATO_DIMMER: 
			return new RispostaStatoDimmerMessage(message);
		case Message.MSG_IMPOSTA_PARAMETRO_DIMMER: 
			return new ImpostaParametroDimmerMessage(message);
		case Message.MSG_RICHIESTA_STATO_TERMOSTATO:
			return new RichiestaStatoTermostatoMessage(message);
		case Message.MSG_TEMPERATURA: 
			return new TemperatureMessage(message);
		case Message.MSG_IMPOSTA_SET_POINT:
			return new ImpostaSetPointMessage(message);
		case Message.MSG_RICHIESTA_SET_POINT:
			return new RichiestaSetPointMessage(message);
		case Message.MSG_LETTURA_SET_POINT: 
			return new CronotermMessage(message);
		default: 
			System.err.println("Messaggio di tipo sconosciuto: " + 
				message[3]);
			return null;
		}
	}

	public Message getMessage()
	{
		return message;
	}

	public boolean isValid() {
		return valid;
	}

	public void clear() {
		ibuff = 0;
		buff = new int[8];
		valid = false;
		message = null;
	}

}
