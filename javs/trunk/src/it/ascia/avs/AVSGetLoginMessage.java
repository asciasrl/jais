package it.ascia.avs;

public class AVSGetLoginMessage extends AVSMessage {

	public AVSGetLoginMessage() {
		super(GET_ERR_LOGIN);
	}

	public AVSGetLoginMessage(AVSMessage m) {
		super(m);
	}

	public String getUser() {
		if (isLoginOk()) {
			return null;
		} else {
			return String.valueOf(data[0] << 8 + data[1] + 1);
		}
	}

	public String toString() {
		return super.toString() + " User="+getUser();
	}

	public boolean isLoginOk() {
		return getData()[2] == INFO_LOGIN_OK;
	}

	public boolean isLogoutOk() {
		return getData()[2] == INFO_LOGOUT_OK;
	}

	public boolean isLogoutTimeout() {
		return getData()[2] == INFO_LOGOUT_TO;
	}

	public boolean isLogoutReset() {
		return getData()[2] == INFO_LOGOUT_RST;
	}
	
	public boolean isLogout() {
		return isLogoutOk() || isLogoutTimeout() || isLogoutReset();
	}
	

}
