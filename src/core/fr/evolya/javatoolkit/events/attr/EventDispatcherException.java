package fr.evolya.javatoolkit.events.attr;

public class EventDispatcherException extends RuntimeException {

	public EventDispatcherException(EventSource<?> src, EventCallback<?> listener, String eventName, Throwable ex) {
		super(ex.getClass().getSimpleName() + " during event" +
				"propagation '" + eventName + "' from: " + listener, ex);
	}

	private static final long serialVersionUID = 1L;

}
