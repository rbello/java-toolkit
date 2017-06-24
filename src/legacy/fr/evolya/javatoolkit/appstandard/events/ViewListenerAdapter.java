package fr.evolya.javatoolkit.appstandard.events;

import fr.evolya.javatoolkit.appstandard.AppView;

/**
 * @param <E> Le type d'event qui est propag� par le framework (AWT event pour swing)
 */
@SuppressWarnings("unchecked")
public class ViewListenerAdapter<E> implements ViewListener<E> {

	@Override
	public boolean onViewCloseIntent(AppView view, E event, boolean isUserIntent) {
		return true;
	}

	@Override
	public void onViewComponentHidden(AppView view, E event) {
		
	}

	@Override
	public void onViewComponentMoved(AppView view, E event) {
		
	}

	@Override
	public void onViewComponentResized(AppView view, E event) {
		
	}

	@Override
	public void onViewComponentShown(AppView view, E event) {		
	}

	@Override
	public void onViewFocusGained(AppView view, E event) {
	}

	@Override
	public void onViewFocusLost(AppView view, E event) {
	}


}
