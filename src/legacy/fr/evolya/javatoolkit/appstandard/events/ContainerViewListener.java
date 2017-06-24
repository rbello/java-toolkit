package fr.evolya.javatoolkit.appstandard.events;

import fr.evolya.javatoolkit.appstandard.AppView;

/**
 * @param <E> Le type d'event qui est propagé par le framework (AWT event pour swing)
 */
@SuppressWarnings("unchecked")
public interface ContainerViewListener<E> extends ViewListener<E> {

	// ContainerListener
	
	public void onViewComponentAdded(AppView view, E event);
	public void onViewComponentRemoved(AppView view, E event);

}
