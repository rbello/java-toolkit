package fr.evolya.javatoolkit.events.attr;


public interface SourceListener extends EventListener {
	
	public void onBind(EventCallback<?> listener, EventSource<?> source);
	
	public void onUnbind(EventCallback<?> listener, EventSource<?> source);
	
	public void onUnbindAll(EventSource<?> source);

}
