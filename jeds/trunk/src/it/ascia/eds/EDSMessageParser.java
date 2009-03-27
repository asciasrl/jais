package it.ascia.eds;

import it.ascia.eds.msg.AcknowledgeMessage;
import it.ascia.eds.msg.CambioVelocitaMessage;
import it.ascia.eds.msg.ComandoBroadcastMessage;
import it.ascia.eds.msg.ComandoUscitaDimmerMessage;
import it.ascia.eds.msg.ComandoUscitaMessage;
import it.ascia.eds.msg.CronotermMessage;
import it.ascia.eds.msg.EDSMessage;
import it.ascia.eds.msg.ImpostaParametroMessage;
import it.ascia.eds.msg.ImpostaSetPointMessage;
import it.ascia.eds.msg.ProgrammazioneMessage;
import it.ascia.eds.msg.RichiestaAssociazioneUscitaMessage;
import it.ascia.eds.msg.RichiestaIngressoIRMessage;
import it.ascia.eds.msg.RichiestaModelloMessage;
import it.ascia.eds.msg.RichiestaParametroMessage;
import it.ascia.eds.msg.RichiestaSetPointMessage;
import it.ascia.eds.msg.RichiestaStatoMessage;
import it.ascia.eds.msg.RichiestaStatoTermostatoMessage;
import it.ascia.eds.msg.RichiestaUscitaMessage;
import it.ascia.eds.msg.RispostaAssociazioneUscitaMessage;
import it.ascia.eds.msg.RispostaIngressoIRMessage;
import it.ascia.eds.msg.RispostaModelloMessage;
import it.ascia.eds.msg.RispostaOpzioniIngressoMessage;
import it.ascia.eds.msg.RispostaParametroMessage;
import it.ascia.eds.msg.RispostaSetPointMessage;
import it.ascia.eds.msg.RispostaStatoDimmerMessage;
import it.ascia.eds.msg.RispostaStatoMessage;
import it.ascia.eds.msg.RispostaStatoTermostatoMessage;
import it.ascia.eds.msg.RispostaUscitaMessage;
import it.ascia.eds.msg.UnknowMessage;
import it.ascia.eds.msg.VariazioneIngressoMessage;

/**
 * Decodifica i messaggi del protocollo EDS
 *  
 * (C) 2007,2009 Ascia S.r.l.
 * @author Sergio Strampelli, arrigo
 */
public class EDSMessageParser extends it.ascia.ais.MessageParser {
	
	protected static int Stx = 2;

	protected static int Etx = 3;

	private int[] buff = new int[8];

	private int ibuff = 0;

	protected long lastReceived;
	
	public static long TIMEOUT = 1000;
	
	public EDSMessageParser() {
		super();
		clear();
	}

