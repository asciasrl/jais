package it.ascia.bentel;

import it.ascia.ais.AISException;

public class BentelKyoUnitPanelFactory {

	public static BentelKyoUnit getPanel(String type, String version) {
		if (type.equals("KYO8") || type.equals("KYO8G")) {
 			return new BentelKyo8(version);
		} else {
			throw(new AISException("Unsupported panel type:"+type));
		}
	}

}
