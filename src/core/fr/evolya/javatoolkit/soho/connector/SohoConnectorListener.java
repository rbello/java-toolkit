package fr.evolya.javatoolkit.soho.connector;

import fr.evolya.javatoolkit.events.attr.EventListener;
import fr.evolya.javatoolkit.soho.session.ISohoSession;

public interface SohoConnectorListener extends EventListener {

	public void onSessionCreated(ISohoSession session, SohoConnector connector);
	
	public boolean beforeConnectorClosed(SohoConnector connector);
	
	public void afterConnectorClosed(SohoConnector connector);
	
}
