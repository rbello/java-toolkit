package fr.evolya.javatoolkit.appstandard.events;

import fr.evolya.javatoolkit.appstandard.AppView;
import fr.evolya.javatoolkit.appstandard.AppViewController;
import fr.evolya.javatoolkit.events.attr.EventListener;

@SuppressWarnings("unchecked")
public interface ViewControllerListener extends EventListener {
	
	public void onViewCreated(AppViewController ctrl, AppView view);
	
	public boolean beforeViewCloseIntent(AppViewController ctrl, boolean isUserIntent);
	
	public void afterViewCloseIntent(AppViewController ctrl, boolean isUserIntent);

}
