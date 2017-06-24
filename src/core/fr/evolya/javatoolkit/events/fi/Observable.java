package fr.evolya.javatoolkit.events.fi;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import fr.evolya.javatoolkit.code.IncaLogger;
import fr.evolya.javatoolkit.code.Instance;
import fr.evolya.javatoolkit.code.Instance.FuturInstance;

public abstract class Observable {

	public static final Logger LOGGER = IncaLogger.getLogger("Events (v2)");
	
	/**
	 * Liste des listeners.
	 */
	private final List<Listener<?>> listeners = new ArrayList<>();
	
	/**
	 * Liste des événement qui doivent être répétés si un listener
	 * s'inscrit après que l'évent soit levé.
	 * Le boolean indique si l'événement est déjà apparu et doit
	 * donc être répété.
	 */
	private final Map<Class<?>, Object[]> repeatedEvents = new HashMap<>();

	/**
	 * Fabrique un listener.
	 * 
	 * @param eventType Le type d'event que l'on va observer.
	 * @return Le listener.
	 */
	public final <T> Listener<T> when(Class<T> eventType) {
		return new Listener<T>(Observable.this, eventType);
	}
	
	final void addListener(Listener<?> listener) {
		if (listener == null)
			throw new NullPointerException();
		
		if (listener.getMethod() == null)
			throw new NullPointerException("Unable to add " + listener + ": no method");
		
		// TODO Synchronized
		listeners.add(listener);
		
		// L'événement doit être répété
		if (repeatedEvents.containsKey(listener.getEventType())) {
			
			Object[] args = repeatedEvents.get(listener.getEventType());
			if (args != null) {
				// Log
				if (LOGGER.isLoggable(IncaLogger.DEBUG_FINE)) {
					String argsm = "";
					for (int i = 0; i < args.length; ++i)
						argsm += (i > 0 ? ", " : "") + (args[i] == null ? "null" : args[i].toString());
					LOGGER.log(IncaLogger.DEBUG_FINE, "  Repeat event " + listener.getEventType().getSimpleName() + " [" + argsm + "]"
							+ " to " + listener.toString());
				}
				// Notify listener
				execute(listener.getEventType(), listener.getMethod(), listener, args);
			}
		}
		
	}
	
	public final void notify(Class<?> eventType, Object... args) {
		
		// Debug
		final boolean debug = LOGGER.isLoggable(IncaLogger.DEBUG_FINE);
		if (debug) {
			String argsm = "";
			for (int i = 0; i < args.length; ++i)
				argsm += (i > 0 ? ", " : "") + (args[i] == null ? "null" : args[i].toString());
			LOGGER.log(IncaLogger.DEBUG_FINE, "  Notify event " + eventType.getSimpleName() + " [" + argsm + "]");
		}
		
		// Mark as repeated
		if (repeatedEvents.containsKey(eventType)) {
			repeatedEvents.put(eventType, args);
		}
		
		// Broadcast
		new ArrayList<>(listeners).stream()
			.filter((item) -> {
				// On vérifie que ce soit bien ce type d'event qui soit demandé, et
				// que le listener accepte les données.
				return item.getEventType() == eventType && item.accept(args);
			})
			.forEach((item) -> {
				// Debug
				if (debug) {
					LOGGER.log(IncaLogger.DEBUG_FINE, "    `-> To handler " + item);
				}
				// On execute le listener
				item.notify(args);
			});
	}
	
	protected final Object execute(Class<?> clazz, Method m, Listener<?> item, Object[] args) {
		if (m == null) {
			throw new NullPointerException("Method is null");
		}
		if (m.getParameters().length < args.length) {
			Object[] copy = args;
			args = new Object[m.getParameters().length];
			for (int i = 0; i < args.length; ++i) {
				args[i] = copy[i];
			}
		}
		try {
			Object target = item.getTarget();
			if (target == null) throw new NullPointerException("Invoke target is null");
			m.setAccessible(true);
			return m.invoke(target, args);
		}
		catch (IllegalArgumentException e) {
			//Method m2 = clazz.getMethods()[0];
			LOGGER.log(IncaLogger.ERROR, "Dispatch error");
			LOGGER.log(IncaLogger.INFO, "  Listener: " + item);
			LOGGER.log(IncaLogger.INFO, "  Event " + clazz.getSimpleName() + " : " + e.getMessage());
			if (e.getMessage().equals("object is not an instance of declaring class")) {
				LOGGER.log(IncaLogger.INFO, "  Object class: " + item.getTarget().getClass().getName());
				LOGGER.log(IncaLogger.INFO, "  Declaring class: " + m.getDeclaringClass().getName());
			}
			LOGGER.log(IncaLogger.INFO, "  Target method is " + item.getTarget().getClass() + "::" + m.getName() + "(" + Arrays.stream(m.getParameterTypes()).map(Class::getName).collect(Collectors.joining(", ")) + ")");
//			LOGGER.log(IncaLogger.INFO, "  Expected arguments are: (" + Arrays.stream(m2.getParameterTypes()).map(Class::getName).collect(Collectors.joining(", ")) + ")");
			LOGGER.log(IncaLogger.INFO, "  Event arguments are: (" + Arrays.stream(args).map((o) -> o.getClass().getName()).collect(Collectors.joining(", ")) + ")");
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return Void.TYPE;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public final void addListener(Instance instance) {
		
		// L'instance donnée est future mais ne possède même pas de classe
		if (instance.getInstanceClass() == null)
			throw new NullPointerException("Cannot add pure futur objet as listener");
		
		// Toutes les méthodes de l'instance
		Arrays.stream(instance.getInstanceClass().getMethods())
			// Uniquement celles avec l'annotation d'injection
			.filter(method -> method.isAnnotationPresent(BindOnEvent.class))
			// Fetch
			.forEach((method) -> {
				BindOnEvent a = method.getAnnotation(BindOnEvent.class);
				Class<?> eventType = a.value();
				String[] tmp = eventType.toString().split("\\.");
				String eventName = tmp[tmp.length - 1];
				
				// Debug
				if (LOGGER.isLoggable(IncaLogger.DEBUG)) {
					LOGGER.log(IncaLogger.DEBUG, String.format(
							"  `-> Bind event %s on method %s::%s",
							eventName,
							instance.getInstanceClass().getSimpleName(),
							method.getName()
							));
				}
				// On rajoute le listener
				Listener l = null;
				if (instance.isFutur()) {
					boolean gui = method.isAnnotationPresent(GuiTask.class);
					l = new FuturListener(this, eventType, (FuturInstance) instance, method, gui);
				}
				else {
					l = new Listener(this, eventType, instance, method);
				}
				if (method.isAnnotationPresent(EventArgClassFilter.class)) {
					Class<?> filter = method.getAnnotation(EventArgClassFilter.class).value();
					l = l.onlyOn(arg -> {
						return filter.isInstance(arg);
					});
				}
				addListener(l);
			});
	}
	
	public final void repeatForNewListeners(Class<?>... events) {
		for (Class<?> event : events) {
			repeatedEvents.put(event, null);
		}
	}
	
	protected List<Listener<?>> getListeners() {
		return listeners;
	}
	
	public abstract boolean isGuiDispatchThread();
	
	protected abstract void invokeAndWaitOnGuiDispatchThread(Runnable task) throws InterruptedException;
	
	protected abstract <T> T invokeAndWaitOnGuiDispatchThread(Callable<T> task);
	
	protected abstract void invokeLaterOnGuiDispatchThread(Runnable task);
	
}
