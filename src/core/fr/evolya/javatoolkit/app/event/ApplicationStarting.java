package fr.evolya.javatoolkit.app.event;

import fr.evolya.javatoolkit.app.App;

@FunctionalInterface
public interface ApplicationStarting {

	public void onApplicationStarting(App app);
	
}
