package fr.evolya.javatoolkit.soho.service;

import fr.evolya.javatoolkit.appstandard.AppService;
import fr.evolya.javatoolkit.soho.connector.SohoConnector;
import fr.evolya.javatoolkit.soho.session.ISohoSession;

public abstract class SohoServiceAdapter implements SohoServiceListener {

	@Override
	public void onServiceStarted(AppService service) {
	}

	@Override
	public void onServiceStopped(AppService service) {
	}

	@Override
	public void onConnectorBound(SohoConnector connector) {
	}

	@Override
	public void onConnectorUnbound(SohoConnector connector) {
	}
	
	@Override
	public void onSessionChanged(ISohoSession newSession, ISohoSession oldSession) {
	}

}
