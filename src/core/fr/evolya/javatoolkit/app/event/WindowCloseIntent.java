package fr.evolya.javatoolkit.app.event;

@FunctionalInterface
public interface WindowCloseIntent {

	public void intentWindowClosing(Object view, Object sourceEvent);

}
