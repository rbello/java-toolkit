package fr.evolya.javatoolkit.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.evolya.javatoolkit.app.cdi.DependencyInjectionContext;
import fr.evolya.javatoolkit.app.cdi.Instance;
import fr.evolya.javatoolkit.app.cdi.Instance.FuturInstance;
import fr.evolya.javatoolkit.app.config.AppConfiguration;
import fr.evolya.javatoolkit.app.config.NonPersistentConfiguration;
import fr.evolya.javatoolkit.app.event.ApplicationBuilding;
import fr.evolya.javatoolkit.app.event.ApplicationReady;
import fr.evolya.javatoolkit.app.event.ApplicationStarted;
import fr.evolya.javatoolkit.app.event.ApplicationStarting;
import fr.evolya.javatoolkit.app.event.ApplicationStopped;
import fr.evolya.javatoolkit.app.event.ApplicationStopping;
import fr.evolya.javatoolkit.app.event.BeforeApplicationStarted;
import fr.evolya.javatoolkit.appstandard.bridge.ILocalApplication;
import fr.evolya.javatoolkit.appstandard.bridge.services.ILocalService;
import fr.evolya.javatoolkit.code.Logs;
import fr.evolya.javatoolkit.events.fi.Listener;
import fr.evolya.javatoolkit.events.fi.Observable;
import fr.evolya.javatoolkit.gui.swing.SwingHelper;

@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class App extends Observable
	implements ILocalApplication {
	
	public static final Logger LOGGER = Logs.getLogger("App (v2)");
	
	private Class<?> _state = ApplicationStopped.class;
	
	private DependencyInjectionContext cdi;

	private static App INSTANCE = null;
	
	public App() {
		
		// Create helper accesssor
		INSTANCE  = this;
		
		// Create CDI context
		cdi = new DependencyInjectionContext((task) -> {
			try {
				this.invokeAndWaitOnGuiDispatchThread(task);
			}
			catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		
		// Register this class into CDI
		cdi.register(App.class, new Instance<>(this));
		
		// Add configuration component
		AppConfiguration conf = new NonPersistentConfiguration();
		conf.setProperty("App.Name", "NoName");
		conf.setProperty("App.Version", "0.0");
		add(AppConfiguration.class, new Instance(conf));
		
		// Intercept SIGINT signal
		Runtime.getRuntime().addShutdownHook(new Thread() {
		    public void run() {
		    	// TODO
		    }
		 });
		
	}
	
	public App add(Class<?> instance) {
		return add(new FuturInstance(instance));
	}
	
	public <T> T add(T instance) {
		add(new Instance(instance));
		return instance;
	}
	
	public App add(Instance instance) {
		Class<?> type = instance.getInstanceClass();
		if (type == null) throw new NullPointerException("Invalid pure futur object");
		return add(type, instance);
	}
	
	protected App add(Class<?> type, Instance instance) {
		
		// Log
		if (LOGGER.isLoggable(Logs.INFO)) {
			LOGGER.log(Logs.INFO, String.format(
					"Add component %s (%s)",
					type.getSimpleName(),
					instance.isFutur() ? "futur" : "instance"
					));
		}
		
		// Register object in CDI context
		cdi.register(type, instance);

		// Search for events annotations
		addListener(instance);
		
		// Notify the event
		notify(instance, BeforeApplicationStarted.class, this);
		
		return this;
	}

	public <T> T get(Class<T> type) {
		return cdi.getInstance(type);
	}

	public Map<Class<?>, Instance<?>> getComponents() {
		return cdi.getComponents();
	}
	
	public String getState() {
		return _state.getSimpleName();
	}
	
	public synchronized void start() {
		
		// Check current state
		if (_state != ApplicationStopped.class)
			throw new IllegalStateException("App is already started");
		
		// Add events that the observable have to repeat to new listeners
		repeatForNewListeners(
				ApplicationStarting.class,
				ApplicationBuilding.class,
				ApplicationStarted.class,
				ApplicationReady.class
				);
		
		// Build CDI cache
		cdi.build();
		setState(ApplicationBuilding.class);
		
		// Change states
		setState(ApplicationStarting.class);
		setState(ApplicationStarted.class);
		setState(ApplicationReady.class);

	}

	@Override
	public void stop() {
		if (_state == ApplicationStopped.class)
			throw new IllegalStateException("App is already stopped");
		setState(ApplicationStopping.class);
		setState(ApplicationStopped.class);
		System.exit(0);
	}

	@Override
	public void interrupt() {
		LOGGER.log(Logs.INFO, "Application was interrupted");
		System.exit(-1);
	}
	
	protected void setState(Class<?> state) {
		_state = state;
		LOGGER.log(Logs.INFO, "Application state changed: " + getState());
		notify(state, this);
	}
	
	public boolean isActive() {
		return _state != ApplicationStopped.class && _state != ApplicationStopping.class;
	}
	
	public static int init() {
		return init(new String[0]);
	}
	
	public static int init(String[] args) {
		
		// Debug mode
		int debugMode = 0;
		Level logLevel = Logs.INFO;
		if (args != null && args.length > 0) {
			if (args[0].toLowerCase().equals("debug=1")) {
				debugMode = 1;
				logLevel = Logs.DEBUG;
			}
			if (args[0].toLowerCase().equals("debug=2")) {
				debugMode = 2;
				logLevel = Logs.ALL;
			}
		}
				
		// Ajustement du niveau de log
		Logs.setDefaultLevel(logLevel);
		Logs.setGlobalLevel(logLevel);
		
    	// Initialisations pour Swing
    	SwingHelper.initLookAndFeel();
    	SwingHelper.initSwingAnimations();
    	SwingHelper.adjustGlobalFontSize(13);
    	
    	return debugMode;
	}
	
	@Override
	public String toString() {
		return get(AppConfiguration.class).toString("%s v%s", "App.Name", "App.Version");
	}

	public void setLogLevel(Level level) {
		Logs.setDefaultLevel(level);
		Logs.setGlobalLevel(level);
	}
	
	@Override
	public AppActivity getInvoker() {
		return null;
	}
	
	@Override
	public String getApplicationID() {
		return getApplicationName();
	}

	@Override
	public String getApplicationName() {
		return get(AppConfiguration.class).getProperty("App.Name");
	}

	@Override
	public ILocalService[] getPublishedServices() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isStarted() {
		return "Started".equals(_state);
	}

	public App remove(Object object) {
		return remove(object.getClass());
	}
	
	public App remove(Class<?> clazz) {
		cdi.unregister(clazz);
		// removeListener(clazz); TODO
		return this;
	}

	public List<Listener<?>> getListeners(Class<?> clazz) {
		List<Listener<?>> output = new ArrayList<Listener<?>>();
		for (Listener<?> listener : getListeners()) {
			if (listener.getTarget().getClass() == clazz)
				output.add(listener);
		}
		return output;
	}

	@Override
	public List<Listener<?>> getListeners() {
		return super.getListeners();
	}

	public void inject(Class<?> targetType, String attributeName, Class<?> typeToInject) {
		cdi.inject(targetType, attributeName, typeToInject);
	}

	public static App instance() {
		return INSTANCE;
	}

	public void magic(Object object) {
		cdi.searchInjections(object);
		addListener(new Instance(object));
	}
	
}
