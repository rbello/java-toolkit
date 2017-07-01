package fr.evolya.javatoolkit.app.event;

import fr.evolya.javatoolkit.app.App;

@FunctionalInterface
public interface WindowCloseIntent {

	public void intentWindowClosing(App app, Object view, Object sourceEvent);

}
