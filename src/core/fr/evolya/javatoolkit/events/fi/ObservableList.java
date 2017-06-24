package fr.evolya.javatoolkit.events.fi;

import java.util.ArrayList;
import java.util.List;

import fr.evolya.javatoolkit.app.event.Model;
import fr.evolya.javatoolkit.app.event.ModelEvent;
import fr.evolya.javatoolkit.app.event.ModelEvent.ModelItemAdded;

public class ObservableList<T> implements Model {

	private Observable dispatcher;
	
	private List<T> list = new ArrayList<>();

	public ObservableList(Observable dispatcher) {
		this.dispatcher = dispatcher;
	}

	@Override
	public <E extends ModelEvent> Listener<E> on(Class<E> eventType) {
		return dispatcher.when(eventType).onlyOn(arg -> arg == this);
	}

	public void add(T item) {
		if (item == null) throw new NullPointerException();
		list.add(item);
		dispatcher.notify(ModelItemAdded.class, this, item, list.size()-1);
	}

}
