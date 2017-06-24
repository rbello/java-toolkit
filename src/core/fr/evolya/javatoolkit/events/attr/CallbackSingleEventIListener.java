package fr.evolya.javatoolkit.events.attr;

import fr.evolya.javatoolkit.code.IncaLogger;
import fr.evolya.javatoolkit.events.alpha.IListener;

public final class CallbackSingleEventIListener<L extends EventListener> implements EventCallback<L> {

	private String _eventName;
	private IListener<String> _listener;

	public CallbackSingleEventIListener(String eventName, IListener<String> listener) {
		_eventName = eventName;
		_listener = listener;
	}

	@Override
	public boolean notifyEvent(EventSource<L> source, String event, Object... args) {
		
		// Filtrage
		if (!_eventName.equals(event)) {
			return true;
		}
		
		// Log
		if (EventSource.LOGGER.isLoggable(IncaLogger.EVENT_NOTIFY)) {
			if (!toString().contains("DebugTreeViewController")) {
				EventSource.LOGGER.log(
						IncaLogger.EVENT_NOTIFY,
						"NOTIFY event " + event
						+ " --> TO " + toString());
			}
		}
		
//		synchronized (_listener) {
			// Propagation au listener
			return _listener.notifyEvent(event, args);
//		}
		
	}
	
	@Override
	public String toString() {
		return "IListener '" + _listener.getClass() + "' for event '"+_eventName+"' only";
	}

	public IListener<String> getListener() {
		return _listener;
	}

	@Override
	public String getMethodName(String eventName) {
		return "notifyEvent";
	}

	@Override
	public Object getTargetObject() {
		return _listener;
	}

}
