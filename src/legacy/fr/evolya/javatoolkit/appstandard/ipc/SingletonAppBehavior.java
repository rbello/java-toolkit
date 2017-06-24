package fr.evolya.javatoolkit.appstandard.ipc;

import fr.evolya.javatoolkit.appstandard.AbstractPlugin;
import fr.evolya.javatoolkit.appstandard.App;
import fr.evolya.javatoolkit.appstandard.bridge.services.ELocalServiceType;
import fr.evolya.javatoolkit.appstandard.events.ServiceListener;
import fr.evolya.javatoolkit.events.attr.EventSource;

public class SingletonAppBehavior extends AbstractPlugin
	implements ILocalIPC {

	private App _app;
	
	protected EventSource<LocalIPCListener> _eventsService = 
			new EventSource<LocalIPCListener>(LocalIPCListener.class, this);

	@Override
	public EventSource<? extends ServiceListener> getEventsService() {
		return _eventsService;
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
		System.err.println("Run");
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void interrupt() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isStarted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ELocalServiceType getServiceType() {
		return ELocalServiceType.LOCAL_IPC;
	}

	@Override
	public boolean isPublished() {
		return false;
	}

	@Override
	protected void connected(App app) {
		this._app = app;
	}

	public static boolean exists(fr.evolya.javatoolkit.app.App app) {
		// TODO Auto-generated method stub
		return false;
	}

	public static void route(fr.evolya.javatoolkit.app.App app, String[] args) {
		// TODO Auto-generated method stub
		
	}

	public static boolean routeIfExists(fr.evolya.javatoolkit.app.App app, String[] args) {
		// TODO Auto-generated method stub
		return false;
	}
	
}
