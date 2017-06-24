package fr.evolya.javatoolkit.appstandard.events;

import fr.evolya.javatoolkit.app.config.AppConfiguration;
import fr.evolya.javatoolkit.app.config.Configurable;

public abstract class ConfigListenerAdapter implements ConfigListener {

	@Override
	public void onConfigurationReady(AppConfiguration config, Configurable target) {
		
	}

	@Override
	public void onConfigurationRestored(AppConfiguration config, Configurable target,
			String filename, boolean success) {
		
	}

	@Override
	public void afterConfigurationSaved(AppConfiguration config, Configurable target,
			String filename) {
		
	}

	@Override
	public boolean beforeConfigurationSaved(AppConfiguration config,
			Configurable target, String filename) {
		return true;
	}

}
