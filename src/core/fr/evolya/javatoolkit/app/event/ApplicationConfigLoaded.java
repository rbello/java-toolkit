package fr.evolya.javatoolkit.app.event;

import fr.evolya.javatoolkit.app.App;
import fr.evolya.javatoolkit.app.config.AppConfiguration;

@FunctionalInterface
public interface ApplicationConfigLoaded {

	public void onConfigLoaded(App app, AppConfiguration config);
	
}
