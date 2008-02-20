/**
 * Copyright (C) 2008 ASCIA S.r.l.
 */
package it.ascia.ais;

/**
 * Un oggetto che riceve eventi da una centralina di allarme.
 * 
 * @author arrigo
 *
 */
public interface AlarmReceiver {
	/**
	 * Ricevuto un allarme.
	 * 
	 * @param alarm identificativo dell'allarme.
	 */
	public void alarmReceived(String alarm);
}
