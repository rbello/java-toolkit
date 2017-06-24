package fr.evolya.javatoolkit.app.event;

import fr.evolya.javatoolkit.events.fi.Listener;

public interface Model {

	public <T extends ModelEvent> Listener<T> on(Class<T> eventType);

}
