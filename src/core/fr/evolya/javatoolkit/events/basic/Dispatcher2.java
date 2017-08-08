package fr.evolya.javatoolkit.events.basic;

import java.util.ArrayList;
import java.util.List;

public class Dispatcher2<E> implements Listenable2<E> {

	protected List<Listener2<E>> listeners;

	public Dispatcher2() {
		this.listeners = new ArrayList<Listener2<E>>();
	}
	
	@Override
	public boolean addListener(Listener2<E> listener) {
		return listeners.add(listener);
	}

	@Override
	public boolean removeListener(Listener2<E> listener) {
		return listeners.remove(listener);
	}

	@Override
	public void notifyEvent(E event) {
		for (Listener2<E> listener : listeners) {
			listener.notifyEvent(event);
		}
	}

}
