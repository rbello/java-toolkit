package fr.evolya.javatoolkit.app.cdi;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Stream;

import fr.evolya.javatoolkit.app.cdi.Instance.FuturInstance;
import fr.evolya.javatoolkit.code.Logs;
import fr.evolya.javatoolkit.code.annotations.Inject;
import fr.evolya.javatoolkit.code.funcint.Action;
import fr.evolya.javatoolkit.code.utils.ReflectionUtils;

/**
 * CDI Container
 */
public class DependencyInjectionContext {

	/**
	 * Current logger for the CDI manager.
	 */
	public static final Logger LOGGER = Logs.getLogger("CDI");

	/**
	 * Contains an association array mapping instances with their classes.
	 * Take into account that this CDI container can hold only one instance
	 * for each class.
	 */
	private Map<Class<?>, Instance<?>> components = new HashMap<>();

	/**
	 * List of all injections. An injection is an object targeting a specific field
	 * attached to a specific instance, and the component's class to inject into.
	 */
	private List<Injection> injections = new ArrayList<>();

	/**
	 * Contain a delegate handler able to execute the given task into a the right
	 * GUI context. For example, the Event Dispatch Thread in Swing Application.
	 */
	private Action<Runnable> guiDelegate;

	/**
	 * Main constructor.
	 *
	 * @param guiDelegate Delegate handler used to execute GUI tasks properly
	 */
	public DependencyInjectionContext(Action<Runnable> guiDelegate) {
		this.guiDelegate = guiDelegate;
	}

	/**
	 * Returns a map containing an association array mapping component's instances with their classes.
	 */
	public Map<Class<?>, Instance<?>> getComponents() {
		// TODO Why do not returns a copy instead of the original instance ?
		return components;
	}

	/**
	 * Returns the unique instance placeholder of the component having the given class.
	 *
	 * @param type Class of the instance asked
	 * @return The component placeholder object (instance), or null if the given class is not bound
	 * @throws NullPointerException If given type is null
	 * @throws ClassCastException If the stored component cannot be cast to the given type. Not likely
	 *           to happen because component's type is checked before inserting into CDI container.
	 */
	@SuppressWarnings("unchecked")
	public <T> Instance<T> getComponent(Class<T> type) {
		if (type == null) throw new NullPointerException();
		if (!components.containsKey(type)) {
			if (LOGGER.isLoggable(Logs.WARNING)) {
				LOGGER.log(Logs.WARNING, "Component not found: " + type.getSimpleName());
			}
			return null;
		}
		return (Instance<T>) components.get(type);
	}

	/**
	 * Returns the unique instance object of the component having the given class.
	 *
	 * @param type Class of the instance asked
	 * @return The component placeholder object (instance), or null if the given class is not bound
	 * @throws NullPointerException If given type is null
	 * @throws ClassCastException If the stored component cannot be cast to the given type. Not likely
	 *           to happen because component's type is checked before inserting into CDI container.
	 */
	public <T> T getInstance(Class<T> type) {
		Instance<T> instance = getComponent(type);
		if (instance == null) {
			return null;
		}
		if (type == null) throw new NullPointerException();
		return (T) instance.getInstance(guiDelegate);
	}
	
	/**
	 * Short alias
	 * @see DependencyInjectionContext#register(Class, Instance)
	 */
	public <T> void register(Class<T> type) {
		register(type, new FuturInstance<T>(type));
	}
	
	/**
	 * Short alias
	 * @see DependencyInjectionContext#register(Class, Instance)
	 */
	public <T> void register(Class<? extends T> type, T object) {
		register(type, new Instance<T>(object));
	}

