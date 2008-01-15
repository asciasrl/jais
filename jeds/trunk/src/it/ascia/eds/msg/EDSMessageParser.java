package it.ascia.eds.msg;

import java.util.Date;

/**
 * Decodifica i messaggi del protocollo EDS
 *  
 * (C) 2007 Ascia S.r.l.
 * @author Sergio Strampelli
 */
public class EDSMessageParser {
	
  protected static int Stx = 2;
  
  protected static int Etx = 3;
  
  private int[] buff = new int[8];
  
  private int ibuff = 0;
  
  private boolean valid = false;
  
  private EDSMessage message;
  
  public EDSMessageParser() {
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
   * 
   * @param message sequenza di byte che compone il messaggio
   * @return messaggio decodificato
   */
  private EDSMessage createMessage(int[] message) {
	  switch (message[3]) {
	  	case 0: return new RichiestaModelloMessage(message);
	  	case 1: return new RispostaModelloMessage(message);
	  	case 4: return new VariazioneIngressoMessage(message);
	  	case 6: return new AknowledgeMessage(message);
	  	case 17: return new BroadcastMessage(message);
	  	case 21: return new ComandoUscitaMessage(message);
	  	case 25: return new RichiestaStatoMessage(message);
	  	case 26: return new RispostaStatoMessage(message);
	  	case 27: return new CambioVelocitaMessage(message);
	  	case 39: return new RichiestaIngressoIRMessage(message);
	  	case 40: return new RispostaIngressoIRMessage(message);
	  	case ComandoUscitaDimmerMessage.TIPO: return new ComandoUscitaDimmerMessage(message);
	  	case 53: return new RispostaStatoDimmerMessage(message);
	  	case ImpostaParametroDimmerMessage.TIPO: return new ImpostaParametroDimmerMessage(message);	  	
	  	case 201: return new TemperatureMessage(message);
	  	case 205: return new CronotermMessage(message);
	  	default: return new EDSMessage(message);
	  }
  }
  
  public EDSMessage getMessage()
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
	  message = new EDSMessage();
  }
    
}
