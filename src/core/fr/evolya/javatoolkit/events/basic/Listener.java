package fr.evolya.javatoolkit.events.basic;

public interface Listener<E extends Event> {

	public void notifyEvent(E event);

}
