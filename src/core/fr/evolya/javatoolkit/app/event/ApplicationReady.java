package fr.evolya.javatoolkit.app.event;

import fr.evolya.javatoolkit.app.App;

@FunctionalInterface
public interface ApplicationReady {

	public void onApplicationReady(App app);
	
}
