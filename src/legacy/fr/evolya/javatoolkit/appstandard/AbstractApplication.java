package fr.evolya.javatoolkit.appstandard;

import fr.evolya.javatoolkit.appstandard.states.ApplicationState;
import fr.evolya.javatoolkit.appstandard.states.PausedState;
import fr.evolya.javatoolkit.appstandard.states.StartedState;
import fr.evolya.javatoolkit.appstandard.states.StoppedState;
import fr.evolya.javatoolkit.code.annotations.ToOverride;

@Deprecated
public abstract class AbstractApplication extends App {

	public AbstractApplication() {
		super("MainApplication", "1.0", true);
	}
	
	public AbstractApplication(boolean isMainApplication) {
		super("MainApplication", "1.0", isMainApplication);
	}
	
	public AbstractApplication(String appName, boolean isMainApplication) {
		super(appName, "1.0", isMainApplication);
	}
	
	public AbstractApplication(String appName, String appVersion, boolean isMainApplication) {
		super(appName, appVersion, isMainApplication);
	}
	
	@Override
	@ToOverride
	protected void onStart() {
	}

	@Override
	@ToOverride
	protected void onSleep() {
	}

	@Override
	@ToOverride
	protected void onStop() {
	}

	@Override
	@ToOverride
	protected void onWakeup() {
	}
	
	@Override
	@ToOverride
	protected PausedState getPausedState() {
		return ApplicationState.getDefaultPausedState();
	}

	@Override
	@ToOverride
	protected StartedState getStartedState() {
		return ApplicationState.getDefaultStartedState();
	}

	@Override
	@ToOverride
	protected StoppedState getStoppedState() {
		return ApplicationState.getDefaultStoppedState();
	}

}
