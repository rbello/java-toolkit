package fr.evolya.javatoolkit.app.event;

import fr.evolya.javatoolkit.app.App;

@FunctionalInterface
public interface GuiIsReady<T> {

	public void onGuiIsReady(T gui, App app);
	
}
