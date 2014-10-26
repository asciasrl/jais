package it.ascia.duemmegi;

import it.ascia.ais.AISException;
import it.ascia.ais.Device;
import it.ascia.duemmegi.domino.device.DF4I;
import it.ascia.duemmegi.domino.device.DF4IV;
import it.ascia.duemmegi.domino.device.DF4R;
import it.ascia.duemmegi.domino.device.DF4RP;
import it.ascia.duemmegi.domino.device.DF8IL;
import it.ascia.duemmegi.domino.device.DFCT;
import it.ascia.duemmegi.domino.device.DFDI;
import it.ascia.duemmegi.domino.device.DFDM;
import it.ascia.duemmegi.domino.device.DFDV;
import it.ascia.duemmegi.domino.device.DFGSM2;
import it.ascia.duemmegi.domino.device.DFIR;
import it.ascia.duemmegi.domino.device.DFTA;
import it.ascia.duemmegi.domino.device.DFTP;
import it.ascia.duemmegi.domino.device.DFTPI;

public abstract class DominoDevice extends Device {

	protected int intAddress;
	
	public DominoDevice(String address) throws AISException {
		super(address);
		intAddress = DominoDevice.getIntAddress(getDeviceAddress());
	}
	
	public static int getIntAddress(String address) {
		return new Integer(address.substring(1)).intValue();
	}

	public static DominoDevice CreateDevice(String model, String address,
			DFCPConnector dfcpConnector) {
		if (model.equals("DF4I")) {
			return new DF4I(address, dfcpConnector);
		} else if (model.equals("DF4I/V")) {
			return new DF4IV(address, dfcpConnector);
		} else if (model.equals("DF8IL")) {
			return new DF8IL(address, dfcpConnector);
		} else if (model.equals("DFIR")) {
			return new DFIR(address);
		} else if (model.equals("DF4R")) {
			return new DF4R(address);
		} else if (model.equals("DF4RP")) {
			return new DF4RP(address);
		} else if (model.equals("DFTA")) {
			return new DFTA(address);
		} else if (model.equals("DFTP")) {
			return new DFTP(address);
		} else if (model.equals("DFTP/I")) {
			return new DFTPI(address,dfcpConnector);
		} else if (model.equals("DFCT")) {
			return new DFCT(address,dfcpConnector);
		} else if (model.equals("DFGSM2")) {
			return new DFGSM2(address);
		} else if (model.equals("DFDM")) {
			return new DFDM(address);
		} else if (model.equals("DFDI")) {
			return new DFDI(address);
		} else if (model.equals("DFDV")) {
			return new DFDV(address);
		} else if (model.equals("DFTA")) {
			return new DFTA(address);
		} else {
			return null;
		}
	}


	//public abstract void messageReceived(DXPMessage m);

	//public abstract void messageSent(DXPMessage m);

}
