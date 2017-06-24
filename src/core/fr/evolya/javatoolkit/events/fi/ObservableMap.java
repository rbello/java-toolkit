package fr.evolya.javatoolkit.events.fi;

import java.util.HashMap;
import java.util.Map;

import fr.evolya.javatoolkit.events.fi.ModelEvent.ModelItemAdded;
import fr.evolya.javatoolkit.events.fi.ModelEvent.ModelItemModified;

public class ObservableMap<K, V> implements Model {

	private Observable dispatcher;
	
	private Map<K, V> list = new HashMap<>();

	public ObservableMap(Observable dispatcher) {
		this.dispatcher = dispatcher;
	}

	@Override
	public <E extends ModelEvent> Listener<E> on(Class<E> eventType) {
		return dispatcher.when(eventType).onlyOn(arg -> arg == this);
	}

	public void put(K key, V value) {
		if (key == null) throw new NullPointerException();
		boolean add = !list.containsKey(key);
		list.put(key, value);
		dispatcher.notify(add ? ModelItemAdded.class : ModelItemModified.class, this, value, key);
	}

}
