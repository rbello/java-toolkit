package fr.evolya.javatoolkit.events.attr;

import java.lang.reflect.Method;

import fr.evolya.javatoolkit.code.IncaLogger;

public final class CallbackEventListener<L extends EventListener> implements EventCallback<L> {

	private L _listener;
	private String _interfaceName;

	public CallbackEventListener(L listener, String interfaceName) {
		_listener = listener;
		_interfaceName = interfaceName;
	}
	
	@Override
	public boolean notifyEvent(EventSource<L> source, String event, Object... args)
		throws Exception {
		
		// TODO On a probléme ici, on essaye toute les méthodes qui ont le même nom
		// dont si l'interface a plusieurs méthodes du même nom, ça balance plein d'exceptions
//		synchronized (_listener) {
		
			// Fetch methods
			for (Method m : _listener.getClass().getMethods()) {
				
				// Name mismatch
				if (!m.getName().equals(event)) {
					continue;
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
				
				try {
					
					// Pour permetre l'invocation d'une méthode dans une classe anonyme
					m.setAccessible(true);
					
					// Si la méthode renvoie une boolean, on s'en sert pour le retour
					if (m.getReturnType().toString().equals("boolean") || m.getReturnType() == Boolean.class) {
						return (Boolean) m.invoke(_listener, args);
					}
					// Sinon on fait juste l'invocation, et on renvera TRUE à la fin
					else {
						m.invoke(_listener, args);
						return true;
					}
					
				}
				
				// Cette exception va se produire si plusieurs méthodes ont le même nom,
				// elles vont toutes être appelée les unes après les autres jusqu'à la bonne
				// si elle existe.
				catch (IllegalArgumentException e) {
					if (EventSource.LOGGER.isLoggable(IncaLogger.WARNING)) {
						StringBuilder sb = new StringBuilder();
						sb.append(e.getClass().getSimpleName());
						sb.append(" - ");
						sb.append(e.getMessage());
						sb.append("\nEvent: ");
						sb.append(event);
						sb.append("(");
						int i = 0;
						for (Object arg : args) {
							if (i++ > 0) sb.append(", ");
							sb.append(arg == null ? "NULL" : arg.getClass().getSimpleName());
						}
						sb.append(")");
						sb.append("\nSource: ");
						sb.append(source.toString());
						sb.append("\nCallback: ");
						sb.append(this);
						sb.append("\n          ");
						sb.append(getMethodName(event));
						sb.append("(");
						i = 0;
						for (Class<?> type : m.getParameterTypes()) {
							if (i++ > 0) sb.append(", ");
							sb.append(args == null ? "NULL" : type.getSimpleName());
						}
						sb.append(")");
						EventSource.LOGGER.log(IncaLogger.WARNING, sb.toString());
					}
				}
				
			}
			
			// Si on arrive ici, c'est qu'on n'a pas trouvé de méthode
			// qui match, donc on ne fait rien.
			return true;
		
//		}
		
	}

	@Override
	public String toString() {
		return "Listener " + _interfaceName + " : " + _listener;
	}

	public L getListener() {
		return _listener;
	}

	@Override
	public String getMethodName(String eventName) {
		return eventName;
	}

	@Override
	public Object getTargetObject() {
		return _listener;
	}
	
}
