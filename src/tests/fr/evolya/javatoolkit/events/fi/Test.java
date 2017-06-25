package fr.evolya.javatoolkit.events.fi;

import java.awt.EventQueue;
import java.util.concurrent.Callable;

import fr.evolya.javatoolkit.app.cdi.Instance;
import fr.evolya.javatoolkit.app.cdi.Instance.FuturInstance;
import fr.evolya.javatoolkit.code.Logs;
import fr.evolya.javatoolkit.test.Assert;
import fr.evolya.javatoolkit.test.Assert.BeforeTests;
import fr.evolya.javatoolkit.test.Assert.TestMethod;

public class Test extends Observable {

	private Test observable;

	@BeforeTests
	public void beforeTest() {
		this.observable = new Test();
		Logs.setLoggerLevel(Observable.LOGGER, Logs.ALL);
	}
	
	@TestMethod(1)
	public void PreparePreconditions() {
		Assert.notNull(observable.getListeners(), "getListeners()");
		Assert.equals(observable.getListeners().size(), 0, "getListeners().size()");
	}
	
	@TestMethod(2)
	public void UsingEmptyFuturInstance() {
		FuturInstance<EmptyObserver> instance = new FuturInstance<>(EmptyObserver.class);
		this.observable.addListener(instance);
		Assert.equals(observable.getListeners().size(), 0, "getListeners().size()");
	}
	
	@TestMethod(3)
	public void UsingAnnotedFuturInstance() {
		FuturInstance<AnnotationObserver> instance = new FuturInstance<>(AnnotationObserver.class);
		this.observable.addListener(instance);
		Assert.equals(observable.getListeners().size(), 2, "getListeners().size()");
	}
	
	@TestMethod(4)
	public void UsingAnnotedInstance() {
		Instance<AnnotationObserver> instance = new Instance<>(new AnnotationObserver());
		this.observable.addListener(instance);
		Assert.equals(observable.getListeners().size(), 4, "getListeners().size()");
	}
	
	@TestMethod(5)
	public void UsingMethods() {
		this.observable.when(Event2.class).execute((a) -> a.nothing());
		Assert.equals(observable.getListeners().size(), 5, "getListeners().size()");
	}
	
	public static void main(String[] args) {
		Assert.runTests(Test.class);
	}

	@Override
	public boolean isGuiDispatchThread() {
		return EventQueue.isDispatchThread();
	}

	@Override
	protected void invokeAndWaitOnGuiDispatchThread(Runnable task) throws InterruptedException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected <T> T invokeAndWaitOnGuiDispatchThread(Callable<T> task) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void invokeLaterOnGuiDispatchThread(Runnable task) {
		throw new UnsupportedOperationException();
	}
	
	private static class EmptyObserver { 
		public void nothing() { }
	}
	
	private static class AnnotationObserver {
		@BindOnEvent(Event1.class)
		public void eventHandler1() { }
		@BindOnEvent(Event2.class)
		public void eventHandler2(EmptyObserver arg) { }
	}
	
	@FunctionalInterface
	private static interface Event1 {
		public void toto();
	}
	
	@FunctionalInterface
	private static interface Event2 {
		public void toto(EmptyObserver arg);
	}
	
}
