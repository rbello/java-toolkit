package fr.evolya.javatoolkit.events.fi;

import fr.evolya.javatoolkit.app.cdi.Instance;

public interface IObservable {

	/**
	 * Add a listener on the given event type.
	 */
	<T> Listener<T> when(Class<T> eventType);
	
	/**
	 * Notify an event to all the listeners.
	 */
	void notify(Class<?> eventType, Object... args);
	
	/**
	 * Notify an event to a given listener.
	 */
	void notify(Instance<?> target, Class<?> eventType, Object... args);
	
	/**
	 * Remove all bound listeners.
	 */
	void removeAllListeners();
	
}
