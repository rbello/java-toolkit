package fr.evolya.javatoolkit.app.cdi;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fr.evolya.javatoolkit.app.cdi.Instance.FuturInstance;
import fr.evolya.javatoolkit.code.Logs;
import fr.evolya.javatoolkit.code.annotations.Inject;
import fr.evolya.javatoolkit.code.funcint.Action;
import fr.evolya.javatoolkit.code.utils.ReflectionUtils;

/**
 * CDI Container
 * 
 * @version 1.5
 */
public class DependencyInjectionContext {

	/**
	 * Current logger for the CDI manager.
	 */
	public static final Logger LOGGER = Logs.getLogger("CDI");

	/**
	 * Default behavior when a component doesn't exists when an injection is executed.
	 * With this behavior, an exception is thrown and the build() method is halted.
	 * 
	 * @see DependencyInjectionContext#setMissingComponentInjectionBehavior(int)
	 */
	public static final int BEHAVIOR_THROW_EXCEPTION = 1;

	/**
	 * Possible behavior when a component doesn't exists when an injection is executed.
	 * With this behavior, NO exception will be thrown and the build() method will continue
	 * if a component's field is bound to an unexisting component.
	 * 
	 * @see DependencyInjectionContext#setMissingComponentInjectionBehavior(int)
	 */
	public static final int BEHAVIOR_WAIT_FOR_IT = 2;

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
	 * The behavior when a missing component is required for an injection.
	 * 
	 * @see DependencyInjectionContext#setMissingComponentInjectionBehavior(int)
	 */
	private int componentMissingBehavior = BEHAVIOR_THROW_EXCEPTION;
	
	/**
	 * Main constructor.
	 *
	 * @param guiDelegate Delegate handler used to execute GUI tasks properly
	 * @since 1.0
	 */
	public DependencyInjectionContext(Action<Runnable> guiDelegate) {
		this.guiDelegate = guiDelegate;
	}

	/**
	 * Returns the unique instance object of the component having the given class.
	 *
	 * @param type Class of the instance asked
	 * @return The component placeholder object (instance), or null if the given class is not bound
	 * @throws NullPointerException If given type is null
	 * @throws ClassCastException If the stored component cannot be cast to the given type. Not likely
	 *           to happen because component's type is checked before inserting into CDI container.
	 * @since 1.0
	 */
	@SuppressWarnings("unchecked")
	public <T> T getInstance(Class<T> type) {
		if (type == null) {
			throw new NullPointerException();
		}
		Instance<T> instance = null;
		synchronized (components) {
			instance = (Instance<T>) components.get(type);
		}
		if (instance == null) {
			return null;
		}
		return (T) instance.getInstance(guiDelegate);
	}

	/**
	 * Returns a list containing all components' classes registred into this CDI contexte.
	 * 
	 * @since 1.5
	 */
	public List<Class<?>> getComponents() {
		synchronized (components) {
			return new LinkedList<Class<?>>(components.keySet());
		}
	}
	
	/**
	 * Returns a copy of the map containing all components registred into this CDI contexte.
	 * 
	 * @since 1.4
	 */
	public Map<Class<?>, Instance<?>> getComponentsMap() {
		synchronized (components) {
			return new HashMap<Class<?>, Instance<?>>(components);
		}
	}

	/**
	 * Returns the unique instance placeholder of the component having the given class.
	 *
	 * @param type Class of the instance asked
	 * @return The component placeholder object (instance), or null if the given class is not bound
	 * @throws NullPointerException If given type is null
	 * @throws ClassCastException If the stored component cannot be cast to the given type. Not likely
	 *           to happen because component's type is checked before inserting into CDI container.
	 * @since 1.0
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
		synchronized (components) {
			return (Instance<T>) components.get(type);
		}
	}

	/**
	 * Return TRUE if a component identified by the given type was registred into this CDI context container.
	 * 
	 * @since 1.0
	 */
	public boolean isComponentRegistered(Class<?> type) {
		return components.containsKey(type);
	}
	
	/**
	 * Short alias
	 * @see DependencyInjectionContext#register(Class, Instance)
	 * @since 1.4
	 */
	public <T> void register(Class<T> type) {
		register(type, new FuturInstance<T>(type));
	}
	
