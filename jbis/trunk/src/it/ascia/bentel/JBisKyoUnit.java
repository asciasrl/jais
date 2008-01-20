package it.ascia.bentel;


public class JBisKyoUnit {

	byte seriale;  // Numero porta seriale (1 - COM1 ... 4 - COM4)
	
	byte tentativi; // Numero di ritentativi in caso di errore di comunicazione (result = 3)
	
	String PIN; 
	
	/**
	 * 
	 * @param s Numero porta seriale (1 - COM1 ... 4 - COM4)
	 * @param t Numero di ritentativi in caso di errore di comunicazione (result = 3)
	 * @param p PIN di accesso alle funzioni con password
	 */
	JBisKyoUnit(int s,int t,String p)
	{
		seriale = (byte)s;
		tentativi = (byte)t;
		PIN = p;
	}
	
	public static void main(String[] args) {

        System.out.println("Bentel GW (C) Ascia S.r.l. 2007-2008");
        
    	JBisKyoUnit b = new JBisKyoUnit(1,1,"0025");
    	byte[] data;
        //while (true) {
        	data = b.Leggi(0x0304);
        
	        //data = b.Leggi(0x00000383);
	                
			for (int i = 0; i < 5; i++) {
				System.out.print(" 0x"+(data[i] & 0xff));
				//System.out.print(" 0x"+data[i]);
			}
			System.out.println("");
			/*try {
				Thread.sleep(500);
			} catch (InterruptedException e) {}
			*/
        //}
	}

    byte[] Leggi(int comando) {
		Integer[] res1 = new Integer[1];
		res1[0] = Integer.valueOf(comando);
		System.out.printf("Comando: 0x%h\r\n",res1);
    	byte[] data;
    	data = new byte[512];
    	byte result = PanelConnection(comando, seriale, tentativi, PIN, data);
    	if (result > 0) {
    		Byte[] res = new Byte[1];
    		res[0] = Byte.valueOf(result);
    		System.out.printf("Errore %d\r\n",res);
    		//data = new byte[0];
    	}
    	return data;
    }

    static
    {
        System.loadLibrary("JBisKyoUnit");
    }

    /**
     * 
     * 0: Operazione andata a buon fine
     * 1: Codice utente non valido
     * 2: Errore apertura porta seriale
     * 3: Errore di comunicazione
     * 4: Comando sconosciuto
     * 5: Tipo centrale non riconosciuto
     * 6: Versione firmware non riconosciuto
     * 7: Aree inserite, impossibile aprire sessione di programmazione
     * 8: Sessione di programmazione già aperta
     * 9: Dati forniti per la scrittura non validi
     * 10: Errore chiusura sessione di programmazione
     * 11: Codice utente non abilitato
     * 
     * @param comando
     * @param seriale
     * @param tentativi
     * @param PIN
     * @param PINLength
     * @param data
     * @return
     */
    private static native byte PanelConnection(int comando, byte seriale, byte tentativi, String PIN, byte[] data);

}