	public String dumpBuffer() {
		StringBuffer s = new StringBuffer();
		s.append("i="+ibuff+" ");
		s.append("STX:0x"+Integer.toHexString(buff[0])+" ");
		s.append("DST:0x"+Integer.toHexString(buff[1])+" ");
		s.append("MIT:0x"+Integer.toHexString(buff[2])+" ");
		s.append("TIP:0x"+Integer.toHexString(buff[3])+" ");
		s.append("BY1:0x"+Integer.toHexString(buff[4])+" ");
		s.append("BY2:0x"+Integer.toHexString(buff[5])+" ");
		s.append("CHK:0x"+Integer.toHexString(buff[6])+" ");
		s.append("ETX:0x"+Integer.toHexString(buff[7])+" ");
		return s.toString();
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
		if (ibuff > 0 && ! valid && (System.currentTimeMillis() - lastReceived) >= TIMEOUT ) {
			logger.warn("Timeout in ricezione");
			clear();
		}
		lastReceived = System.currentTimeMillis();
		//logger.warn("ibuff="+ibuff+ " b = "+b);
		buff[ibuff++] = b;
		// verifica che il primo byte sia Stx, altrimenti lo scarta
		if (ibuff == 1) {
			if (b != Stx) {
				ibuff = 0;
				logger.warn("Non Stx:" + b);
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
				logger.warn("Errore checksum");
				logger.debug(dumpBuffer());
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
	private EDSMessage createMessage(int[] message) {
		switch (message[3]) {
		case EDSMessage.MSG_RICHIESTA_MODELLO : 
			return new RichiestaModelloMessage(message);
		case EDSMessage.MSG_RISPOSTA_MODELLO: 
			return new RispostaModelloMessage(message);
		case EDSMessage.MSG_VARIAZIONE_INGRESSO:
			// Non consideriamo MSG_IMPOSTAZIONE_STATO_TERMOSTATO, che ha lo
			// stesso valore, perche' non riceveremo mai messaggi di quel tipo,
			// ma li genereremo soltanto.
			return new VariazioneIngressoMessage(message);
		case EDSMessage.MSG_ACKNOWLEDGE: 
			return new AcknowledgeMessage(message);
		case EDSMessage.MSG_RICHIESTA_USCITA:
			return new RichiestaUscitaMessage(message);
		case EDSMessage.MSG_RISPOSTA_USCITA:
			return new RispostaUscitaMessage(message);
		case EDSMessage.MSG_RICHIESTA_ASSOCIAZIONE_BROADCAST: 
			return new RichiestaAssociazioneUscitaMessage(message);
		case EDSMessage.MSG_RISPOSTA_ASSOCIAZIONE_BROADCAST: 
			return new RispostaAssociazioneUscitaMessage(message);
		case EDSMessage.MSG_COMANDO_BROADCAST: 
			return new ComandoBroadcastMessage(message);
		case EDSMessage.MSG_COMANDO_USCITA: 
			return new ComandoUscitaMessage(message);
		case EDSMessage.MSG_RISPOSTA_OPZIONI_INGRESSO: 
			return new RispostaOpzioniIngressoMessage(message);			
		case EDSMessage.MSG_RICHIESTA_STATO: 
			return new RichiestaStatoMessage(message);
		case EDSMessage.MSG_RISPOSTA_STATO: 
			return new RispostaStatoMessage(message);
		case EDSMessage.MSG_CAMBIO_VELOCITA: 
			return new CambioVelocitaMessage(message);
		case EDSMessage.MSG_PROGRAMMAZIONE:
			return new ProgrammazioneMessage(message);
		case EDSMessage.MSG_RICHIESTA_INGRESSO_IR: 
			return new RichiestaIngressoIRMessage(message);
		case EDSMessage.MSG_RISPOSTA_INGRESSO_IR: 
			return new RispostaIngressoIRMessage(message);
		case EDSMessage.MSG_COMANDO_USCITA_DIMMER: 
			return new ComandoUscitaDimmerMessage(message);
		case EDSMessage.MSG_RISPOSTA_STATO_DIMMER: 
			return new RispostaStatoDimmerMessage(message);
		case EDSMessage.MSG_IMPOSTA_PARAMETRO: 
			return new ImpostaParametroMessage(message);
		case EDSMessage.MSG_RICHIESTA_PARAMETRO:
			return new RichiestaParametroMessage(message);
		case EDSMessage.MSG_RISPOSTA_PARAMETRO:
			return new RispostaParametroMessage(message);			
		case EDSMessage.MSG_RICHIESTA_STATO_TERMOSTATO:
			return new RichiestaStatoTermostatoMessage(message);
		case EDSMessage.MSG_RISPOSTA_STATO_TERMOSTATO: 
			return new RispostaStatoTermostatoMessage(message);
		case EDSMessage.MSG_IMPOSTA_SET_POINT:
			return new ImpostaSetPointMessage(message);
		case EDSMessage.MSG_RICHIESTA_SET_POINT:
			return new RichiestaSetPointMessage(message);
		case EDSMessage.MSG_LETTURA_SET_POINT: 
			return new CronotermMessage(message);
		case EDSMessage.MSG_RISPOSTA_SET_POINT: 
			return new RispostaSetPointMessage(message);
		default: 
			return new UnknowMessage(message);
		}
	}

	public void clear() {
		ibuff = 0;
		buff = new int[8];
		valid = false;
		message = null;
	}
	
	public boolean isBusy() {
		return  (ibuff > 0 && ! valid && (System.currentTimeMillis() - lastReceived) < TIMEOUT);
	}

}
