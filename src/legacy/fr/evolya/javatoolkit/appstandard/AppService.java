package fr.evolya.javatoolkit.appstandard;

import fr.evolya.javatoolkit.app.AppActivity;
import fr.evolya.javatoolkit.appstandard.bridge.services.ILocalService;
import fr.evolya.javatoolkit.appstandard.events.ServiceListener;
import fr.evolya.javatoolkit.events.attr.EventSource;

@Deprecated
public interface AppService extends AppPlugin, AppActivity, ILocalService {

	public EventSource<? extends ServiceListener> getEventsService();
	
}
