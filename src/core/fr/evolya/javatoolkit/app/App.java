package fr.evolya.javatoolkit.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.evolya.javatoolkit.app.config.AppConfiguration;
import fr.evolya.javatoolkit.app.config.NonPersistentConfiguration;
import fr.evolya.javatoolkit.app.event.ApplicationBuilding;
import fr.evolya.javatoolkit.app.event.ApplicationReady;
import fr.evolya.javatoolkit.app.event.ApplicationStarted;
import fr.evolya.javatoolkit.app.event.ApplicationStarting;
import fr.evolya.javatoolkit.appstandard.bridge.ILocalApplication;
import fr.evolya.javatoolkit.appstandard.bridge.services.ILocalService;
import fr.evolya.javatoolkit.code.Logs;
import fr.evolya.javatoolkit.code.Instance;
import fr.evolya.javatoolkit.code.Instance.FuturInstance;
import fr.evolya.javatoolkit.events.fi.Listener;
import fr.evolya.javatoolkit.events.fi.Observable;
import fr.evolya.javatoolkit.gui.swing.SwingHelper;

@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class App extends Observable
	implements ILocalApplication {
	
	public static final Logger LOGGER = Logs.getLogger("App (v2)");
	
	private Map<Class<?>, Instance> components = new HashMap<>();
	
	private String _state = "Stopped";
	
	public App() {
		AppConfiguration conf = new NonPersistentConfiguration();
		conf.setProperty("App.Name", "NoName");
		conf.setProperty("App.Version", "0.0");
		add(AppConfiguration.class, conf);
	}
	
	public App add(Class<?> instance) {
		return add(new FuturInstance(instance));
	}
	
	public <T> T add(T instance) {
		add(new Instance(instance));
		return instance;
	}
	
	public App add(Instance instance) {
		return add(instance.getInstanceClass(), instance);
	}
	
	public App add(Class<?> clazz, Object instance) {
		return add(clazz, new Instance(instance));
	}
	
	public App add(Class<?> clazz, Instance instance) {
		if (components.containsKey(clazz)) {
			throw new IllegalAccessError("Component " + clazz.getSimpleName() + " allready exists");
		}
		components.put(clazz, instance);
		if (LOGGER.isLoggable(Logs.DEBUG)) {
			LOGGER.log(Logs.DEBUG, String.format(
					"Add component %s to app %s (%s)",
					clazz.getSimpleName(),
					get(AppConfiguration.class).getProperty("App.Name"),
					instance.isFutur() ? "futur" : "instance"
					));
		}
		addListener(instance);
		return this;
	}

	public <T> T get(Class<T> clazz) {
		if (!components.containsKey(clazz)) return null;
//		T t = (T) components.get(clazz);
//		if (t == null) {
//			try {
//				t = clazz.newInstance();
//				System.out.println("Instantiation de " + clazz.getSimpleName());
//				components.put(clazz, t);
//			} catch (Throwable e) {
//				System.err.println("Unable to instantiate " + clazz.getSimpleName());
//			}
//		}
		return (T) components.get(clazz).getInstance();
	}

	public Map<Class<?>, Instance> getComponents() {
		return components;
	}
	

//	public App bind(Object listener) {
//		if (listener != null) listeners.add(listener);
//		return this;
//	}
//	
//	public void bind(Class<?> class1, Runnable object) {
//		
//	}
	
	public String getState() {
		return _state ;
	}
	
	public synchronized void start() {
		
		if (!"Stopped".equals(_state)) throw new IllegalStateException("App cannot be started");
		
		repeatForNewListeners(
				ApplicationStarting.class,
				ApplicationBuilding.class,
				ApplicationStarted.class,
				ApplicationReady.class
				);
		
		_state = "Starting";
		notify(ApplicationStarting.class, this);
		
		notify(ApplicationBuilding.class, this);
		
		_state = "Started";
		notify(ApplicationStarted.class, this);
		
		notify(ApplicationReady.class, this);
		
		LOGGER.log(Logs.INFO, "Application is ready !");
		
	}
	
	public static boolean init() {
		return init(new String[0]);
	}
	
	public static boolean init(String[] args) {
		
		// Debug mode
		final boolean debugMode = args != null && args.length > 0 &&
				args[0].toLowerCase().equals("debug=1");
		
		// Ajustement du niveau de log
		Logs.setDefaultLevel(debugMode ? Logs.INFO : Logs.NONE);
		Logs.setGlobalLevel(debugMode ? Logs.INFO : Logs.NONE);
		
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

	public <T> T get(String id, Class<T> classe) {
		if (!components.containsKey(id)) return null;
		return (T) components.get(id).getInstance();
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getApplicationName() {
		return get(AppConfiguration.class).getProperty("App.Name");
	}

	@Override
	public ILocalService[] getPublishedServices() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void interrupt() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isStarted() {
		return "Started".equals(_state);
	}

	public App remove(Object object) {
		return remove(object.getClass());
	}
	
	public App remove(Class<?> clazz) {
		if (!components.containsKey(clazz)) return this;
		components.remove(clazz);
		// TODO Detach events !
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
	
}
