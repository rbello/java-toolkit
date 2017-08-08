package fr.evolya.javatoolkit.events.basic;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public abstract class Dispatcher1<L, E, A> implements Listenable1<L, E, A> {

	protected List<L> listeners;

	public Dispatcher1() {
		this.listeners = new ArrayList<L>();
	}

	@Override
	public boolean addListener(L listener) {
		return listeners.add(listener);
	}

	@Override
	public boolean removeListener(L listener) {
		return listeners.remove(listener);
	}
	
	@Override
	public void notifyEvent(E event, A... args) {
		System.out.println("Notify " + event);
		Method m = mapMethod(event);
		for (L listener : listeners) {
			try {
				m.invoke(listener, args);
			}
			catch (Throwable t) {
				System.out.println("Unable to dispatch to " + listener);
				t.printStackTrace();
			}
		}
	}

	protected abstract Method mapMethod(E methodName);

}