	/**
	 * Register a new object into the CDI container.
	 *
	 * @param type The type used as identifier for the stored instance
	 * @param instance The instance to store into CDI context
	 * @throws NullPointerException If one or the other 'type' and 'instance' is/are null
	 * @throws IllegalArgumentException If given 'instance' hasn't the given 'type'
	 * @throws IllegalStateException If a component is already registred with this 'type'
	 */
	public void register(Class<?> type, Instance<?> instance) {
		
		// Check if the given placeholder's object if really an instance of the given type
		if (!ReflectionUtils.isInstanceOf(type, instance.getInstanceClass())) {
			throw new IllegalArgumentException(instance.getInstanceClass().getSimpleName() 
					+ " is not a subclass of " + type.getSimpleName());
		}

		// Lock on the given instance
		synchronized (instance) {

			// Check if a component with the same type exists
			synchronized (components) {
				if (components.containsKey(type)) {
					throw new IllegalStateException("Component with class '" + type.getSimpleName() + "' already exists");
				}
				components.put(type, instance);
			}

			// The instance is not already created, so this object will be handled later
			if (instance.isFutur()) {
				((FuturInstance<?>)instance).onInstanceCreated((object) -> {
					whenInstanceCreated(instance, true);
				});
			}
			else {
				whenInstanceCreated(instance, false);
			}

			// Search for wired injections on this object
			searchInjections(instance);
		}
	}

	/**
	 * Private method used to execute injections when an instance is created.
	 *
	 * @param instance The instance concerned
	 * @param lazyCreation Indicates whereas the instance was already created or
	 *                 created on-demand (lazy creation). 
	 * @throws NullPointerException If no component was found to inject into
	 */
	private void whenInstanceCreated(Instance<?> instance, boolean lazyCreation) {

		// Log
		LOGGER.log(Logs.DEBUG_FINE, "Instance created: " + instance.getInstanceClass()
				+ " (" + (lazyCreation ? "lazy creation" : "already created") + ")");

		// Execute injections contained in this instance
		injections.stream()
			.filter(injection -> injection.instance == instance && !injection.done)
			.forEach((injection) -> {
				Object object = getInstance(injection.type);
				if (object == null) {
					throw new NullPointerException(String.format(
							"Unable to get component having class '%s' into %s::%s (%s)",
							injection.type.getSimpleName(),
							injection.instance.getInstanceClass().getSimpleName(),
							injection.field.getName(),
							injection.instance.getInstanceHashCode()));
				}
				else {
					injection.inject(object);
					injection.done = true;
					// TODO Remove injection from injections map?
				}
			});

		// Execute injections targeting this instance
		injections.stream()
			.filter(injection -> !injection.done && 
					injection.type == instance.getInstanceClass())
			.forEach((injection) -> {
				injection.inject(instance.getInstance(guiDelegate));
				injection.done = true;
				// TODO Remove injection from injections map?
			});
	}

	/**
	 * Execute manually the research of injections on an object without registering
	 * into the CDI container. This method is not recommended because links between
	 * object is weak.
	 *
	 * @param object The target
	 * @throws NullPointerException If the given instance is null, or hasn't any class
	 */
	@Deprecated
	public <T> void searchInjections(final T object) {
		if (object == null) {
			throw new NullPointerException();
		}
		searchInjections(new Instance<T>(object));
	}

	/**
	 * Execute manually the research of injections on an object without registering
	 * into the CDI container.
	 *
	 * @param object The target
	 * @throws NullPointerException If the given instance is null, or hasn't any class
	 * @throws IllegalArgumentException If the given instance is a pure futur object, a.k.a. without any class
	 * @throws UnsupportedOperationException If the type of injected attribute is invalid
	 */
	protected void searchInjections(final Instance<?> instance) {

		if (instance == null) {
			throw new NullPointerException();
		}

		// The given instance hasn't any class
		if (instance.getInstanceClass() == null) {
			throw new IllegalArgumentException("Cannot inject on pure futur objet");
		}

		// Fetch all fields of the instance's class
		Stream.concat(
				Arrays.stream(instance.getInstanceClass().getDeclaredFields()), // locales + privates
				Arrays.stream(instance.getInstanceClass().getFields()) // inherited + public
			)
			.distinct()
			// Filter only those with @Inject annotation
			.filter(field -> field.isAnnotationPresent(Inject.class))
			// Fetch
			.forEach((field) -> {

				// Annotation can specify the type
				Class<?> type = field.getAnnotation(Inject.class).value();
				// But otherwise we gather the type using field declaration
				if (type == null || type == Void.class) {
					type = field.getType();
				}

				// Check 
				if (type == Instance.class || type == FuturInstance.class) {
					throw new UnsupportedOperationException(String.format(
						"Cannot inject %s::%s because given type if a placeholder (%s) not a real type",
						instance.getInstanceClass().getSimpleName(),
						field.getName(),
						type.getSimpleName()
					));
				}

				// Futur injection
				if (instance.isFutur() || !isComponentRegistered(type)) {
					injections.add(new Injection(instance, field, type));
					if (LOGGER.isLoggable(Logs.DEBUG_FINE)) {
						LOGGER.log(Logs.DEBUG_FINE, String.format(
								"  `-> Create dependency injection of component '%s' on field %s::%s (%s)",
								type.getSimpleName(),
								instance.getInstanceClass().getSimpleName(),
								field.getName(),
								"futur"
								));
					}
				}
				// Present injection is executed right now
				else {
					new Injection(instance, field, type)
						.inject(getComponent(type).getInstance());
				}
				
			});
	}

