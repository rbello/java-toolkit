package fr.evolya.javatoolkit.events.fi;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import fr.evolya.javatoolkit.app.cdi.Instance.FuturInstance;
import fr.evolya.javatoolkit.code.annotations.GuiTask;

@SuppressWarnings({"rawtypes", "unchecked"})
public class FuturListener extends Listener {

	private FuturInstance instance;

	public FuturListener(Observable observable, Class<?> eventType, FuturInstance instance, Method method, boolean isGui) {
		super(observable, eventType, instance, method);
		this.instance = instance;
		this.gui = isGui;
	}
	
	@Override
	protected Method getMethod() {
		return method;
	}
	
	@Override
	public Object getTarget() {
		// Si on doit fabriquer cet objet
		if (instance.isFutur()) {
			boolean gui = this.gui;
			if (!gui) {
				// On regarde si le constructeur vide a l'annotation des tâches de GUI
				try {
					gui = instance.getInstanceClass().getConstructor().isAnnotationPresent(GuiTask.class);
				} catch (Throwable e) {}
			}
			// Et qu'on doit le faire dans le thread GUI
			if (gui) {
				// On fabrique cet objet dans le thread UI
				return this.observable.invokeAndWaitOnGuiDispatchThread(new Callable<Object>() {
					public Object call() throws Exception {
						return instance.getInstance();
					}
				});
			}
		}
		return instance.getInstance();
	}
	
	@Override
	public String toString() {
		return "FuturListener " + instance.getInstanceClass().getSimpleName() + "::" + method.getName()
				+ " on " + getEventType().getSimpleName();
	}

}
