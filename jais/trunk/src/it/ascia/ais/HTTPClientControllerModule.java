package it.ascia.ais;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import org.apache.commons.configuration.HierarchicalConfiguration;

public class HTTPClientControllerModule extends ControllerModule {

	public void start() {
		HierarchicalConfiguration config = getConfiguration();
		Boolean isSsl = config.getBoolean("ssl", false);
		String protocol = config.getString("protocol", isSsl ? "https" : "http");
		String host = config.getString("host");
		int port = config.getInt("port", isSsl ? 443 : 80);
		String file = config.getString("file");
		try {
			URL myurl;
			myurl = new URL(protocol, host, port, file);
			HttpsURLConnection con;
			con = (HttpsURLConnection) myurl.openConnection();
			InputStream ins = con.getInputStream();
			InputStreamReader isr = new InputStreamReader(ins);
			BufferedReader in = new BufferedReader(isr);
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				System.out.println(inputLine);
			}
			in.close();
		} catch (MalformedURLException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}

		super.start();
	}

	public void stop() {
		super.stop();
		logger.debug("Arresto server HTTP ...");
		try {
			// client.stop();
			logger.debug("Arrestato server HTTP.");
		} catch (Exception e) {
			logger.error("Errore durante l'arresto del server: ", e);
		}
	}

}
