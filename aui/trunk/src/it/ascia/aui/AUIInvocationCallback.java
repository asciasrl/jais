/**
 * 
 */
package it.ascia.aui;

import it.ascia.ais.AISException;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import org.jabsorb.callback.InvocationCallback;

/**
 * Questa callback gestisce le autorizzazioni all'utilizzo dei metodi di
 * AUIRPCServer
 * 
 * @author Sergio
 * 
 */
public class AUIInvocationCallback implements InvocationCallback {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1484973287251876354L;
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jabsorb.callback.InvocationCallback#preInvoke(java.lang.Object,
	 * java.lang.Object, java.lang.reflect.AccessibleObject, java.lang.Object[])
	 */
	public void preInvoke(Object context, Object instance, AccessibleObject m,
			Object[] arguments) throws Exception {
		// TODO remove log
		Logger logger = Logger.getLogger(getClass());
		logger.trace("Preinvoke instance: " + instance);
		String method = ((Method) m).getName();
		// TODO utilizzare framework di autenticazione (JAAS ?)
		if (method.equals("login") || method.equals("isLogged")) {
			logger.debug("Authentication not required");
			return;
		}
		if (!((AUIRPCServer) instance).isLogged(((HttpServletRequest) context).getSession(false))) {
			throw new AISException("Accesso non autorizzato");
		}
	}

	public void postInvoke(Object context, Object instance, AccessibleObject m,
			Object result) throws Exception {
		// Nothing to do
	}

}
