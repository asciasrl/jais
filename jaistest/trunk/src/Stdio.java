import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Copyright (C) 2008 ASCIA S.r.l.
 */

/**
 * Fornisce alcune funzioni utili per l'input/output da terminale.
 * 
 * <p>Java e' tanto un bel linguaggio, ma fare input da terminale e' 
 * difficle.</p>
 * 
 * @author arrigo
 */
public class Stdio {

	static BufferedReader stdin = 
		new BufferedReader(new InputStreamReader(System.in));;
	
	/**
	 * Richiede un intero.
	 * 
	 * @param message messaggio da mostrare.
	 * 
	 * @return il valore inserito dall'utente.
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
	 * Richiede l'inserimento di un numero double.
	 * 
	 * @param message messaggio da mostrare.
	 * 
	 * @return il numero inserito.
	 */
	static double inputDouble(String message) {
		double retval = 0;
		boolean entered = false;
		while (!entered) {
			try {
				System.out.print(message);
				retval = Double.parseDouble(stdin.readLine());
				entered = true;
			} catch (NumberFormatException e) {
				// Inserito un input invalido. Lo ignoriamo.
			} catch (IOException e) {
			}
		}
		return retval;
	}
}