	/**
	 * Short alias
	 * @see DependencyInjectionContext#register(Class, Instance)
	 * @since 1.4
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
	 * @since 1.0
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
	 * Execute manually the research of injections on an object without registering
	 * into the CDI container.
	 *
	 * @param object The target
	 * @throws NullPointerException If the given instance is null, or hasn't any class
	 * @since 1.3
	 */
	public <T> void searchInjections(final T object) {
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
	 * @since 1.0
	 */
	public void searchInjections(final Instance<?> instance) {

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
						"Cannot inject into %s::%s because given type if a placeholder (%s) not a real type",
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
							"  `-> Create dependency injection of component '%s' into field %s::%s (futur)",
							type.getSimpleName(),
							instance.getInstanceClass().getSimpleName(),
							field.getName()
							));
					}
				}
				// Present injection is executed right now
				else {
					new Injection(instance, field, type).execute();
						//.inject(getComponent(type).getInstance());
				}
				
			});
	}

	/**
	 * Private method used to execute injections when an instance is created.
	 *
	 * @param instance The instance concerned
	 * @param lazyCreation Indicates whereas the instance was already created or
	 *                 created on-demand (lazy creation). 
	 * @since 1.1
	 */
	private void whenInstanceCreated(Instance<?> instance, boolean lazyCreation) {

		// Log
		LOGGER.log(Logs.DEBUG_FINE, "Instance created: " + instance.getInstanceClass()
				+ " (" + (lazyCreation ? "lazy creation" : "already created") + ")");

		// Execute injections contained in this instance
		injections.stream()
			.filter(injection -> injection.instance == instance && !injection.isExecuted())
			.forEach((injection) -> injection.execute());

		// Execute injections targeting this instance
		injections.stream()
			.filter(injection -> injection.type == instance.getInstanceClass() && !injection.isExecuted())
			.forEach((injection) -> injection.execute());
	}

	/**
	 * Unregister the component identified by the given type from this CDI context container.
	 *
	 * @return FALSE if there was no component mapping for the given type, TRUE otherwise
	 * @since 1.0
	 */
	public boolean unregister(Class<?> type) {
		synchronized (components) {
			return components.remove(type) != null;
		}
	}

	/**
	 * Construct all futur objects contained in this CDI context.
	 * 
	 * @since 1.1
	 */
	public void build() {
		// Objects to build
		List<Instance<?>> toBuild = new LinkedList<Instance<?>>();
		// Lock on components array
		synchronized (components) {
			for (Instance<?> component : components.values()) {
				// Already created
				if (!component.isFutur()) continue;
				// Component is going to be built
				toBuild.add(component);
			}
		}
		// Fetch components to build and request the creation of this object using the delegate
		toBuild.forEach(component -> component.getInstance(guiDelegate));
	}

	/**
	 * Manually inject a dependency to the given object into his attribute
	 * 
	 * @param object The target object
	 * @param attributeName Attribute name in the target object
	 * @param typeToInject Class of component to inject
	 * @throws IllegalArgumentException For many reasons (types mismatch, component non registred, etc.)
	 * @since 1.4
	 */
	public <T> void inject(T object, String attributeName, Class<?> typeToInject) {
		// Search the right field
		Field field = ReflectionUtils.getFieldMatching(object.getClass(), attributeName);
		// Inject the object into it
		new Injection(new Instance<T>(object), field, typeToInject).execute();
	}

	/**
	 * Debug method.
	 * 
	 * @since 1.5
	 */
	protected List<String> getUnexecutedInjections() {
		return injections.stream()
				.filter(i -> !i.isExecuted())
				.map(i -> String.format(
						"(%s) %s",
						i.type.getSimpleName(),
						i.toString()))
				.collect(Collectors.toList());
	}

	/**
	 * Change the behavior when a missing component is required for an injection.
	 * 
	 * @param behavior The behavior's value code
	 * @throws IllegalArgumentException
	 * @see {@link DependencyInjectionContext#BEHAVIOR_THROW_EXCEPTION}
	 * @see {@link DependencyInjectionContext#BEHAVIOR_WAIT_FOR_IT}
	 * @since 1.5
	 */
	public void setMissingComponentInjectionBehavior(int behavior) {
		// Check value
		if (behavior != BEHAVIOR_THROW_EXCEPTION && behavior != BEHAVIOR_WAIT_FOR_IT) {
			throw new IllegalArgumentException("Invalid behavior");
		}
		// No change
		if (componentMissingBehavior == behavior) {
			return;
		}
		componentMissingBehavior = behavior;
	}

	/**
	 * Different states of injection.
	 * 
	 * @since 1.5
	 */
	private static enum InjectionState {
		TODO, // Has to be done
		NOTFOUND, // Component to inject is not found
		FAILURE, // Injection failure, no futur attempts
		DOING, // Injection is executed right now
		DONE // Injection executed, no futur attempts
	}

	/**
	 * Contains a futur injection, a.k.a mapping between an object attribute and a component to inject later.
	 * 
	 * @since 1.0
	 */
	public final class Injection {

		protected InjectionState state = InjectionState.TODO;

		public final Instance<?> instance;
		public final Field field;
		public final Class<?> type;

		public Injection(Instance<?> instance, Field field, Class<?> type) {
			if (instance == null || field == null || type == null) {
				throw new NullPointerException();
			}
			this.instance = instance;
			this.field = field;
			this.type = type;
		}

		public boolean isExecuted() {
			return (state == InjectionState.DONE || state == InjectionState.FAILURE);
		}

		/**
		 * TODO Doc exception
		 * @throws NoSuchElementException
		 * @throws IllegalArgumentException
		 * @throws IllegalAccessException
		 * @throws StackOverflowError
		 * 
		 * @return TRUE if the injection was done, or FALSE if the injection raised a fatal
		 * 			error, is delayed due to missing component, or currently executing
		 */
		public boolean execute() {
			
			// Doing right now
			if (state == InjectionState.DOING) return false;

			// Already done
			if (state == InjectionState.DONE) return true;

			// Failure
			if (state == InjectionState.FAILURE) return false;

			// Check circular reference
			if (instance.getInstanceClass() == type) {
				throw new StackOverflowError(String.format(
					"Recursive injection of component '%s' into field %s::%s",
					type.getSimpleName(),
					type.getSimpleName(),
					field.getName()));
			}

			// The component to inject is missing
			if (!isComponentRegistered(type)) {
				
				// Mark this injection to failed
				state = InjectionState.NOTFOUND;

				// The behavior is to throw an exception
				if (componentMissingBehavior == BEHAVIOR_THROW_EXCEPTION) {
					throw new NoSuchElementException(String.format(
						"Unable to inject component '%s' into %s::%s because this component doesn't exists",
						type.getSimpleName(),
						instance.getInstanceClass().getSimpleName(),
						field.getName()));
				}

				// Otherwise, just return FALSE indicating the injection is delayed
				return false;

			}

			// Log
			LOGGER.log(Logs.DEBUG_FINE, "  `-> Inject component '" + type.getSimpleName() + "' into field " + this);
			
			// Try to execute the injection
			try {
				synchronized (this) {
					state = InjectionState.DOING;
					field.setAccessible(true);
					field.set(
						instance.getInstance(guiDelegate),
						getInstance(type)
					);
					state = InjectionState.DONE;
				}
				return true;
			}

			// Arguments mismatch
			catch (IllegalArgumentException e) {
				state = InjectionState.FAILURE;
				throw new IllegalArgumentException(String.format(
					"Unable to inject component '%s' into %s::%s because types mismatche\nExpected: %s\nGiven:    %s",
					type.getSimpleName(),
					instance.getInstanceClass().getSimpleName(),
					field.getName(),
					field.getType().getSimpleName(),
					type.getSimpleName()));
			}

			// Security error
			catch (IllegalAccessException e) {
				state = InjectionState.FAILURE;
				throw new SecurityException(String.format(
					"Unable to inject component '%s' into %s::%s because illegal access (private attribute)",
					type.getSimpleName(),
					instance.getInstanceClass().getSimpleName(),
					field.getName()));
			}
		}
		
		@Override
		public String toString() {
			return instance.getInstanceClass().getSimpleName() + "::" + field.getName();
		}
		
	}
	
}
