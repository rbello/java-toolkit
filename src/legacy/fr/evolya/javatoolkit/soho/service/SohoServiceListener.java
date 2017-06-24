package fr.evolya.javatoolkit.soho.service;

import fr.evolya.javatoolkit.appstandard.events.ServiceListener;
import fr.evolya.javatoolkit.soho.connector.SohoConnector;
import fr.evolya.javatoolkit.soho.session.ISohoSession;

public interface SohoServiceListener extends ServiceListener {

	public void onConnectorBound(SohoConnector connector);
	
	public void onConnectorUnbound(SohoConnector connector);
	
	public void onSessionChanged(ISohoSession newSession, ISohoSession oldSession);
	
}
