package fr.evolya.javatoolkit.appstandard.events;

import fr.evolya.javatoolkit.appstandard.AppView;

/**
 * @param <E> Le type d'event qui est propag� par le framework (AWT event pour swing)
 */
@SuppressWarnings("unchecked")
public interface WindowViewListener<E> extends ContainerViewListener<E> {

	// WindowListener
	
	public void onViewActivated(AppView view, E event);
	public void onViewClosed(AppView view, E event);
	public void onViewClosing(AppView view, E event);
	public void onViewDeactivated(AppView view, E event);
	public void onViewIconified(AppView view, E event);
	public void onViewDeiconified(AppView view, E event);
	public void onViewOpened(AppView view, E event);
	
	// WindowStateListener
	
	public void onViewStateChanged(AppView view, E event);

}
