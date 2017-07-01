package fr.evolya.javatoolkit.app.event;

import fr.evolya.javatoolkit.app.App;

@FunctionalInterface
public interface ApplicationStopped {

	public void onApplicationStopped(App app);
	
}
