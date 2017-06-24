package fr.evolya.javatoolkit.soho.service;

import fr.evolya.javatoolkit.appstandard.AbstractThreadedService;
import fr.evolya.javatoolkit.appstandard.App;
import fr.evolya.javatoolkit.appstandard.bridge.services.ELocalServiceType;
import fr.evolya.javatoolkit.events.attr.EventSource;
import fr.evolya.javatoolkit.soho.connector.SohoConnector;
import fr.evolya.javatoolkit.soho.connector.SohoConnectorListener;
import fr.evolya.javatoolkit.soho.session.ISohoSession;

public class SohoService extends AbstractThreadedService {

	public SohoService() {
		// On surcharge l'interface du listener de services, et on demande que le
		// service démarre automatiquement au lancement de l'application.
		super(SohoServiceListener.class, false, true);
	}

	/**
	 * Le connecteur Soho.
	 */
	private SohoConnector _connector;

	/**
	 * La session en cours.
	 */
	private ISohoSession _session;

	@Override
	public ELocalServiceType getServiceType() {
		return ELocalServiceType.REMOTE_CUSTOM_SERVICE;
	}

	@Override
	protected void onStart() throws Exception {
	}

	@Override
	protected void onStop() throws Exception {
		if (_connector != null) {
			getEventsConnector().setEnabled(false);
			_connector.dispose();
			_connector = null;
		}
	}

	@Override
	protected void onConnected(App app) {
		app.getEventsApp().bind("onConfigurationRestored", this, "onConfigurationRestored");
	}
	
	public EventSource<SohoConnectorListener> getEventsConnector() {
		return _connector != null ? _connector.getEventsConnector() : null;
	}
	
	public void setConnector(SohoConnector connector) {
		// On ne fait rien
		if (connector == _connector) {
			return;
		}
		// Unbound de l'ancien
		if (_connector != null) {
			getEventsService().trigger("onConnectorUnbound", connector);
		}
		// Bound du nouveau
		if (connector != null) {
			getEventsService().trigger("onConnectorBound", connector);
		}
		// On remplace le connecteur
		_connector = connector;
	}

	public SohoConnector getConnector() {
		return _connector;
	}
	

	public ISohoSession getSession() {
		return _session;
	}

	public void setSession(ISohoSession session) {
		// Event
		if (session != _session) {
			getEventsService().trigger("onSessionChanged", session, _session);
		}
		// Sauvegarde
		_session = session;
	}

}
