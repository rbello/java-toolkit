package fr.evolya.javatoolkit.events.attr;

import fr.evolya.javatoolkit.code.Logs;
import fr.evolya.javatoolkit.events.alpha.IListener;

public final class CallbackMultipleEventIListener<L extends EventListener> implements EventCallback<L> {

	private IListener<String> _listener;

	public CallbackMultipleEventIListener(IListener<String> listener) {
		_listener = listener;
	}

	@Override
	public boolean notifyEvent(EventSource<L> source, String event, Object... args) {
		
		// Log
		if (EventSource.LOGGER.isLoggable(Logs.EVENT_NOTIFY)) {
			if (!toString().contains("DebugTreeViewController")) {
				EventSource.LOGGER.log(
						Logs.EVENT_NOTIFY,
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
		return "(IListener)" + _listener.getClass();
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
