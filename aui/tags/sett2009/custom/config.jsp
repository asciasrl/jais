<%
/**
 * Copyright (C) 2008 ASCIA S.r.l.
 *
 * <p>Questo file contiene le informazioni di configurazione di AUI
 * personalizzate per un cliente.</p>
 */
%>
<%@ page import="java.util.Map, java.util.HashMap, java.util.Vector" %>
<%!
/**
 * Posizione iniziale della appBar [pixel].
 *
 * <p>Se la appBar non scorre, questo numero deve indicare la posizione della
 * icona da selezionare all'avvio di AUI.</p>
 */
final int APPBAR_START_POSITION = 40;

/**
 * True se la appbar non deve fare lo scrolling.
 */
final boolean APPBAR_SIMPLE = false;

/**
 * Lista dei servizi.
 */
final String apps[] = {"illuminazione", "serramenti", "sicurezza",
	"video", "clima", "audio", "energia", "scenari"};

/**
 * Altezza della "parte utile" dello slider del dimmer [pixel].
 *
 * <p>Questa deve essere l'altezza dentro la quale si puo' spostare il cursore
 * del dimmer.</p>
 */
final int DIMMER_SLIDER_HEIGHT = 100;

/**
 * Immagine che mostra i piani.
 */
final String pianiFile = "custom/images/assonometria320x370.png"; // images/piani-all.png";
/// Larghezza, altezza
final int pianiSize[] = {IPOD_VIEWPORT_WIDTH, 370};

/**
 * Lista dei piani.
 */
Vector piani = new Vector();
Map piano01a = new HashMap();

/**
 * Popola la lista dei piani.
 */
void initPiani() {
	piano01a.put("id", "piano-01A");
	piano01a.put("header", "Piano 1A");
	piano01a.put("mapFile", "custom/images/planimetria.png");
	int size[] = {IPOD_VIEWPORT_WIDTH, 299};
	piano01a.put("mapSize", size);
	piano01a.put("bigMapFile", "custom/images/planimetria-big.png");
	int bigSize[] = {800, 748};
	piano01a.put("bigMapSize", bigSize);
	piani.add(piano01a);
}

/**
 * Luci presenti nel sistema.
 * 
 * <p>Questa array di ID verra' popolata in fase di creazione delle luci.</p>
 */
Vector idLuci = new Vector();

/**
 * Frame: illuminazione.
 * 
 * <p>Gli indici sono gli ID dei piani.</p>
 */
Map frameIlluminazione = new HashMap();
 
/**
 * Popola la lista di luci.
 */
void initLuci() {
	Map illuminazionePiano01a = new HashMap();
	Map p1aLuce1 = new HashMap();
	p1aLuce1.put("type", ILL_LUCE);
	p1aLuce1.put("x", new Integer(100));
	p1aLuce1.put("y", new Integer(100));
	p1aLuce1.put("label", "Applique");
	p1aLuce1.put("address", "0.3:Out1");
	illuminazionePiano01a.put("p1a-luce1", p1aLuce1);
	Map p1aLuce2 = new HashMap();
	p1aLuce2.put("type", ILL_LUCE);
	p1aLuce2.put("x", new Integer(300));
	p1aLuce2.put("y", new Integer(100));
	p1aLuce2.put("label", "Luce pitosforo");
	p1aLuce2.put("address", "0.3:Out2");
	illuminazionePiano01a.put("p1a-luce2", p1aLuce2);
	Map p1aDimmer1 = new HashMap();
	p1aDimmer1.put("type", ILL_DIMMER);
	p1aDimmer1.put("x", new Integer(100));
	p1aDimmer1.put("y", new Integer(340));
	p1aDimmer1.put("label", "Dimmer allarme");
	p1aDimmer1.put("address", "0.5:Out1");
	illuminazionePiano01a.put("p1a-dimmer1", p1aDimmer1);
	Map p1aDimmer2 = new HashMap();
	p1aDimmer2.put("type", ILL_DIMMER);
	p1aDimmer2.put("x", new Integer(300));
	p1aDimmer2.put("y", new Integer(340));
	p1aDimmer2.put("label", "Dimmer BMC virtuale");
	p1aDimmer2.put("address", "0.5:Out2");
	illuminazionePiano01a.put("p1a-dimmer2", p1aDimmer2);
	frameIlluminazione.put("piano-01A", illuminazionePiano01a);
}

/**
 * Prese comandate presenti nel sistema.
 * 
 * <p>Questa array di ID verra' popolata in fase di creazione delle prese
 * comandate.</p>
 */
Vector idPrese = new Vector();

/**
 * Frame: energia.
 * 
 * <p>Gli indici sono gli ID dei piani.</p>
 */
Map frameEnergia = new HashMap();

/**
 * Popola il frame Energia.
 */
void initEnergia() {
	Map energiaPiano01a = new HashMap();
	Map p1aPresa1 = new HashMap();
	p1aPresa1.put("x", new Integer(200));
	p1aPresa1.put("y", new Integer(200));
	p1aPresa1.put("label", "Presa 1");
	p1aPresa1.put("address", "0.3:Out3");
	energiaPiano01a.put("p1a-presa1", p1aPresa1);
	Map p1apresa2 = new HashMap();
	p1apresa2.put("x", new Integer(400));
	p1apresa2.put("y", new Integer(200));
	p1apresa2.put("label", "Presa pitosforo");
	p1apresa2.put("address", "0.3:Out4");
	energiaPiano01a.put("p1a-presa2", p1apresa2);
	Map p1aPresa3 = new HashMap();
	p1aPresa3.put("x", new Integer(200));
	p1aPresa3.put("y", new Integer(400));
	p1aPresa3.put("label", "Presa lavatrice");
	p1aPresa3.put("address", "0.5:Out5");
	energiaPiano01a.put("p1a-presa3", p1aPresa3);
	frameEnergia.put("piano-01A", energiaPiano01a);
}

