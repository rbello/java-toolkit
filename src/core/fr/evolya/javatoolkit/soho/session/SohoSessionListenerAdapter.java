package fr.evolya.javatoolkit.soho.session;

import fr.evolya.javatoolkit.net.http.HttpException;

public abstract class SohoSessionListenerAdapter implements SohoSessionListener {

	@Override
	public void afterSessionAuthentication(ISohoSession session, String userID, boolean success) {
	}

	@Override
	public void afterSessionClosed(ISohoSession session) {
	}

	@Override
	public void afterSessionLogout(ISohoSession session, String userID, boolean remoteLogoutSuccess) {
	}

	@Override
	public boolean beforeSessionAuthentication(ISohoSession session, String userID) {
		return true;
	}

	@Override
	public void beforeSessionClosed(ISohoSession session) {
	}

	@Override
	public boolean beforeSessionLogout(ISohoSession session, String userID) {
		return true;
	}

	@Override
	public void onSessionAuthenticationFailure(ISohoSession session, String userID, HttpException ex) {
	}

	@Override
	public void onSessionAuthenticationSuccess(ISohoSession session, String userID) {
	}
	
}
