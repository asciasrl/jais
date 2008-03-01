/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.ais;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Riceve le richieste di AUI e le manda al controller.
 * 
 * <p>Le richieste vengono onorate solo se presentano i parametri "name" e "value". Altrimenti
 * vengono ignorate.</p>
 * 
 * @author arrigo
 */
public class AUIRequestServlet extends HttpServlet {
	/**
	 * Il nostro controller.
	 */
	private Controller controller;
	/**
	 * Costruttore.
	 * @param controller il controller del bus.
	 */
	public AUIRequestServlet(Controller controller) {
		this.controller = controller;
	}
	
	/**
	 * Gestisce la richiesta.
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
	throws IOException, ServletException {
		PrintWriter out = response.getWriter();
		// Ritorniamo sempre & solo testo
		response.setContentType("text/plain");
		// Va sempre tutto bene (gli errori li scriviamo dentro)
		response.setStatus(HttpServletResponse.SC_OK);
		if (request.getMethod().toUpperCase().equals("GET")) {
			String name = request.getParameter("name");
			String uri = request.getRequestURI();
			// Per sicurezza: non vogliamo null che girano
			if (name == null) name = "";
			if (uri == null) uri = "";
			// Togliamo le directory fino all'ultimo slash
			int index = uri.lastIndexOf("/");
			if (index > 0) {
				String action = uri.substring(index + 1);
				String value = request.getParameter("value");
				String pin = request.getParameter("pin");
				if (value == null) {
					value = "";
				}
				if (pin == null) {
					pin = "";
				}
				out.println(controller.receiveRequest(action, name, value, 
						pin));
			} else {
				out.println("ERROR: malformed action");
			}
		} else {
			out.println("ERROR: needs GET method");
		}
	}

}
