package fr.evolya.javatoolkit.events.fi;

public abstract interface ModelEvent {
	
	@FunctionalInterface
	public static interface ModelItemAdded extends ModelEvent {
		public void onModelItemAdded(Model m, Object item, Object index);
	}
	
	@FunctionalInterface
	public static interface ModelItemModified extends ModelEvent {
		public void onModelItemAdded(Model m, Object item, Object index);
	}

}
