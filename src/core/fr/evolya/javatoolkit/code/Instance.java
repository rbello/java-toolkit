package fr.evolya.javatoolkit.code;

import fr.evolya.javatoolkit.app.App;

public class Instance<T> {
	
	protected T instance = null;
	protected Class<T> clazz = null;
	
	@SuppressWarnings("unchecked")
	public <X extends T> Instance(X instance) {
		if (instance == null) {
			throw new NullPointerException();
		}
		if (instance instanceof Instance) {
			throw new IllegalArgumentException("Circular instance");
		}
		this.instance = instance;
		this.clazz = (Class<T>) instance.getClass();
	}
	
	protected Instance() {
	}
	
	public Class<?> getInstanceClass() {
		return clazz;
	}
	
	public Object getInstance() {
		return instance;
	}
	
	public boolean isFutur() {
		return false;
	}
	
//	public static class OptionalInstance extends Instance {
//		public OptionalInstance(Optional<?> instance) {
//			super(instance);
//			if (instance.isPresent()) this.clazz = instance.get().getClass();
//			else this.clazz = Void.class;
//		}
//	}

	public static class FuturInstance<T> extends Instance<T> {
		
		private Class<T> clazz;
		private Throwable ex = null;
		
		public FuturInstance() {
			super();
			this.clazz = null;
		}
		
		public FuturInstance(Class<T> clazz) {
			super();
			this.clazz = clazz;
		}
		
		@Override
		public synchronized T getInstance() {
			if (this.clazz == null) return null;
			if (isFutur()) {
				try {
					App.LOGGER.log(Logs.DEBUG_FINE, "Instantiate future object: "
							+ clazz.getSimpleName());
					instance = (T) clazz.newInstance();
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
			return clazz;
		}
		
		public boolean isFutur() {
			return instance == null && ex == null;
		}
		
		@SuppressWarnings("unchecked")
		public void setInstance(T instance) {
			if (this.instance != null)
				throw new IllegalStateException("FutureInstance is allready created");
			this.instance = instance;
			this.clazz = (Class<T>) instance.getClass();
		}
		
	}
	
}