/**
 * Termostati presenti nel sistema.
 * 
 * <p>Questa array di ID verra' popolata in fase di creazione delle prese
 * comandate.</p>
 */
Vector idClimi = new Vector();

/**
 * Frame: clima.
 * 
 * <p>Gli indici sono gli ID dei piani.</p>
 */

Map frameClima = new HashMap();
 
/**
 * Popola il frame Clima.
 */
void initClima() {
	Map climaPiano01a = new HashMap();
	Map p1aClima1 = new HashMap();
	p1aClima1.put("x", new Integer(300));
	p1aClima1.put("y", new Integer(200));
	p1aClima1.put("label", "Termostato a vapore");
	p1aClima1.put("address", "0.3:Out6");
	climaPiano01a.put("p1a-clima1", p1aClima1);
	Map p1aClima2 = new HashMap();
	p1aClima2.put("x", new Integer(400));
	p1aClima2.put("y", new Integer(400));
	p1aClima2.put("label", "Bruciatore");
	p1aClima2.put("address", "0.3:Out7");
	climaPiano01a.put("p1a-clima2", p1aClima2);
	frameClima.put("piano-01A", climaPiano01a);
}

/**
 * Tapparelle collegate al sistema.
 * 
 * <p>Questa array di ID verra' popolata in fase di creazione delle 
 * tapparelle.</p>
 */
Vector idSerramenti = new Vector();

/**
 * Frame: serramenti.
 * 
 * <p>Gli indici sono gli ID dei piani.</p>
 */
Map frameSerramenti = new HashMap();
 
/** 
 * Popola il frame serramenti.
 */
void initSerramenti() {
	Map serramentiPiano01a = new HashMap();
	Map p1aSerr1 = new HashMap();
	p1aSerr1.put("x", new Integer(460));
	p1aSerr1.put("y", new Integer(40));
	p1aSerr1.put("label", "Tapparella verde");
	p1aSerr1.put("addressopen", "0.3:Out1");
	p1aSerr1.put("addressclose", "0.3:Out2");
	serramentiPiano01a.put("p1a-serr1", p1aSerr1);
	frameSerramenti.put("piano-01A", serramentiPiano01a);
}

/**
 * Schermi e altre cose "video" collegate al sistema.
 * 
 * <p>Questa array di ID verra' popolata in fase di creazione degli elementi 
 * del layer "video".</p>
 */
Vector idVideo = new Vector();

/**
 * Frame: video.
 * 
 * <p>Gli indici sono gli ID dei piani.</p>
 */
Map frameVideo = new HashMap();

 /**
 * Popola il frame Video.
 */
void initVideo() {	
	Map videoPiano01a = new HashMap();
	Map p1aSchermo1 = new HashMap();
	p1aSchermo1.put("x", new Integer(295));
	p1aSchermo1.put("y", new Integer(70));
	p1aSchermo1.put("label", "Schermo");
	p1aSchermo1.put("addressopen", "0.5:Out1");
	p1aSchermo1.put("addressclose", "0.5:Out2");
	videoPiano01a.put("p1a-schermo1", p1aSchermo1);
	frameVideo.put("piano-01A", videoPiano01a);
}
 
/**
 * Allarmi gestiti dal sistema.
 * 
 * <p>Questa array di ID verra' popolata in fase di creazione degli elementi 
 * del layer "sicurezza".</p>
 */
Vector idAllarmi = new Vector();

/**
 * Frame: sicurezza.
 * 
 * <p>Gli indici sono gli ID dei piani.</p>
 */
Map frameSicurezza = new HashMap();
 
/**
 * Popola il frame sicurezza.
 */
void initSicurezza() {
	Map sicurezzaPiano01a = new HashMap();
	Map p1aPorta1 = new HashMap();
	p1aPorta1.put("type", SIC_PORTA); 
	p1aPorta1.put("x", new Integer(525));
	p1aPorta1.put("y", new Integer(325));
	p1aPorta1.put("label", "Porta");
	sicurezzaPiano01a.put("p1a-porta1", p1aPorta1);
	Map p1aAllarme1 = new HashMap();
	p1aAllarme1.put("type", SIC_LUCCHETTO); 
	p1aAllarme1.put("x", new Integer(365));
	p1aAllarme1.put("y", new Integer(342));
	p1aAllarme1.put("label", "Allarme");
	sicurezzaPiano01a.put("p1a-allarme1", p1aAllarme1);
	frameSicurezza.put("piano-01A",  sicurezzaPiano01a);
}

/**
 * True se tutto è già stato inizializzato.
 */
boolean initialized = false;

/**
 * Inizializza tutto, se tutto non è ancora stato inizializzato.
 */
synchronized void initAUI() {
	if (initialized) {
		return;
	}
	initPiani();
	initLuci();
	initEnergia();
	initClima();
	initSerramenti();
	initVideo();
	initSicurezza();
	initialized = true;
}
%>