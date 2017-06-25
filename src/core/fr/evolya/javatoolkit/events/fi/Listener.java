package fr.evolya.javatoolkit.events.fi;

import java.lang.reflect.Method;
import java.util.function.Predicate;

import fr.evolya.javatoolkit.app.cdi.Instance;
import fr.evolya.javatoolkit.code.utils.ReflectionUtils;

@SuppressWarnings({"rawtypes", "unchecked"})
public class Listener<EVENT> {
	
	protected final Observable observable;
	protected final Class<EVENT> eventType;
	
	protected Instance target;
	protected Method method;
	
	protected boolean gui = false;
	protected Predicate<Object>[] filters = null;
	
	
	/**
	 * Constructeur.
	 *
	 * @param observable 
	 * @param eventType
	 */
	public Listener(Observable observable, Class<EVENT> eventType) {
		if (observable == null || eventType == null) {
			throw new NullPointerException();
		}
		this.observable = observable;
		this.eventType = eventType;
		this.target = null;
		this.method = null;
	}
	
//	protected Listener(Observable observable, Class<EVENT> eventType, Instance<?> target) {
//		this(observable, eventType, target, getMethod(eventType.getMethods()[0], target));
//	}
	
	protected Listener(Observable observable, Class<EVENT> eventType, Instance<?> target, Method method) {
		if (observable == null || eventType == null || target == null || method == null) {
			throw new NullPointerException();
		}
//		if (method.getDeclaringClass() != target.getInstanceClass()) {
//			throw new IllegalArgumentException("Not the same declaring class ("
//					+ method.getDeclaringClass() + " / " + target.getInstanceClass() + ")");
//		}
		this.observable = observable;
		this.eventType = eventType;
		this.target = target;
		this.method = method;
	}
	
	public Class<?> getEventType() {
		return eventType;
	}
	
	protected Method getMethod() {
		return this.method;
	}
	
	protected void notify(Object[] args) {
		Method m = getMethod();
		if (gui) {
			observable.invokeLaterOnGuiDispatchThread(() -> observable.execute(eventType, m, this, args));
		}
		else {
			observable.execute(eventType, m, this, args);
		}
	}
	
	@Override
	public String toString() {
		return "Listener " + target.getInstanceClass() + "::"
				+ (method == null ? "?" : method.getName())
				+ " on " + getEventType().getSimpleName();
	}
	
	protected boolean accept(Object[] args) {
		if (filters == null) return true;
		for (int i = 0, a = args.length, f = filters.length; i < f; ++i) {
			if (i >= a) return false;
			if (!filters[i].test(args[i])) return false;
		}
		return true;
	}
	
	public Observable execute(EVENT target) {
		return execute(target, false);
	}
	
	public Observable executeOnGui(EVENT target) {
		return execute(target, true);
	}
	
	public Observable execute(Object target, boolean onGui) {
		if (this.target != null) {
			throw new IllegalStateException("Listener is allready bound to target object");
		}
		if (target instanceof Instance) {
			this.target = (Instance<?>)target;
		}
		else {
			this.target = new Instance(target);
		}
		this.gui = onGui;
		this.method = ReflectionUtils.getMethodMatching(target, eventType.getMethods()[0]);
		// On inscrit l'observable à ce moment.
		observable.addListener(this);
		return observable;
	}
	
	@SafeVarargs
	public final Listener<EVENT> onlyOn(Predicate<Object>... filters) {
		this.filters = filters;
		return this;
	}
	
	public final Listener<EVENT> onlyOn(Class<?> filter) {
		return onlyOn((e) -> filter.isInstance(e));
	}

	public Object getTarget() {
		return target.getInstance();
	}
	
}
