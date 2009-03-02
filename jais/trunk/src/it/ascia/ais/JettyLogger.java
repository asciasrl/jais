/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.ais;

/**
 * Interfaccia di logging per Jetty.
 * 
 * <p>Questa classe fa da ponte tra Jetty e Log4J.</p>
 * 
 * <p>Purtroppo, l'interfaccia org.mortbay.log.Logger non e' commentata bene.
 * Ciononostante, questa classe sembra funzionare.</p>
 * 
 * @author arrigo
 */
public class JettyLogger implements org.mortbay.log.Logger  {
	/**
	 * Il nostro logger (quello vero :-).
	 */
	private org.apache.log4j.Logger logger;
	/**
	 * Variabile interna di Jetty.
	 */
	private boolean debugging;
	
	/**
	 * Concatena due oggetti, se non sono nulli.
	 */
	private String joinObjects(Object arg1, Object arg2) {
		String retval = "  ";
		if (arg1 != null) {
			retval += arg1.toString();
		}
		retval += "  ";
		if (arg2 != null) {
			retval += arg2.toString();
		}
		return retval;
	}
	
	public JettyLogger() {
		logger = org.apache.log4j.Logger.getLogger(getClass());
		// Il funzionamento interno di Jetty ci interessa ?
		debugging = true;
	}
	
	public void debug(String arg0, Object arg1, Object arg2) {
		if (isDebugEnabled()) {
			logger.trace(arg0 + joinObjects(arg1, arg2));
		}
	}

	public org.mortbay.log.Logger getLogger(String arg0) {
		return this;
	}

	public void info(String arg0, Object arg1, Object arg2) {
		logger.info(arg0 + joinObjects(arg1, arg2));
	}

	public boolean isDebugEnabled() {
		return debugging;
	}

	public boolean isTraceEnabled() {
		return debugging;
	}

	public void setDebugEnabled(boolean arg0) {
		debugging = arg0;
	}

	public void warn(String arg0, Throwable arg1) {
		logger.warn(arg0, arg1);
	}

	public void warn(String arg0, Object arg1, Object arg2) {
		logger.warn(arg0 + joinObjects(arg1, arg2));
	}

	public void debug(String arg0, Throwable arg1) {
		if (isDebugEnabled()) {
			logger.debug(arg0, arg1);
		}
	}
}