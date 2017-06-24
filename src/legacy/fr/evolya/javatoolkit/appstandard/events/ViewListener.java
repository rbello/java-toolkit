package fr.evolya.javatoolkit.appstandard.events;

import fr.evolya.javatoolkit.appstandard.AppView;
import fr.evolya.javatoolkit.events.attr.EventListener;

/**
 * @param <E> Le type d'event qui est propag� par le framework (AWT event pour swing)
 */
@SuppressWarnings("unchecked")
public interface ViewListener<E> extends EventListener {
	
	// Intents
	
	public boolean onViewCloseIntent(AppView view, E event, boolean isUserIntent);
	
	// ComponentListener
	
	public void onViewComponentHidden(AppView view, E event);
	public void onViewComponentMoved(AppView view, E event);
	public void onViewComponentResized(AppView view, E event);
	public void onViewComponentShown(AppView view, E event);

	// FocusListener
	
	public void onViewFocusGained(AppView view, E event);
	public void onViewFocusLost(AppView view, E event);

}
