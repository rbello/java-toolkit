package fr.evolya.javatoolkit.events.attr;

public final class CallbackEventSource<L extends EventListener> implements EventCallback<L> {

	private EventSource<? extends EventListener> _target;

	public CallbackEventSource(EventSource<? extends EventListener> target) {
		_target = target;
	}

	@Override
	public boolean notifyEvent(EventSource<L> source, String eventName, Object... args) {
		
		// Log
		/*if (EventSource.LOGGER.isLoggable(IncaLogger.EVENT_NOTIFY)) {
			EventSource.LOGGER.log(
					IncaLogger.EVENT_NOTIFY,
					"REDIRECT event " + eventName
					+ " --> TO " + toString());
		}*/
		
		// Redirection vers la cible
//		synchronized (_target) {
			return _target.trigger(eventName, args);
//		}
		
	}
	
	@Override
	public String toString() {
		return _target + "";
	}

	@Override
	public String getMethodName(String eventName) {
		return "trigger";
	}

	@Override
	public Object getTargetObject() {
		// On n'a pas l'info de qui a réellement envoyé l'event
		return null;
	}

}
