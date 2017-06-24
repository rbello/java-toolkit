package fr.evolya.javatoolkit.appstandard.events;

import fr.evolya.javatoolkit.appstandard.App;
import fr.evolya.javatoolkit.appstandard.AppService;
import fr.evolya.javatoolkit.appstandard.states.ApplicationState;
import fr.evolya.javatoolkit.code.annotations.ToOverride;

public class AppListenerAdapter implements AppListener {

	@Override
	@ToOverride
	public void afterApplicationStarted(App app) {
	}

	@Override
	@ToOverride
	public boolean beforeApplicationStarted(App app) {
		return true;
	}

	@Override
	@ToOverride
	public boolean beforeApplicationStopped(App app) {
		return true;
	}

	@Override
	@ToOverride
	public void onApplicationStarted(App app) {
	}

	@Override
	@ToOverride
	public void onApplicationStateChanged(App app, ApplicationState state) {
	}

	@Override
	@ToOverride
	public void onServiceStarted(App app, AppService service) {
	}

	@Override
	@ToOverride
	public void onServiceStopped(App app, AppService service) {
	}

}
