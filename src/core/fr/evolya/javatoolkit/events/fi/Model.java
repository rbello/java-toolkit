package fr.evolya.javatoolkit.events.fi;

public interface Model {

	public <T extends ModelEvent> Listener<T> on(Class<T> eventType);

}
