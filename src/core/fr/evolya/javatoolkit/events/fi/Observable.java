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
import java.util.stream.Stream;

import fr.evolya.javatoolkit.app.cdi.Instance;
import fr.evolya.javatoolkit.app.cdi.Instance.FuturInstance;
import fr.evolya.javatoolkit.code.Logs;
import fr.evolya.javatoolkit.code.annotations.DesignPattern;
import fr.evolya.javatoolkit.code.annotations.GuiTask;
import fr.evolya.javatoolkit.code.annotations.Pattern;
import fr.evolya.javatoolkit.code.annotations.ToOverride;
import fr.evolya.javatoolkit.code.utils.ReflectionUtils;

@DesignPattern(type = Pattern.Observer)
public /*abstract*/ class Observable implements IObservable {

	public static final Logger LOGGER = Logs.getLogger("Events (v2)");
	
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
	@Override
	public final <T> Listener<T> when(Class<T> eventType) {
		return new Listener<T>(Observable.this, eventType);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public final void addListener(Instance instance) {
		
		// L'instance donnée est future mais ne possède même pas de classe
		if (instance.getInstanceClass() == null)
			throw new NullPointerException("Cannot add pure futur objet as listener");
		
		// Toutes les méthodes de l'instance
		ReflectionUtils.forEachMethodsHaving(instance.getInstanceClass(), BindOnEvent.class, (method) -> {
			BindOnEvent a = method.getAnnotation(BindOnEvent.class);
			Class<?> eventType = a.value();
			String eventName = eventType.getSimpleName();
			// Debug
			if (LOGGER.isLoggable(Logs.DEBUG)) {
				LOGGER.log(Logs.DEBUG, String.format(
						"  `-> Bind event %s on method %s::%s()",
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
				if (LOGGER.isLoggable(Logs.DEBUG_FINE)) {
					String argsm = "";
					for (int i = 0; i < args.length; ++i)
						argsm += (i > 0 ? ", " : "") + (args[i] == null ? "null" : args[i].toString());
					LOGGER.log(Logs.DEBUG_FINE, "  Repeat event " + listener.getEventType().getSimpleName() + " [" + argsm + "]"
							+ " to " + listener.toString());
				}
				// Notify listener
				execute(listener.getEventType(), listener.getMethod(), listener, args);
			}
		}
		
	}
	
	@Override
	public final void notify(Instance<?> target, Class<?> eventType, Object... args) {
		notify(eventType, args, new ArrayList<>(listeners).stream()
			.filter((item) -> {
				// On vérifie que ce soit bien ce type d'event qui soit demandé, et
				// que le listener accepte les données, et également qu'il s'agit bien
				// de l'instance cible.
				return item.getInstance() == target && item.getEventType() == eventType && item.accept(args);
			}));
	}
	
	@Override
	public final void notify(Class<?> eventType, Object... args) {
		notify(eventType, args, new ArrayList<>(listeners).stream()
			.filter((item) -> {
				// On vérifie que ce soit bien ce type d'event qui soit demandé, et
				// que le listener accepte les données.
				return item.getEventType() == eventType && item.accept(args);
			}));
	}
	
	private void notify(Class<?> eventType, Object[] args, Stream<Listener<?>> steam) {
		
		// Debug
		final boolean debug = LOGGER.isLoggable(Logs.DEBUG_FINE);
		if (debug) {
			String argsm = "";
			for (int i = 0; i < args.length; ++i)
				argsm += (i > 0 ? ", " : "") + (args[i] == null ? "null" : args[i].toString());
			LOGGER.log(Logs.DEBUG_FINE, "Notify event " + eventType.getSimpleName() + " [" + argsm + "]");
		}
		
		// Mark as repeated
		if (repeatedEvents.containsKey(eventType)) {
			repeatedEvents.put(eventType, args);
		}
		
		// Broadcast
		steam.forEach((item) -> {
				// Debug
				if (debug) {
					LOGGER.log(Logs.DEBUG_FINE, "  `-> To handler " + item);
				}
				// On execute le listener
				item.notify(args);
			});
	}

	protected final Object execute(Class<?> clazz, Method m, Listener<?> item, Object[] args) {
		if (m == null) {
			throw new NullPointerException("Method is null");
		}
		try {
			Object target = item.isStaticCall() ? null : item.getTarget();
			if (!item.isStaticCall() && target == null)
				throw new NullPointerException("Invoke target is null when calling "
						+ ReflectionUtils.toString(m));
			return ReflectionUtils.invokeMethod(target, m, args);
		}
		catch (IllegalArgumentException e) {
			LOGGER.log(Logs.ERROR, "Dispatch error");
			LOGGER.log(Logs.INFO, "  Listener: " + item);
			LOGGER.log(Logs.INFO, "  Event " + clazz.getSimpleName() + " : " + e.getMessage());
			if (e.getMessage().equals("object is not an instance of declaring class")) {
				LOGGER.log(Logs.INFO, "  Object class: " + item.getTarget().getClass().getName());
				LOGGER.log(Logs.INFO, "  Declaring class: " + m.getDeclaringClass().getName());
			}
			LOGGER.log(Logs.INFO, "  Target method is " + ReflectionUtils.toString(item.getTarget(), m));
			LOGGER.log(Logs.INFO, "  Event arguments are: (" + Arrays.stream(args).map((o) -> o.getClass().getName()).collect(Collectors.joining(", ")) + ")");
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return Void.TYPE;
	}
	
	@Override
	public void removeAllListeners() {
		listeners.clear();
	}
	
	public final void repeatForNewListeners(Class<?>... events) {
		for (Class<?> event : events) {
			repeatedEvents.put(event, null);
		}
	}
	
	protected List<Listener<?>> getListeners() {
		return listeners;
	}
	
	@ToOverride
	public /*abstract*/ boolean isGuiDispatchThread() {
		return true;
	}
	
	@ToOverride
	protected /*abstract*/ void invokeAndWaitOnGuiDispatchThread(Runnable task) 
			throws InterruptedException {
		task.run();
	}
	
	@ToOverride
	protected /*abstract*/ <T> T invokeAndWaitOnGuiDispatchThread(Callable<T> task) {
		try {
			return task.call();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@ToOverride
	protected /*abstract*/ void invokeLaterOnGuiDispatchThread(Runnable task) {
		new Thread(task).start();
	}

}
