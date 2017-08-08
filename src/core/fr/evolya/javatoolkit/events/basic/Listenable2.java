package fr.evolya.javatoolkit.events.basic;

public interface Listenable2<E> {

	boolean addListener(Listener2<E> listener);
	
	boolean removeListener(Listener2<E> listener);
	
	void notifyEvent(E event);
	
}
