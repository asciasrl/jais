/**
 * (C) 2007,2009 Ascia S.r.l.
 * 
 */
package it.ascia.eds;

import it.ascia.eds.msg.AcknowledgeMessage;
import it.ascia.eds.msg.CambioVelocitaMessage;
import it.ascia.eds.msg.ComandoBroadcastMessage;
import it.ascia.eds.msg.ComandoUscitaDimmerMessage;
import it.ascia.eds.msg.ComandoUscitaMessage;
import it.ascia.eds.msg.CronotermMessage;
import it.ascia.eds.msg.EDSMessage;
import it.ascia.eds.msg.ImpostaParametroMessage;
import it.ascia.eds.msg.ImpostaRTCCMessage;
import it.ascia.eds.msg.ImpostaSetPointMessage;
import it.ascia.eds.msg.ProgrammazioneMessage;
import it.ascia.eds.msg.RichiestaAssociazioneUscitaMessage;
import it.ascia.eds.msg.RichiestaIngressoIRMessage;
import it.ascia.eds.msg.RichiestaModelloMessage;
import it.ascia.eds.msg.RichiestaParametroMessage;
import it.ascia.eds.msg.RichiestaRTCCMessage;
import it.ascia.eds.msg.RichiestaSetPointMessage;
import it.ascia.eds.msg.RichiestaStatoMessage;
import it.ascia.eds.msg.RichiestaStatoTermostatoMessage;
import it.ascia.eds.msg.RichiestaUscitaMessage;
import it.ascia.eds.msg.RispostaAssociazioneUscitaMessage;
import it.ascia.eds.msg.RispostaIngressoIRMessage;
import it.ascia.eds.msg.RispostaModelloMessage;
import it.ascia.eds.msg.RispostaOpzioniIngressoMessage;
import it.ascia.eds.msg.RispostaParametroMessage;
import it.ascia.eds.msg.RispostaRTCCMessage;
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
 * @author Sergio Strampelli, arrigo
 */
public class EDSMessageParser extends it.ascia.ais.MessageParser {
	
	protected static int Stx = 2;

	protected static int Etx = 3;

	private int[] buff = new int[8];

	protected long lastReceived;
	
	public long TIMEOUT = 100;
	
	public EDSMessageParser() {
		super();
		clear();
	}

	private void clear() {
		buff = new int[8];
		for (int i=0; i < 8; i++) {
			buff[i] = -1;
		}
		lastReceived = 0;
	}
	
	public boolean isBusy() {
		return (!valid) && ((System.currentTimeMillis() - lastReceived) < TIMEOUT);
	}

	public String dumpBuffer() {
		StringBuffer s = new StringBuffer();
		s.append("STX:0x"+Integer.toHexString(buff[0])+" ");
		s.append("DST:0x"+Integer.toHexString(buff[1])+" ");
		s.append("MIT:0x"+Integer.toHexString(buff[2])+" ");
		s.append("TYP:0x"+Integer.toHexString(buff[3])+" ");
		s.append("BY1:0x"+Integer.toHexString(buff[4])+" ");
		s.append("BY2:0x"+Integer.toHexString(buff[5])+" ");
		s.append("CHK:0x"+Integer.toHexString(buff[6])+" ");
		s.append("ETX:0x"+Integer.toHexString(buff[7])+" ");
		return s.toString();
	}

	/**
	 * Calcola il checksum del buffer
	 * @return checksum
	 */
	private int checksum() {
		int chk = 0;
		for (int i = 0; i < 6; i++) {
			chk = (chk + buff[i] & 0xFF) & 0xFF;
		}
		return chk;
	}
	
	public void push(int b) {
		b = b & 0xFF;
		// TODO Implementazione inefficiente di buffer circolare: vale la pena ottimizzarlo ?
		for (int i = 1; i < 8; i++) {
			buff[i-1] = buff[i];
		}
		buff[7] = b;
		if (!valid && (lastReceived > 0) && (System.currentTimeMillis() - lastReceived) >= TIMEOUT ) {
			logger.error("Timeout in ricezione "+(System.currentTimeMillis() - lastReceived));
		}
		lastReceived = System.currentTimeMillis();
		if ((buff[0] == Stx) && (buff[7] == Etx)) {
			if (buff[6] == checksum()) {
				valid = true;
				message = createMessage();
				clear();
			} else {
				logger.warn("Checksum error");
				valid = false;
			}
		} else {
			valid = false;
		}
	}

