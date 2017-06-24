package fr.evolya.javatoolkit.appstandard.events;

import fr.evolya.javatoolkit.appstandard.AppView;
import fr.evolya.javatoolkit.appstandard.AppViewController;

@SuppressWarnings("unchecked")
public class ViewControllerListenerAdapter implements ViewControllerListener {

	@Override
	public void afterViewCloseIntent(AppViewController ctrl, boolean isUserIntent) {
		
	}

	@Override
	public boolean beforeViewCloseIntent(AppViewController ctrl, boolean isUserIntent) {
		return true;
	}

	@Override
	public void onViewCreated(AppViewController ctrl, AppView view) {
		
	}
	
}
