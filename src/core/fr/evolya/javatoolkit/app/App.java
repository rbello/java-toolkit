package fr.evolya.javatoolkit.app;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
import fr.evolya.javatoolkit.code.annotations.Bug;
import fr.evolya.javatoolkit.code.annotations.ConfigDeclare;
import fr.evolya.javatoolkit.code.annotations.ToOverride;
import fr.evolya.javatoolkit.code.funcint.Action;
import fr.evolya.javatoolkit.code.utils.ReflectionUtils;
import fr.evolya.javatoolkit.code.utils.Utils;
import fr.evolya.javatoolkit.events.fi.Listener;
import fr.evolya.javatoolkit.events.fi.Observable;

@SuppressWarnings({"rawtypes", "unchecked"})
public class App extends Observable
	implements ILocalApplication {
	
	public static final Logger LOGGER = Logs.getLogger("App");
	
	private static App INSTANCE = null;

	private static Class<?> MAIN_CLASS = null;
	
	protected final DependencyInjectionContext cdi;

	protected final int debugLevel;

	private Class<?> state = ApplicationStopped.class;

	private Map<Class<? extends Annotation>, Action> annotations = new HashMap<>();
	
	private AppActivity invoker = null;
	
	public App() {
		this(null, new String[0]);
	}
	
	public App(AppActivity invoker) {
		this(invoker, new String[0]);
	}
	
	public App(String[] args) {
		this(null, args);
	}
	
	/**
	 * Construct application giving application command line arguments.
	 * Eg.
	 * 		public static void main(String[] args) {
	 * 			App app = new App(args);
	 * 		}
	 */
	public App(AppActivity invoker, String[] args) {
		
		// Ensure that current application was running into main thread
		checkMainClass();

		// Create helper accessor
		INSTANCE = this;
		
		// Save invoker
		this.invoker = invoker;
		
		// Init debug level
		this.debugLevel = initDebugLevel(args);
		
		// Create CDI context
		this.cdi = new DependencyInjectionContext((task) -> {
			try {
				this.invokeAndWaitOnGuiDispatchThread(task);
			}
			catch (InterruptedException ex) {
				// Catch, restore and terminate
				Thread.currentThread().interrupt();
				return;
			}
		});
		
		// Log
		if (LOGGER.isLoggable(Logs.DEBUG)) {
			LOGGER.log(Logs.DEBUG, "Execution path: " + App.getExecutionDirectory());
			LOGGER.log(Logs.DEBUG, "Library path  : " + System.getProperty("java.library.path"));
			LOGGER.log(Logs.DEBUG, "Binaries path : " + App.getBinaryDirectory());
		}
		LOGGER.log(Logs.INFO, "");
		LOGGER.log(Logs.INFO, "APPLICATION CREATION");
		
		// Add configuration component
		AppConfiguration conf = new NonPersistentConfiguration();
		conf.setProperty("App.Name", "NoName");
		conf.setProperty("App.Version", "0.0");
		add(AppConfiguration.class, new Instance(conf));
		
		// Register this class into CDI & events
		add(App.class, new Instance<>(this));
		
		// Intercept SIGINT signal
		Runtime.getRuntime().addShutdownHook(new Thread() {
		    public void run() {
		    	// The application wasn't closed properly
		    	if (App.this.state != ApplicationStopped.class) {
		    		LOGGER.log(Logs.ERROR, "APPLICATION WAS INTERRUPTED IMPROPERLY");
		    	}
		    }
		 });
		
	}
	
	private static void checkMainClass() {
		if (Thread.currentThread().getId() != 1) {
			throw new RuntimeException("ERROR: APP MUST BE CREATED INTO THE MAIN THREAD");
		}
		StackTraceElement first = Utils.first(Thread.currentThread().getStackTrace());
		try {
			 MAIN_CLASS  = Class.forName(first.getClassName());
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Returns the last instance of App created. If only one App is created in your
	 * application contexte, this method is like a static Singleton accessor.
	 */
	public static App instance() {
		return INSTANCE;
	}
	
	public App add(String className) {
		try {
			return add(Class.forName(className));
		}
		catch (ClassNotFoundException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public App add(Class<?> instance) {
		return add(new FuturInstance(instance));
	}
	
	public App add(Object object) {
		add(new Instance(object));
		return this;
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
		
		add(instance, type, true, true);
		
		// Notify the event
		notify(instance, BeforeApplicationStarted.class, this);

		return this;
	}

	/**
	 * 
	 * @param instance
	 * @param registerId (Optional)
	 * @param searchConfigDeclarations
	 * @param bindEvents
	 */
	protected <T> void add(Instance<T> instance, Class<T> registerId, boolean searchConfigDeclarations, boolean bindEvents) {

		// Register object in CDI context
		if (registerId != null) cdi.register(registerId, instance);
		// Or just search for dependencies injections 
		else cdi.searchInjections(instance);

		// Search for config declarations
		if (searchConfigDeclarations) searchConfigDeclarations(instance);

		// Search for events binding annotations
		if (bindEvents) addListener(instance);
		
		// Search for specific annotations
		searchAnnotations(instance);
		
		// TODO
		// Auto-build
		//if (instance.isFutur() && (state == ApplicationStarted.class || state == ApplicationReady.class)) {
		//	System.out.println("Create " + instance);
		//	//instance.getInstance();
		//	cdi.build();
		//}
		
	}

	protected void searchConfigDeclarations(Instance<?> instance) {
		
		Arrays
			.asList(instance.getInstanceClass().getDeclaredAnnotationsByType(ConfigDeclare.class))
			.forEach((a) -> {
				String key = a.value();
				if (key.contains("=")) {
					String[] keyvalue = key.split("=", 2);
					get(AppConfiguration.class).setPropertyIfUndefined(keyvalue[0], keyvalue[1]);
					if (LOGGER.isLoggable(Logs.DEBUG_FINE)) {
						LOGGER.log(Logs.DEBUG_FINE, "  `-> Found config declaration '" + keyvalue[0] + "' = " + keyvalue[1] + " in " 
							+ instance.getInstanceClass().getSimpleName());
					}
				}
				else {
					get(AppConfiguration.class).setPropertyIfUndefined(key);
					if (LOGGER.isLoggable(Logs.DEBUG_FINE)) {
						LOGGER.log(Logs.DEBUG_FINE, "  `-> Found config declaration '" + key + "' in " 
							+ instance.getInstanceClass().getSimpleName());
					}
				}
			});
		
	}

	public <T> T get(Class<T> type) {
		return cdi.getInstance(type);
	}

	public Map<Class<?>, Instance<?>> getComponents() {
		return cdi.getComponentsMap();
	}
	
	/**
	 * Inject the type `typeToInject` into the component identified by type `targetType`,
	 * into the attribute `attributeName`.
	 */
	@Deprecated
	@Bug
	public void inject(Class<?> targetType, String attributeName, Class<?> typeToInject) {
		// TODO BUG Note : this method will throw an IllegalArgumentException because now this method
		// has to receive a real object as first argument, not a class
		cdi.inject(targetType, attributeName, typeToInject);
	}

	/**
	 * Apply some magic behavior to an object:
	 * 	- Search for @Inject annotations
	 *  - Search for @BindOnEvent or @GuiTask annotations
	 *  
	 *  This method is useful to initialize an object with dependency
	 *  injection and event binding without creating a component.
	 *  
	 *  Use this method with caution : invoking it during another
	 *  invokation can generate an interlock issue.
	 */
	@Deprecated
	public void magic(Object object) {
		LOGGER.log(Logs.DEBUG_FINE, "Apply magic to: " + object.getClass());
		cdi.searchInjections(object);
		addListener(new Instance(object));
	}
	
	public String getState() {
		return state.getSimpleName();
	}
	
	public synchronized void start() {
		
		try {
		
			// Check current state
			if (state != ApplicationStopped.class)
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
		catch (Throwable t) {
			LOGGER.log(Logs.ERROR, "Unable to start application: " + t.getClass().getSimpleName()
					+ " - " + t.getMessage(), t);
		}

	}

	@Override
	public void stop() {
		if (state == ApplicationStopped.class)
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
		this.state = state;
		LOGGER.log(Logs.INFO, "");
		LOGGER.log(Logs.INFO, "APPLICATION STATE CHANGED: " + getState());
		notify(state, this);
	}
	
	@Override
	public AppActivity getInvoker() {
		return invoker ;
	}
	
	@Override
	@ToOverride
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
		return state == ApplicationStarted.class;
	}
	
	public boolean isActive() {
		return state != ApplicationStopped.class && state != ApplicationStopping.class;
	}

	public boolean remove(Object object) {
		return remove(object.getClass());
	}
	
	public boolean remove(Class<?> type) {
		Instance<?> c = cdi.getComponent(type);
		if (c == null) return false;
		if (!c.isFutur()) {
			removeListener(c.getInstance());
		}
		cdi.unregister(type);
		return true;
	}

	/**
	 * Returns a list filled with listeners bound to given class.  
	 */
	public List<Listener<?>> getListeners(Class<?> type) {
		List<Listener<?>> output = new ArrayList<>();
		for (Listener<?> listener : getListeners()) {
			if (listener.getTarget().getClass() == type)
				output.add(listener);
		}
		return output;
	}

	public int getDebugLevel() {
		return debugLevel;
	}
	
	public void setLogLevel(Level level) {
		Logs.setGlobalLevel(level);
	}

	protected int initDebugLevel(String[] args) {
		
		// Debug mode
		int debugMode = 0;
		Level logLevel = Logs.INFO;
		
		// Arguments handling
		if (args != null && args.length > 0) {
			for (String arg : args) {
				if (!arg.startsWith("--debug=")) continue;
				try {
					debugMode = new Integer(arg.substring(8));
					break;
				}
				catch (Throwable t) {
					LOGGER.log(Logs.WARNING, "Invalid debug argument");
				}
			}
		}

		// Ajustement du niveau de log
		if (debugMode > 0) {
			if (debugMode >= 2) logLevel = Logs.DEBUG_FINE;
			else if (debugMode >= 1) logLevel = Logs.DEBUG;
			LOGGER.log(Logs.INFO, "Debug mode: " + debugMode + " (" + logLevel + ")");
		}

		setLogLevel(logLevel);
		
    	return debugMode;
	}
	
	protected Action<?> method(String methodName) {
		Method m = ReflectionUtils.getMethodMatching(getClass(), methodName);
		if (m == null) throw new NullPointerException("Method not found: " + 
				getClass().getSimpleName() + "::" + methodName + "()");
		return (instance) -> {
			try {
				m.invoke(this, instance);
			}
			catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		};
	}
	
	protected void addAnnotation(Class<? extends Annotation> annotation, Action<?> method) {
		if (!annotation.isAnnotation()) throw new IllegalArgumentException();
		if (method == null) throw new NullPointerException();
		this.annotations.put(annotation, method);
	}
	
	private void searchAnnotations(Instance<?> instance) {
		// Fetch annotations
		this.annotations.forEach((annotation, handler) -> {
			
			if (!instance.getInstanceClass().isAnnotationPresent(annotation))
				return;
			
			if (LOGGER.isLoggable(Logs.DEBUG)) {
				LOGGER.log(Logs.DEBUG, String.format(" `--> Register annotation '@%s' on object '%s'",
						annotation.getSimpleName(), instance.getInstanceClass().getSimpleName()));
			}
			
			if (instance.isFutur()) {
				((FuturInstance<?>)instance).onInstanceCreated(object -> {
					handler.call(instance);
				});
			}
			else {
				handler.call(instance);
			}
		});
	}

	public static boolean setLogFileOutput(String path, Level loglevel) {
		try {
			Logs.addFileOutputHandler(path, loglevel);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Return path to the current execution context.
	 */
	public static File getExecutionDirectory() {
		return new File(System.getProperty("user.dir"));
	}
	
	/**
	 * Return path to the directory containing the current executed jar 
	 */
	public static File getBinaryDirectory() {
		try {
			Class<?> type = MAIN_CLASS == null ? App.class : MAIN_CLASS;
			return new File(type.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile();
		}
		catch (Throwable ex) {
			return null;
		}
	}
	
	/**
	 * Return path to the directory containing `java-toolkit-X.X.X.jar` 
	 */
	public static File getToolkitDirectory() {
		try {
			return new File(App.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile();
		}
		catch (Throwable ex) {
			return null;
		}
	}
	
	@Override
	public String toString() {
		try {
			return get(AppConfiguration.class).toString("%s v%s", "App.Name", "App.Version");
		}
		catch (Exception ex) {
			return "App";
		}
	}
	
}
