package fr.evolya.javatoolkit.events.basic;

public interface Listenable1<L, E, A> {

	boolean addListener(L listener);
	
	boolean removeListener(L listener);
	
	void notifyEvent(E event, @SuppressWarnings("unchecked") A... args);
	
}
