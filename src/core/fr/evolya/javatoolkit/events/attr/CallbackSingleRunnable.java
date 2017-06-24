package fr.evolya.javatoolkit.events.attr;

import fr.evolya.javatoolkit.code.IncaLogger;

public final class CallbackSingleRunnable<L extends EventListener> implements EventCallback<L> {

	private String _eventName;
	private Runnable _runnable;
	
	public CallbackSingleRunnable(String eventName, Runnable runnable) {
		_eventName = eventName;
		_runnable = runnable;
	}
	
	@Override
	public String getMethodName(String eventName) {
		return "run";
	}

	@Override
	public boolean notifyEvent(EventSource<L> source, String eventName, Object... args) {
		if (!_eventName.equals(eventName)) {
			return true;
		}
		// Log
		if (EventSource.LOGGER.isLoggable(IncaLogger.EVENT_NOTIFY)) {
			if (!toString().contains("DebugTreeViewController")) {
				EventSource.LOGGER.log(
						IncaLogger.EVENT_NOTIFY,
						"NOTIFY event " + eventName
						+ " --> TO " + _runnable);
			}
		}
		_runnable.run();
		return true;
	}

	public Runnable getListener() {
		return _runnable;
	}
	
	public String getEventName() {
		return _eventName;
	}

	@Override
	public String toString() {
		return "Runnable " + _runnable.getClass()
			+ " for event '" + _eventName + "' only";
	}

	@Override
	public Object getTargetObject() {
		return _runnable;
	}
	
}
