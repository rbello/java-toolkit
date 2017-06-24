package fr.evolya.javatoolkit.events.attr;

public interface EventCallback<L extends EventListener> {
	
	public boolean notifyEvent(EventSource<L> source, String eventName, Object... args)
			throws Exception;
	
	public String getMethodName(String eventName);
	
	public Object getTargetObject();

}
