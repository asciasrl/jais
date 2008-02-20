/**
 * Copyright (C) 2008 ASCIA S.r.l.
 */
package it.ascia.bentel;

import it.ascia.ais.AlarmReceiver;

import java.io.IOException;

import org.apache.log4j.PropertyConfigurator;

/**
 * Test per JBisKyoUnit.
 * 
 * @author arrigo
 *
 */
public class JBisKyoTest implements AlarmReceiver{

	public static void main(String[] args) {
		PropertyConfigurator.configure("conf/log4j.conf");
		JBisKyoUnit b;
		try {
			b = new JBisKyoUnit(1,1,"0025", new JBisKyoTest());
			/*b.updateStatus();
			if (b.hasAlarms()) {
				System.out.println("Allarmi:");
				if (b.hasZoneAlarms()) {
					System.out.println(" Zone:");
					for (int i = 1; i <= b.ZONES_NUM; i++) {
						if (b.hasZoneAlarm(i)) {
							System.out.println("  " + i);
						}
					}
				}
				if (b.hasAreaAlarms()) {
					System.out.println(" Aree:");
					for (int i = 1; i <= b.AREAS_NUM; i++) {
						if (b.hasAreaAlarm(i)) {
							System.out.println("  " + i);
						}
					}
				}
			} // Allarmi
			if (b.hasSabotages()) {
				System.out.println("Sabotaggi:");
				if (b.hasZoneSabotages()) {
					System.out.println(" zone:");
					for (int i = 1; i <= b.ZONES_NUM; i++) {
						if (b.hasZoneSabotage(i)) {
							System.out.println("  " + i);
						}
					}
				}
				if (b.hasOtherSabotages()) {
					System.out.println(" altro:");
					LinkedList l = b.getOtherSabotages();
					Iterator it = l.iterator();
					while (it.hasNext()) {
						System.out.println("  " + it.next());
					}
				}
			} // Sabotaggi
			if (b.hasWarnings()) {
				System.out.println("Warning:");
				LinkedList l = b.getWarnings();
				Iterator it = l.iterator();
				while (it.hasNext()) {
					System.out.println(" " + it.next());
				}
			} // Warning */
			System.out.println();
			System.out.println("Lettura log...");
			b.updateLog();
			b.start();
			System.out.println("Premi ENTER per uscire.");
			try {
				System.in.read();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			b.stop();
		} catch (JBisException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
	}

	public void alarmReceived(String alarm) {
		System.err.println("ALLARME: " + alarm);
	}

	
}