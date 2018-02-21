package fr.evolya.javatoolkit.gui.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;

import fr.evolya.javatoolkit.app.App;
import fr.evolya.javatoolkit.app.cdi.Instance;
import fr.evolya.javatoolkit.app.cdi.Instance.FuturInstance;
import fr.evolya.javatoolkit.code.Logs;

public class SwingApp extends App {
	
	public SwingApp() {
		super();
	}

	public SwingApp(String[] args) {
		super(args);
	}

	@Override
	public boolean isGuiDispatchThread() {
		return EventQueue.isDispatchThread();
	}

	@Override
	public void invokeAndWaitOnGuiDispatchThread(Runnable task) throws InterruptedException {
		try {
			EventQueue.invokeAndWait(task);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void invokeLaterOnGuiDispatchThread(Runnable task) {
		EventQueue.invokeLater(task);
	}
	
	@Override
	protected void exploreDeepContainer(Instance<?> instance) {
		if (!instance.isFutur() && instance.isInstanceOf(Container.class)) {
			if (LOGGER.isLoggable(Logs.DEBUG_FINE)) {
				LOGGER.log(Logs.DEBUG_FINE, " `-> Deep explore view with specific Swing procedure: " 
						+ instance.getInstanceClass().getSimpleName());
			}
			exploreViewComponents((Container) instance.getInstance(), 0);
		}
	}

	protected void exploreViewComponents(Container instance, int depth) {
		if (depth > 0) {
			add(new Instance<>(instance), null, true, true);
		}
		for (Component c : instance.getComponents()) {
			if (c instanceof Container) {
				exploreViewComponents((Container)c, depth++);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <T> T invokeAndWaitOnGuiDispatchThread(Callable<T> task) {
		// On est dans le bon thread
		if (isGuiDispatchThread()) {
			try {
				return task.call();
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		FuturInstance<Object> result = new FuturInstance<>(Object.class);
		FuturInstance<Exception> exception = new FuturInstance<>(Exception.class);
		
		// On change de thread
		try {
			EventQueue.invokeAndWait(() -> {
				try {
					T t = task.call();
					result.setInstance(t);
				} catch (Exception ex) {
					exception.setInstance(ex);
				}
			});
			if (!exception.isFutur()) {
				throw new RuntimeException((Exception) exception.getInstance());
			}
			return (T) result.getInstance();
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return null;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
}
