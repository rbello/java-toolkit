package fr.evolya.javatoolkit.appstandard.bridge;

import fr.evolya.javatoolkit.app.AppActivity;
import fr.evolya.javatoolkit.appstandard.bridge.services.ILocalService;

public interface ILocalApplication extends AppActivity {

	public String getApplicationID();
	
	public String getApplicationName();
	
	@Deprecated
	public ILocalService[] getPublishedServices();
	
}
