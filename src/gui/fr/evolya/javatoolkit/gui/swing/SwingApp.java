package fr.evolya.javatoolkit.gui.swing;

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;

import fr.evolya.javatoolkit.app.App;
import fr.evolya.javatoolkit.app.cdi.Instance.FuturInstance;

public class SwingApp extends App {

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
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
