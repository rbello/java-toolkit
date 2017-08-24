package fr.evolya.javatoolkit.app.cdi;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import fr.evolya.javatoolkit.app.cdi.Instance.FuturInstance;
import fr.evolya.javatoolkit.code.Logs;
import fr.evolya.javatoolkit.code.annotations.Inject;
import fr.evolya.javatoolkit.code.funcint.Action;
import fr.evolya.javatoolkit.code.utils.ReflectionUtils;

public class DependencyInjectionContext {
	
	public static final Logger LOGGER = Logs.getLogger("CDI");
	
	private Map<Class<?>, Instance<?>> components = new HashMap<>();

	private List<Injection> injections = new ArrayList<>();

	private Action<Runnable> guiDelegate;
	
	public DependencyInjectionContext(Action<Runnable> guiDelegate) {
		this.guiDelegate = guiDelegate;
	}

	public Map<Class<?>, Instance<?>> getComponents() {
		return components;
	}
	
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
	
	@SuppressWarnings("unchecked")
	public <T> T getInstance(Class<T> type) {
		if (type == null) throw new NullPointerException();
		if (!components.containsKey(type)) {
			if (LOGGER.isLoggable(Logs.WARNING)) {
				LOGGER.log(Logs.WARNING, "Component not found: " + type.getSimpleName());
			}
			return null;
		}
		return (T) components.get(type).getInstance(guiDelegate);
	}
	
	public void register(Class<?> type, Instance<?> instance) {
		if (!ReflectionUtils.isInstanceOf(type, instance.getInstanceClass()))
			throw new IllegalArgumentException(instance.getInstanceClass().getSimpleName() 
					+ " is not a subclass of " + type.getSimpleName());
		synchronized (instance) {
			// Check is a component with the same class exists
			synchronized (components) {
				if (components.containsKey(type)) {
					throw new IllegalAccessError("Component " + type.getSimpleName() + " allready exists");
				}
				components.put(type, instance);
			}

			if (instance.isFutur()) {
				((FuturInstance<?>)instance).onInstanceCreated((object) -> {
					whenInstanceCreated(instance, true);
				});
			}
			else {
				whenInstanceCreated(instance, false);
			}
			searchInjections(instance);
		}
	}

	private void whenInstanceCreated(Instance<?> instance, boolean lazyCreation) {
		LOGGER.log(Logs.DEBUG_FINE, "Instance created: " + instance.getInstanceClass()
				+ " (" + (lazyCreation ? "lazy creation" : "already created") + ")");
		
		// Les injections de cette instance
		injections.stream()
			.filter(injection -> injection.instance == instance && !injection.done)
			.forEach((injection) -> {
				Object object = getInstance(injection.type);
				if (object == null) {
					throw new NullPointerException("Unable to get component "
							+ injection.type);
				}
				else {
					injection.inject(object);
					injection.done = true;
				}
			});
		
		// Les injections pointant vers cette instance 
		injections.stream()
			.filter(injection -> !injection.done && 
					injection.type == instance.getInstanceClass())
			.forEach((injection) -> {
				injection.inject(instance.getInstance(guiDelegate));
				injection.done = true;
			});
	}

	private void searchInjections(Instance<?> instance) {
		
		// L'instance donnée est future mais ne possède même pas de classe
		if (instance.getInstanceClass() == null)
			throw new NullPointerException("Cannot inject on pure futur objet");
		
		// Toutes les méthodes de l'instance
		Arrays.stream(instance.getInstanceClass().getFields())
			// Uniquement celles avec l'annotation d'injection
			.filter(field -> field.isAnnotationPresent(Inject.class))
			// Fetch
			.forEach((field) -> {
				// Recherche du type par l'annotation
				Class<?> type = field.getAnnotation(Inject.class).value();
				// Si l'annotation n'est pas définie, on recupère le type
				// de l'attribut.
				if (type == null || type == Void.class) {
					type = field.getType();
				}
				if (type == Instance.class || type == FuturInstance.class) {
					throw new UnsupportedOperationException("Cannot inject instance directly");
				}
				
				boolean done = false;

				// Futur injection
				if (instance.isFutur() || !isComponentRegistered(type)) {
					injections.add(new Injection(instance, field, type));
					if (LOGGER.isLoggable(Logs.DEBUG_FINE)) {
						LOGGER.log(Logs.DEBUG_FINE, String.format(
								"  `-> Create dependency injection of object '%s' on field %s::%s (%s)",
								type.getSimpleName(),
								instance.getInstanceClass().getSimpleName(),
								field.getName(),
								done ? "done" : "futur"
								));
					}
				}
				// Present injection
				else {
					new Injection(instance, field, type)
						.inject(getComponent(type).getInstance());
					done = true;
				}
				
			});
	}

	public boolean isComponentRegistered(Class<?> type) {
		return components.containsKey(type);
	}

	public boolean unregister(Class<?> clazz) {
		if (!components.containsKey(clazz)) return false;
		components.remove(clazz);
		// TODO Cleanup ?
		return true;
	}
	

	public void build() {
		synchronized (components) {
			for (Instance<?> component : components.values()) {
				// Allready created
				if (!component.isFutur()) continue;
				component.getInstance(guiDelegate);
			}
		}
	}
	
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
		
		public void inject(Object instance) {
			LOGGER.log(Logs.DEBUG_FINE, "  `-> Inject " + instance.getClass().getSimpleName() + " into " + this);
			field.setAccessible(true);
			try {
				field.set(this.instance.getInstance(guiDelegate), instance);
			}
			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("Injection field: " + this);
				System.out.println("Argument object: " + instance.getClass());
				return;
			}
		}
		
		@Override
		public String toString() {
			return "(" + this.type.getSimpleName() + ")" 
					+ instance.getInstanceClass().getSimpleName() 
					+ "::" + field.getName();
		}
		
	}

	public void inject(Class<?> targetType, String attributeName, Class<?> typeToInject) {
		try {
			Field field = ReflectionUtils.getFieldMatching(targetType, attributeName);
			injections.add(new Injection(getComponent(targetType), field, typeToInject));
			if (LOGGER.isLoggable(Logs.DEBUG_FINE)) {
				LOGGER.log(Logs.DEBUG_FINE, String.format(
						"  `-> Create dependency injection of object '%s' on field %s::%s (%s)",
						typeToInject.getSimpleName(),
						targetType.getSimpleName(),
						attributeName,
						"futur"
						));
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
}
