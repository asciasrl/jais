package it.ascia.modbus;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;

import it.ascia.ais.ControllerModule;

public class ModbusControllerModule extends ControllerModule {

	public void start() {
		super.start();

		List<HierarchicalConfiguration> connectors = getConfiguration().configurationsAt("connectors.connector");
		for (Iterator<HierarchicalConfiguration> c = connectors.iterator(); c.hasNext();) {
			HierarchicalConfiguration sub = c.next();
			if (sub.getBoolean("[@disabled]", false)) {
				logger.debug("Connector disabled: " + sub.getString("name"));
				continue;
			}
						
			ModbusConnector conn = new ModbusConnector(sub.getInt("master"),sub.getString("name"), sub.getString("portname"), sub.getString("encoding"));

			controller.addConnector(conn);

			List<HierarchicalConfiguration> slaves = sub.configurationsAt("slave");
			if (slaves == null || slaves.size() == 0) {
				logger.fatal("Nessun dispositivo slave");
			} else {
				for (Iterator<HierarchicalConfiguration> i = slaves.iterator(); i.hasNext();) {
					try {
						conn.addSlave(i.next());
					} catch (NumberFormatException e) {
						logger.error("Indirizzo dispositivo slave non corretto: " + e.getMessage());
					}
				}
			}

		}
	}

}
