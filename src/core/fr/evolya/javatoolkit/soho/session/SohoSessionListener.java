package fr.evolya.javatoolkit.soho.session;

import fr.evolya.javatoolkit.events.attr.EventListener;
import fr.evolya.javatoolkit.net.http.HttpException;

public interface SohoSessionListener extends EventListener {

	public boolean beforeSessionLogout(ISohoSession session, String userID);
	
	public void afterSessionLogout(ISohoSession session, String userID, boolean remoteLogoutSuccess);
	
	public void beforeSessionClosed(ISohoSession session);
	
	public void afterSessionClosed(ISohoSession session);
	
	public boolean beforeSessionAuthentication(ISohoSession session, String userID);
	
	public void onSessionAuthenticationFailure(ISohoSession session, String userID, HttpException ex);
	
	public void onSessionAuthenticationSuccess(ISohoSession session, String userID);
	
	public void afterSessionAuthentication(ISohoSession session, String userID, boolean success);
	
}
