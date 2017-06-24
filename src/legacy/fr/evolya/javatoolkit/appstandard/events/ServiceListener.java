package fr.evolya.javatoolkit.appstandard.events;

import fr.evolya.javatoolkit.appstandard.AppService;
import fr.evolya.javatoolkit.events.attr.EventListener;

public interface ServiceListener extends EventListener {

	public void onServiceStarted(AppService service);
	
	public void onServiceStopped(AppService service);

}
