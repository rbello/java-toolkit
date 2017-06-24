package fr.evolya.javatoolkit.appstandard.events;

import fr.evolya.javatoolkit.appstandard.App;
import fr.evolya.javatoolkit.appstandard.AppService;
import fr.evolya.javatoolkit.appstandard.states.ApplicationState;
import fr.evolya.javatoolkit.events.attr.EventListener;

public interface AppListener extends EventListener {
	
	public boolean beforeApplicationStarted(App app);
	
	public void onApplicationStarted(App app);
	
	public void afterApplicationStarted(App app);
	
	public boolean beforeApplicationStopped(App app);
	
	public void onApplicationStateChanged(App app, ApplicationState state);

	public void onServiceStarted(App app, AppService service);
	
	public void onServiceStopped(App app, AppService service);
	
}
