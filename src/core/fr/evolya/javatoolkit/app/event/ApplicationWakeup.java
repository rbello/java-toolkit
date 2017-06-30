package fr.evolya.javatoolkit.app.event;

import fr.evolya.javatoolkit.app.App;

@FunctionalInterface
public interface ApplicationWakeup {
	
	public void onApplicationWakeup(App app);

}
