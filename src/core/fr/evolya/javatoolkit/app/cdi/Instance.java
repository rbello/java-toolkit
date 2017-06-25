package fr.evolya.javatoolkit.app.cdi;

import java.lang.reflect.Constructor;

import fr.evolya.javatoolkit.code.annotations.GuiTask;
import fr.evolya.javatoolkit.code.funcint.Action;

public class Instance<T> {
	
	protected T instance = null;
	protected Class<T> type = null;
	protected boolean gui;
	
	@SuppressWarnings("unchecked")
	public <X extends T> Instance(X instance) {
		if (instance == null) {
			throw new NullPointerException();
		}
		if (instance instanceof Instance) {
			throw new IllegalArgumentException("Circular instance");
		}
		this.instance = instance;
		this.type = (Class<T>) instance.getClass();
		try {
			Constructor<?> constructor = type.getConstructor(new Class<?>[] { });
			this.gui = constructor.getAnnotation(GuiTask.class) != null;
		}
		catch (Exception e) {
			this.gui = false;
		}
	}
	
	protected Instance() {
	}
	
	public Class<?> getInstanceClass() {
		return type;
	}
	
	public Object getInstance() {
		return instance;
	}
	
	public boolean isFutur() {
		return false;
	}
	
	public boolean isGuiInstance() {
		return this.gui;
	}
	
//	public static class OptionalInstance extends Instance {
//		public OptionalInstance(Optional<?> instance) {
//			super(instance);
//			if (instance.isPresent()) this.clazz = instance.get().getClass();
//			else this.clazz = Void.class;
//		}
//	}

	public static class FuturInstance<T> extends Instance<T> {
		
//		private Class<T> type;
		private Throwable ex = null;
		private Action<T> callback;
		
		public FuturInstance() {
			this(null);
		}
		
		public FuturInstance(Class<T> type) {
			super();
			this.type = type;
			try {
				Constructor<?> constructor = type.getConstructor(new Class<?>[] { });
				this.gui = constructor.getAnnotation(GuiTask.class) != null;
			}
			catch (Exception e) {
				this.gui = false;
			}
		}
		
		@Override
		public synchronized T getInstance() {
			if (this.type == null) return null;
			if (isFutur()) {
				try {
					instance = (T) type.newInstance();
					if (this.callback != null)
						this.callback.action(instance);
				}
				catch (Throwable ex) {
					this.ex = ex;
					ex.printStackTrace();
				}
			}
			return instance;
		}
		
		@Override
		public Class<T> getInstanceClass() {
			return type;
		}
		
		public boolean isFutur() {
			return instance == null && ex == null;
		}
		
		@SuppressWarnings("unchecked")
		public void setInstance(T instance) {
			if (this.instance != null)
				throw new IllegalStateException("FutureInstance is allready created");
			this.instance = instance;
			this.type = (Class<T>) instance.getClass();
			if (this.callback != null)
				this.callback.action(instance);
		}

		public void onInstanceCreated(Action<T> action) {
			this.callback = action;
		}
		
	}
	
}