	/**
	 * Metodo factory per creare messaggi in base al contenuto del buffer
	 * 
	 * TODO Realizzare una Factory dinamica
	 * @return messaggio decodificato
	 */
	private EDSMessage createMessage() {
		switch (buff[3]) {
		case EDSMessage.MSG_RICHIESTA_MODELLO : 
			return new RichiestaModelloMessage(buff);
		case EDSMessage.MSG_RISPOSTA_MODELLO: 
			return new RispostaModelloMessage(buff);
		case EDSMessage.MSG_VARIAZIONE_INGRESSO:
			// Non consideriamo MSG_IMPOSTAZIONE_STATO_TERMOSTATO, che ha lo
			// stesso valore, perche' non riceveremo mai messaggi di quel tipo,
			// ma li genereremo soltanto.
			return new VariazioneIngressoMessage(buff);
		case EDSMessage.MSG_ACKNOWLEDGE: 
			return new AcknowledgeMessage(buff);
		case EDSMessage.MSG_RICHIESTA_USCITA:
			return new RichiestaUscitaMessage(buff);
		case EDSMessage.MSG_RISPOSTA_USCITA:
			return new RispostaUscitaMessage(buff);
		case EDSMessage.MSG_RICHIESTA_ASSOCIAZIONE_BROADCAST: 
			return new RichiestaAssociazioneUscitaMessage(buff);
		case EDSMessage.MSG_RISPOSTA_ASSOCIAZIONE_BROADCAST: 
			return new RispostaAssociazioneUscitaMessage(buff);
		case EDSMessage.MSG_COMANDO_BROADCAST: 
			return new ComandoBroadcastMessage(buff);
		case EDSMessage.MSG_COMANDO_USCITA: 
			return new ComandoUscitaMessage(buff);
		case EDSMessage.MSG_RISPOSTA_OPZIONI_INGRESSO: 
			return new RispostaOpzioniIngressoMessage(buff);			
		case EDSMessage.MSG_RICHIESTA_STATO: 
			return new RichiestaStatoMessage(buff);
		case EDSMessage.MSG_RISPOSTA_STATO: 
			return new RispostaStatoMessage(buff);
		case EDSMessage.MSG_CAMBIO_VELOCITA: 
			return new CambioVelocitaMessage(buff);
		case EDSMessage.MSG_PROGRAMMAZIONE:
			return new ProgrammazioneMessage(buff);
		case EDSMessage.MSG_RICHIESTA_INGRESSO_IR: 
			return new RichiestaIngressoIRMessage(buff);
		case EDSMessage.MSG_RISPOSTA_INGRESSO_IR: 
			return new RispostaIngressoIRMessage(buff);
		case EDSMessage.MSG_COMANDO_USCITA_DIMMER: 
			return new ComandoUscitaDimmerMessage(buff);
		case EDSMessage.MSG_RISPOSTA_STATO_DIMMER: 
			return new RispostaStatoDimmerMessage(buff);
		case EDSMessage.MSG_IMPOSTA_PARAMETRO: 
			return new ImpostaParametroMessage(buff);
		case EDSMessage.MSG_RICHIESTA_PARAMETRO:
			return new RichiestaParametroMessage(buff);
		case EDSMessage.MSG_RISPOSTA_PARAMETRO:
			return new RispostaParametroMessage(buff);			
		case EDSMessage.MSG_RICHIESTA_STATO_TERMOSTATO:
			return new RichiestaStatoTermostatoMessage(buff);
		case EDSMessage.MSG_RISPOSTA_STATO_TERMOSTATO: 
			return new RispostaStatoTermostatoMessage(buff);
		case EDSMessage.MSG_IMPOSTA_SET_POINT:
			return new ImpostaSetPointMessage(buff);
		case EDSMessage.MSG_RICHIESTA_SET_POINT:
			return new RichiestaSetPointMessage(buff);
		case EDSMessage.MSG_LETTURA_SET_POINT: 
			return new CronotermMessage(buff);
		case EDSMessage.MSG_RISPOSTA_SET_POINT: 
			return new RispostaSetPointMessage(buff);
		case EDSMessage.MSG_RICHIESTA_RTCC: 
			return new RichiestaRTCCMessage(buff);
		case EDSMessage.MSG_RISPOSTA_RTCC: 
			return new RispostaRTCCMessage(buff);
		case EDSMessage.MSG_IMPOSTA_RTCC:
			return new ImpostaRTCCMessage(buff);
		default: 
			return new UnknowMessage(buff);
		}
	}

}
