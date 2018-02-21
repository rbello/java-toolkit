package fr.evolya.javatoolkit.events.fi;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

public class DecorableObservable extends Observable {
	
	private Observable dispatcher;

	public DecorableObservable() {
		super();
	}
	
	public DecorableObservable(Observable dispatcher) {
		this.dispatcher = dispatcher;
	}

	@Override
	protected void notify(Class<?> eventType, Object[] args, Stream<Listener<?>> steam) {
		if (dispatcher != null)
			dispatcher.notify(eventType, args, steam);
		else
			super.notify(eventType, args, steam);
	}

	@Override
	public void removeAllListeners() {
		if (dispatcher != null)
			dispatcher.removeAllListeners();
		else
			super.removeAllListeners();
	}

	@Override
	public List<Listener<?>> getListeners() {
		if (dispatcher != null)
			return dispatcher.getListeners();
		else
			return super.getListeners();
	}

	@Override
	public boolean isGuiDispatchThread() {
		if (dispatcher != null)
			return dispatcher.isGuiDispatchThread();
		else
			return super.isGuiDispatchThread();
	}

	@Override
	protected void invokeAndWaitOnGuiDispatchThread(Runnable task) throws InterruptedException {
		if (dispatcher != null)
			dispatcher.invokeAndWaitOnGuiDispatchThread(task);
		else
			super.invokeAndWaitOnGuiDispatchThread(task);
	}

	@Override
	protected <T> T invokeAndWaitOnGuiDispatchThread(Callable<T> task) {
		if (dispatcher != null)
			return dispatcher.invokeAndWaitOnGuiDispatchThread(task);
		else
			return super.invokeAndWaitOnGuiDispatchThread(task);
	}

	@Override
	protected void invokeLaterOnGuiDispatchThread(Runnable task) {
		if (dispatcher != null)
			dispatcher.invokeLaterOnGuiDispatchThread(task);
		else
			super.invokeLaterOnGuiDispatchThread(task);
	}

	public Observable getDispatcher() {
		return dispatcher;
	}

	public void setDispatcher(Observable dispatcher) {
		this.dispatcher = dispatcher;
	}

}
