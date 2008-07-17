/**
 * Copyright (C) 2008 ASCIA S.r.l.
 */
package it.ascia.bentel;

import it.ascia.ais.DeviceEvent;
import it.ascia.ais.DeviceListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.PropertyConfigurator;

/**
 * Test per JBisKyoUnit.
 * 
 * @author arrigo
 *
 */
public class JBisKyoTest implements DeviceListener {
	static BufferedReader stdin;
	static JBisKyoUnit b;
	static JBisKyoDevice d;
	
	/**
	 * Java avra' tanti pregi, ma l'input da stdin e' difficile.
	 */
	static int inputInteger(String message) {
		int retval = 0;
		boolean entered = false;
		while (!entered) {
			try {
				System.out.print(message);
				retval = Integer.parseInt(stdin.readLine());
				entered = true;
			} catch (NumberFormatException e) {
				// Inserito un input invalido. Lo ignoriamo.
			} catch (IOException e) {
			}
		}
		return retval;
	}
	
	/**
	 * Test del comando setOutput;
	 */
	private static void testSetOutput() {
		int port = 0;
		while (port >= 0) {
			port = inputInteger("Numero della porta (<0 esce): ");
			if (port >= 0) {
				System.out.print("On ");
				try {
					d.setOutput(port, true);
				} catch (JBisException e1) {
					System.err.println(e1.getMessage());
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					System.err.println(e.getMessage());
				}
				System.out.print(" Off");
				try {
					d.setOutput(port, false);
				} catch (JBisException e) {
					System.err.println(e.getMessage());
				}
				System.out.println(d.getStatus("*", 0));
			}
		}
	}
	
	public static void main(String[] args) {
		PropertyConfigurator.configure("conf/log4j.conf");
		stdin = new BufferedReader(new InputStreamReader(System.in));
		try {
			b = new JBisKyoUnit(1,4,"0025", "jbis");
			d = (JBisKyoDevice)(b.getDevices("0")[0]);
			d.setDeviceListener(new JBisKyoTest());
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
			// b.updateLog();
			b.start();
			testSetOutput();
			b.stop();
		} catch (JBisException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
	}

	public void statusChanged(DeviceEvent event) {
		System.err.println(event.getInfo());		
	}

	
}