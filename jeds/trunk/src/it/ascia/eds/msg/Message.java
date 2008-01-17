/**
 * Copyright (C) 2007 ASCIA S.R.L.
 */
package it.ascia.eds.msg;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Messaggio EDS generico.
 * 
 * @author sergio, arrigo
 */
public class Message
  	implements MessageInterface {
	
  protected int[] rawmessage;
  
  protected static int Stx = 2;
  
  protected static int Etx = 3;
  
  protected int Destinatario;
  
  protected int Mittente;
  
  protected int TipoMessaggio;
  
  protected int Byte1;
  protected int Byte2;
    
  public int[] getRawMessage() {
	  int message[] = new int[8];
	  message[0] = Stx;
	  message[1] = Destinatario;
	  message[2] = Mittente;
	  message[3] = TipoMessaggio;
	  message[4] = Byte1;
	  message[5] = Byte2;
	  message[6] = checkSum();
	  message[7] = Etx;
	  return message; 
  }
  
  public int getRecipient() {
	  return Destinatario;
  }
  
  public int getSender() {
	  return Mittente;
  }
  
  public byte[] getBytesMessage() {
	  byte message[] = new byte[8];
	  message[0] = (new Integer(Stx)).byteValue();
	  message[1] = (new Integer(Destinatario)).byteValue();
	  message[2] = (new Integer(Mittente)).byteValue();
	  message[3] = (new Integer(TipoMessaggio)).byteValue();
	  message[4] = (new Integer(Byte1)).byteValue();
	  message[5] = (new Integer(Byte2)).byteValue();
	  message[6] = (new Integer(checkSum())).byteValue();
	  message[7] = (new Integer(Etx)).byteValue();
	  return message; 
  }

  public void write(OutputStream out) throws IOException {
	  out.write(getBytesMessage());
  }
  
  public int checkSum() {
	  return (new Integer((Stx+Destinatario+Mittente+TipoMessaggio+Byte1+Byte2) & 0xff)).byteValue();
  }
  
  public String toString() {
	  StringBuffer s = new StringBuffer();
	  if (rawmessage == null) {
		  rawmessage = getRawMessage();
	  }
	  s.append(toHexString() + "\r\n");
	  s.append("Tipo Messaggio: "+getTipoMessaggio()+"\r\n");
	  s.append(getInformazioni());
	  return s.toString();
  }
  
  public String toHexString()
  {
	  StringBuffer s = new StringBuffer();
	  s.append("STX:"+b2h(rawmessage[0])+" ");
	  s.append("DST:"+b2h(rawmessage[1])+" ");
	  s.append("MIT:"+b2h(rawmessage[2])+" ");
	  s.append("TIP:"+b2h(rawmessage[3])+" ");
	  s.append("BY1:"+b2h(rawmessage[4])+" ");
	  s.append("BY2:"+b2h(rawmessage[5])+" ");
	  s.append("CHK:"+b2h(rawmessage[6])+" ");
	  s.append("ETX:"+b2h(rawmessage[7])+" ");
	  return s.toString();
  }
  
  protected String b2h(int i)
  {
	  String s = "0x";
	  if (i < 16) {
		  s += "0";
	  }
	  s += Integer.toHexString(i);
	  return s;
  }
  
  public String getTipoMessaggio() {
	  return "Unknown ("+TipoMessaggio+")";
  }
  
  public String getInformazioni()
  {
	  StringBuffer s = new StringBuffer();
	  s.append("Mittente: "+Mittente+"\r\n");
	  s.append("Destinatario: "+Destinatario+"\r\n");
	  s.append("Byte1: "+Byte1+"\r\n");
	  s.append("Byte2: "+Byte2+"\r\n");
	  return s.toString();
  }
  
  public Message() {  
  }

  public Message(int[] message) {
	  parseMessage(message);
  }
  
  public void parseMessage(int[] message) {
	  rawmessage = message;
	  Destinatario = message[1];
	  Mittente = message[2];
	  TipoMessaggio = message[3];
	  Byte1 = message[4];
	  Byte2 = message[5];
  }
  
  /**
   * Is this message for everybody?
   * 
   * @return true if this message is broadcast
   */
  public boolean isBroadcast() {
	  System.err.println("Calling isBroadcast() on a generic message!");
	  return false;
  }
  
}
