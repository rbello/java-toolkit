package fr.evolya.javatoolkit.appstandard.bridge;

import fr.evolya.javatoolkit.appstandard.bridge.services.ILocalService;
import fr.evolya.javatoolkit.appstandard.bridge.services.IRemoteService;
import fr.evolya.javatoolkit.events.attr.EventSource;

public interface IBridge {

	public EventSource<IBridgeListener> onChanges();
	
	public void onChanges(IBridgeListener listener);
	
	public String getBridgeName();
	
	public Iterable<ILocalService> getLocalServices();
	
	public Iterable<IRemoteService> getRemoteServices();
	
}
