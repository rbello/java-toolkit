package fr.evolya.javatoolkit.app.event;

import fr.evolya.javatoolkit.app.App;

@FunctionalInterface
public interface ApplicationStopping {

	public void onApplicationStopping(App app);
	
}
