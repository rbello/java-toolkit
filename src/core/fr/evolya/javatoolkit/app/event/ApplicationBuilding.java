package fr.evolya.javatoolkit.app.event;

import fr.evolya.javatoolkit.app.App;

@FunctionalInterface
public interface ApplicationBuilding {

	public void build(App app);
	
}