	/**
	 * Returns TRUE if a component identified by the given type was registred into this CDI context container.
	 */
	public boolean isComponentRegistered(Class<?> type) {
		return components.containsKey(type);
	}

	/**
	 * Unregister the component identified by the given type from this CDI context container.
	 *
	 * @return FALSE if there was no component mapping for the given type, TRUE otherwise
	 */
	public boolean unregister(Class<?> type) {
		synchronized (components) {
			return components.remove(type) != null;
		}
	}

	/**
	 * Construct all futur objects contained in this CDI context.
	 */
	public void build() {
		synchronized (components) {
			for (Instance<?> component : components.values()) {
				// Already created
				if (!component.isFutur()) continue;
				// Request the creation of this object using the delegate
				component.getInstance(guiDelegate);
			}
		}
	}
	
	/**
	 * Manually inject a dependency to the given object into his attribute
	 * 
	 * @param object The target object
	 * @param attributeName Attribute name in the target object
	 * @param typeToInject Class of component to inject
	 * @throws IllegalArgumentException For many reasons (types mismatch, component non registred, etc.)
	 */
	public <T> void inject(T object, String attributeName, Class<?> typeToInject) {
		// Search the right field
		Field field = ReflectionUtils.getFieldMatching(object.getClass(), attributeName);
		// Inject the object into it
		new Injection(new Instance<T>(object), field, typeToInject).inject();
	}
	
	/**
	 * Contains a futur injection, a.k.a mapping between an object attribute and a component to inject later.
	 */
	private class Injection {
		
		public boolean done = false;
		public final Instance<?> instance;
		public final Field field;
		public final Class<?> type;
		
		public Injection(Instance<?> instance, Field field, Class<?> type) {
			this.instance = instance;
			this.field = field;
			this.type = type;
		}
		
		public void inject() {
			if (!isComponentRegistered(type)) {
				throw new IllegalArgumentException(String.format(
						"Unable to inject %s into %s::%s because %s is not registred",
						type.getSimpleName(),
						this.instance.getInstanceClass().getSimpleName(),
						field.getName(),
						type.getSimpleName()));
			}
			inject(getInstance(type));
		}

		public void inject(Object obj) {
			LOGGER.log(Logs.DEBUG_FINE, "  `-> Inject " + obj.getClass().getSimpleName() + " into " + this);
			field.setAccessible(true);
			try {
				field.set(this.instance.getInstance(guiDelegate), obj);
				//done = true;
			}
			catch (IllegalArgumentException e) {
				throw new IllegalArgumentException(String.format(
						"Unable to inject %s into %s::%s because types mismatche\nExpected: %s\nGiven:    %s",
						obj.getClass().getSimpleName(),
						this.instance.getInstanceClass().getSimpleName(),
						field.getName(),
						field.getType().getSimpleName(),
						obj.getClass().getSimpleName()));
			}
			catch (IllegalAccessException e) {
				throw new IllegalArgumentException(String.format(
						"Unable to inject %s into %s::%s because illegal access (private attribute)",
						obj.getClass().getSimpleName(),
						this.instance.getInstanceClass().getSimpleName(),
						field.getName()));
			}
		}
		
		@Override
		public String toString() {
//			return "(" + this.type.getSimpleName() + ")" 
//					+ instance.getInstanceClass().getSimpleName() 
//					+ "::" + field.getName();
			return instance.getInstanceClass().getSimpleName() 
					+ "::" + field.getName();
		}
		
	}
	
}
