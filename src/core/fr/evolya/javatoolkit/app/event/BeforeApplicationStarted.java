package fr.evolya.javatoolkit.app.event;

import fr.evolya.javatoolkit.app.App;

@FunctionalInterface
public interface BeforeApplicationStarted {

	public void beforeApplicationStarted(App app);

}
