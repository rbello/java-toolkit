package fr.evolya.javatoolkit.appstandard;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import fr.evolya.javatoolkit.app.AppActivity;
import fr.evolya.javatoolkit.appstandard.bridge.ILocalApplication;
import fr.evolya.javatoolkit.appstandard.bridge.services.ILocalService;
import fr.evolya.javatoolkit.appstandard.events.AppListener;
import fr.evolya.javatoolkit.appstandard.states.ApplicationState;
import fr.evolya.javatoolkit.appstandard.states.PausedState;
import fr.evolya.javatoolkit.appstandard.states.StartedState;
import fr.evolya.javatoolkit.appstandard.states.StoppedState;
import fr.evolya.javatoolkit.code.IncaLogger;
import fr.evolya.javatoolkit.code.Util;
import fr.evolya.javatoolkit.code.annotations.DesignPattern;
import fr.evolya.javatoolkit.code.annotations.Pattern;
import fr.evolya.javatoolkit.events.attr.EventSource;
import fr.evolya.javatoolkit.exceptions.AllreadyStartedException;
import fr.evolya.javatoolkit.exceptions.AllreadyStoppedException;
import fr.evolya.javatoolkit.exceptions.StateChangeException;
import fr.evolya.javatoolkit.gui.swing.SwingHelper;
import fr.evolya.javatoolkit.gui.swing.dialogs.ErrorDialog;

/**
 * Une application, avec gestion des plugins, des services et des événements.
 */
@DesignPattern(type = Pattern.DynamicSingleton)
@Deprecated
public abstract class App implements ILocalApplication {

	/**
	 * Logger des applications.
	 */
	public static final Logger LOGGER = IncaLogger.getLogger("Application");
	
	/**
	 * Sauvegarde de la dernière instance d'application créee.
	 * Dans une logique de singleton, cette méthode permet de récupérer simplement
	 * l'instance de l'application en cours.
	 */
	private static App INSTANCE = null;
	
	/**
	 * Nom de l'application.
	 */
	private String _appName;

	/**
	 * Version de l'application.
	 */
	private String _appVersion;

	/**
	 * Bus pour les events d'applications.
	 */
	private EventSource<? extends AppListener> _eventsApp =
			new EventSource<AppListener>(AppListener.class, this);
	
	/**
	 * La liste des plugins.
	 * La string représente l'identifiant du plugin.
	 */
	private Map<String, AppPlugin> _plugins = new HashMap<String, AppPlugin>();

	/**
	 * L'état actuel de l'application
	 */
	private ApplicationState _state = ApplicationState.getDefaultStoppedState();
	
	/**
	 * Indique s'il s'agit de l'application principale. Si on la ferme,
	 * on ferme toute la JVM.
	 */
	private boolean _isMainApplication;

	/**
	 * L'activité qui a lancé cette application, ou NULL si le lancement
	 * de l'application a été fait dans à partir du contexte main.
	 */
	private AppActivity _invoker;
	
	/**
	 * Constructeur par défaut.
	 */
	public App(String appName, String appVersion, boolean isMainApplication) {
		
		// On enregistre les infos d'application
		_appName = appName.toString();
		_appVersion = appVersion.toString();
		_isMainApplication = isMainApplication;
		
		// Et on sauvegarde l'instance.
		INSTANCE = this;
		
	}
	
	/**
	 * Constructeur vide, pour l'IOC.
	 */
	public App() {
		this("MainApplication", "1.0", true);
	}

	/**
	 * Renvoie le nom de l'application.
	 * 
	 * @return Le nom de l'application
	 */
	public final String getApplicationName() {
		return _appName;
	}

	/**
	 * Renvoie la version actuelle de l'application.
	 * @return La version actuelle de l'application
	 */
	public final String getApplicationVersion() {
		return _appVersion;
	}
	
	@Override
	public final String getApplicationID() {
		return super.toString();
	}
	
	@Override
	public final AppActivity getInvoker() {
		return _invoker;
	}
	
	/**
	 * Renvoie le bus pour les events d'application.
	 */
	public EventSource<? extends AppListener> getEventsApp() {
		return _eventsApp;
	}
		
	/**
	 * Lancement de l'application.
	 * 
	 * @throws StateChangeException
	 * @throws SecurityException
	 */
	@Override
	public final synchronized void start() throws StateChangeException, SecurityException {
		
		// Allready started
		if (isStarted()) {
			throw new AllreadyStartedException();
		}
		
		try {
		
			// Event before
			if (!_eventsApp.dispatch("beforeApplicationStarted", this)) {
				throw new SecurityException("Application start halted");
			}
			
			// Log
			if (LOGGER.isLoggable(IncaLogger.INFO)) {
				LOGGER.log(IncaLogger.INFO, "Application " + _appName + " is starting");
			}
			
			// On enregistre l'activité parente
			_invoker = getParentInvoker();
			
			// Lancement interne
			onStart();
			
			// On change l'état de l'application
			setState(getStartedState(), true);
			
			// Event inside
			_eventsApp.dispatch("onApplicationStarted", this);
			
			// Log
			if (LOGGER.isLoggable(IncaLogger.INFO)) {
				LOGGER.log(IncaLogger.INFO, "Application " + _appName + " is started");
			}
			
			// Event after
			_eventsApp.dispatch("afterApplicationStarted", this);
		
		}
		
		catch (Throwable ex) {
			
			// Pour l'application principale, 
			if (_isMainApplication) {
				ErrorDialog.showDialog(ex, "Unable to start " + _appName,
						"The application has encountered a very big problem," +
						"and is unable to start. We are sorry for the inconvenience.",
						new Runnable() {
							public void run() {
								System.exit(-1);								
							}
						});
			}
			
			// On repropage l'exception
			throw new RuntimeException(ex);
			
		}
		
	}
	
	/**
	 * Modifier l'état actuel de l'application.
	 */
	protected final void setState(ApplicationState state, boolean raiseEvents) {
		
		// Vérification
		if (state == null) {
			throw new NullPointerException("Application state is NULL");
		}
		
		// Ce boolean sert à indiquer si les deux états sont équivalents
		boolean equals = false;
		
		synchronized (_state) {
			
			// Aucun changement si c'est exactement la même instance
			if (state == _state) {
				return;
			}
			
			// Comparaison des deux états
			// On fait appel au equals() qui compare deux ApplicationState
			equals = _state.equals(state);
		
			// On enregistre l'état
			_state = state;
			
		}
		
		// Si les deux états ne sont pas du même "type" on a eu un changement
		if (!equals && !raiseEvents) {
			// On lance un event
			_eventsApp.trigger("onApplicationStateChanged", this, state);
		}
		
	}
	
	/**
	 * Renvoie l'état actuel de l'application.
	 */
	public final ApplicationState getState() {
		synchronized (_state) {
			return _state;
		}
	}

	/**
	 * A implémenter par les applications, lors 
	 */
	protected abstract void onStart();
	
	protected abstract void onStop();
	
	protected abstract void onSleep();
	
	protected abstract void onWakeup();
	
	/**
	 * Pause l'execution de l'application.
	 * 
	 */
	public final synchronized void sleep() throws StateChangeException {
		// TODO throw StateChangeException
		setState(getPausedState(), true);
		onSleep();
		// TODO Propagation aux plugins ? Event ?
	}
	
	/**
	 * Reprend l'execution de l'application.
	 * 
	 */
	public final synchronized void wakeup() throws StateChangeException {
		// TODO throw StateChangeException
		setState(getStartedState(), true);
		onWakeup();
		// TODO Propagation aux plugins ? Event ?
	}
	
	@Override
	public final synchronized void interrupt() {
		try {
			stop(true);
		} catch (StateChangeException e) { }
	}
	
	@Override
	public final synchronized void stop() throws StateChangeException {
		stop(false);
	}
	
	
	private void stop(boolean interrupted) throws StateChangeException {
		
		// Not started
		if (!isStarted()) {
			throw new AllreadyStoppedException();
		}
		
		// Log
		if (LOGGER.isLoggable(IncaLogger.INFO)) {
			LOGGER.log(IncaLogger.INFO, "Close intent");
		}
		
		// Event before
		if (!interrupted && !_eventsApp.dispatch("beforeApplicationStopped", this)) {
			return;
		}
		
		// Log
		if (LOGGER.isLoggable(IncaLogger.INFO)) {
			LOGGER.log(IncaLogger.INFO, "Application " + _appName + " is stopping");
		}
		
		// Set as stopped
		setState(getStoppedState(), !interrupted);
		
		Collection<AppPlugin> plugins = getPlugins().values();
		
		// Stop all activities
		for (AppPlugin p : plugins) {
			if (p instanceof AppActivity && ((AppActivity)p).isStarted()) {
				try {
					((AppActivity)p).stop();
				}
				catch (Throwable t) {
					// Log
					if (LOGGER.isLoggable(IncaLogger.WARNING)) {
						LOGGER.log(IncaLogger.WARNING, "Unable to stop activity: "
								+ getPluginName(p), t);
					}
				}
			}
		}
		
		// Dispose all plugins
		for (AppPlugin p : plugins) {
			try {
				p.dispose();
			}
			catch (Throwable t) {
				// Log
				if (LOGGER.isLoggable(IncaLogger.WARNING)) {
					LOGGER.log(IncaLogger.WARNING, "Unable to dispose plugin: "
							+ getPluginName(p), t);
				}
			}
		}
		
		// Arret en interne, sauf en cas d'interruption forcée
		if (!interrupted) {
			onStop();
		}
		
		// Remove all plugins
		_plugins.clear();

		// And all listeners
		_eventsApp.dispose();
		
		// Log
		if (LOGGER.isLoggable(IncaLogger.INFO)) {
			LOGGER.log(IncaLogger.INFO, "Application " + _appName + " is stopped");
		}
		
		// Destruct
		_appVersion = null;
		_appName = null;
		
		// On désassocie le singleton
		if (INSTANCE == this) {
			INSTANCE = null;
		}
		
		// Off
		if (_isMainApplication) {
			System.exit(0);
		}
		
	}
	
	/**
	 * Renvoie la liste des plugins, en thread-safe.
	 */
	public final Map<String, AppPlugin> getPlugins() {
		synchronized (_plugins) {
			return new HashMap<String, AppPlugin>(_plugins);
		}
	}
	
	public String getPluginName(AppPlugin plugin) {
		
		// Vérification des arguments
		if (plugin == null) {
			throw new NullPointerException();
		}
		
		// On copie la liste des plugins de l'application
		Map<String, AppPlugin> plugins = getPlugins();
		
		// On parcours les plugins
		for (String pluginName : plugins.keySet()) {
			
			// Le plugin courant
			AppPlugin p = plugins.get(pluginName);
			
			// C'est le bon
			if (p == plugin) return pluginName;
			
		}
		
		return plugin.getClass().getSimpleName();
			
	}
	
	/**
	 * Renvoie la liste des plugins qui sont du type donné.
	 * 
	 * @param type
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public final <T extends AppPlugin> Map<String, T> getPluginsByClass(Class<T> type) {
		
		// Vérification des arguments
		if (type == null) {
			throw new NullPointerException();
		}
		
		// On prépare la liste de sortie
		Map<String, T> out = new HashMap<String, T>();
		
		// On copie la liste des plugins de l'application
		Map<String, AppPlugin> plugins = getPlugins();
		
		// On parcours les plugins
		for (String pluginName : plugins.keySet()) {
			
			// Le plugin courant
			AppPlugin p = plugins.get(pluginName);
			
			// On filtre avec le type donné
			if (type.isInstance(p)) {
				out.put(pluginName, (T) p);
			}
			
		}
		
		// On renvoie la liste
		return out;
		
	}

	/**
	 * Renvoie le premier service du type classe.
	 * 
	 * @param classe
	 * @return Le premier service correspondant, ou NULL si aucun service n'a ce type
	 */
	public <T extends AppService> T getServiceByClass(Class<T> classe) {
		Map<String, T> plugins = getPluginsByClass(classe);
		String key = plugins.keySet().iterator().next();
		return plugins.get(key);
	}

	/**
	 * Renvoie le service associé au nom serviceName.
	 * 
	 * @param serviceName
	 * @return Le service correspondant, ou NULL si aucun service n'a ce nom
	 */
	public final AppService getServiceByName(String serviceName) {
		
		// Vérification des arguments
		if (serviceName == null) {
			throw new NullPointerException();
		}
		
		// On recupère les services
		Map<String, AppService> plugins = getPluginsByClass(AppService.class);
		
		// Fetch des services
		for (String pluginName : plugins.keySet()) {
			
			// Filtre sur le nom du plugin
			if (pluginName.equals(serviceName)) {
				return (AppService) plugins.get(pluginName);
			}
			
		}
		
		// Service non trouvé
		return null;
		
	}
	
	public final boolean removePlugin(AppPlugin plugin) {
		// Vérification des arguments
		if (plugin == null) {
			throw new NullPointerException();
		}
		// On retire tous les listeners de ce plugin
		getEventsApp().unbindTarget(plugin);
		// On retire le plugin dans la liste
		synchronized (_plugins) {
			if (!_plugins.containsValue(plugin)) {
				return false;
			}
			// Remove
			for (String key : _plugins.keySet()) {
				if (_plugins.get(key) == plugin) {
					_plugins.remove(key);
					return true;
				}
			}
		}
		return false;
	}
	
	public final boolean removePlugin(String pluginID) {
		// Vérification des arguments
		if (pluginID == null) {
			throw new NullPointerException();
		}
		// On retire le plugin dans la liste
		synchronized (_plugins) {
			if (!_plugins.containsKey(pluginID)) {
				return false;
			}
			// Remove
			AppPlugin plugin = _plugins.remove(pluginID);
			// On retire tous les listeners de ce plugin
			getEventsApp().unbindTarget(plugin);
		}
		return true;
	}

	/**
	 * Ajouter un plugin à l'application.
	 * 
	 * Renvoie FALSE si le nom de plugin est déjà utilisé
	 * 
	 * @param plugin
	 * @param pluginID
	 * @return
	 */
	public final boolean addPlugin(AppPlugin plugin, String pluginID) {
		
		// Vérification des arguments
		if (plugin == null || pluginID == null) {
			throw new NullPointerException();
		}
		
		// On ajoute le plugin dans la liste
		synchronized (_plugins) {
			if (_plugins.containsKey(pluginID)) {
				return false;
			}
			// Add
			_plugins.put(pluginID, plugin);
		}
		
		// On associe l'application au plugin
		plugin.setApplication(this);
		
		// On trigger l'event onApplicationStarted si le plugin a été
		// ajouté après le lancement de l'application.
		if (isStarted()) {
			// Debug
			if (LOGGER.isLoggable(IncaLogger.INFO)) {
				LOGGER.log(IncaLogger.INFO, "Plugin added: " + plugin.getClass().getSimpleName()
						+ " (" + plugin + ")");
			}
			_eventsApp.triggerOnly("onApplicationStarted", plugin, this);
		}
		
		// Debug
		if (LOGGER.isLoggable(IncaLogger.DEBUG)) {
			LOGGER.log(IncaLogger.DEBUG, "addPlugin(" + plugin.getClass().getCanonicalName() + ")");
		}
		
		// Status OK
		return true;
		
	}

	/**
	 * Renvoie le nombre de plugins associés à cette application.
	 */
	public final int getPluginCount() {
		synchronized (_plugins) {
			return _plugins.size();
		}
	}
	
	/**
	 * Renvoie le plugin identifié par pluginID, ou NULL si aucun plugin
	 * ne correspond.
	 */
	public final AppPlugin getPluginByID(String pluginID) {
		
		// Vérification des arguments
		if (pluginID == null) {
			throw new NullPointerException();
		}
		
		// On cherche dans la liste
		synchronized (_plugins) {
			return _plugins.get(pluginID);
		}
		
	}
	
	protected abstract StartedState getStartedState();
	protected abstract StoppedState getStoppedState();
	protected abstract PausedState getPausedState();
	
	/**
	 * Indique si l'application est lanc�e et non endormie.
	 */
	public final synchronized boolean isAlive() {
		return (_state instanceof StartedState && !(_state instanceof PausedState));
	}
	
	/**
	 * Indique si l'application est lancée.
	 * 
	 * @return FALSE si l'application n'a pas été lancée avec start().
	 */
	public final synchronized boolean isStarted() {
		return (_state instanceof StartedState);
	}
	
	@Override
	public synchronized final ILocalService[] getPublishedServices() {
		
		ArrayList<ILocalService> services = new ArrayList<ILocalService>();
		
		// On parcours les plugins
		for (AppPlugin p : _plugins.values()) {
			
			// On cherche ceux qui sont des services locaux
			if (p instanceof ILocalService) {
				
				final ILocalService s = (ILocalService) p;
				
				// On ne garde que les services publiés
				if (!s.isPublished()) {
					continue;
				}
				 
				services.add(s);
				
			}
		}
		
		return services.toArray(new ILocalService[0]);
	}
	
	/**
	 * Temps actuel, en millisecondes depuis l'époch.
	 */
	public static long currentTimeMillis() {
		return new Date().getTime();
	}
	
	public static long getTimeMillis() {
		return currentTimeMillis();
	}

	/**
	 * Modifie l'instance principale d'application.
	 */
	public static final void setInstance(App app) {
		INSTANCE = app;
	}
	
	/**
	 * Permet d'obtenir l'instance de la dernière application créée.
	 * S'il n'y a qu'une application, cette méthode permet de récupèrer
	 * simplement l'application en cours de manière statique.
	 */
	public static App getInstance() {
		return INSTANCE;
	}
	
	/**
	 * Renvoie l'actvité parente qui a initialiser cette application.
	 */
	public static final AppActivity getParentInvoker() {
		//throw new NotImplementedException();
		return null;
	}
	
	public static File getJarFile() {
		try {
			return new File(App.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		} catch (Throwable ex) {
			throw new RuntimeException(ex);
		}
	}

	public static void start(String[] args, Class<? extends App> classe, String methodName) {
		
		// Debug mode
		final boolean debugMode = args != null && args.length > 0 &&
				args[0].toLowerCase().equals("debug=1");
		
		// Ajustement du niveau de log
		IncaLogger.setDefaultLevel(debugMode ? IncaLogger.INFO : IncaLogger.NONE);
		IncaLogger.setGlobalLevel(debugMode ? IncaLogger.INFO : IncaLogger.NONE);
		
    	// Initialisations pour Swing
    	SwingHelper.initLookAndFeel();
    	SwingHelper.initSwingAnimations();
    	SwingHelper.adjustGlobalFontSize(13);
		
		try {
			
			// On cherche la méthode de factory
			Method m = classe.getMethod(methodName, Boolean.class, Boolean.class);
			
			// Création de l'application
	    	App app = (App) m.invoke(null, true, debugMode);
			
			// Lancement de l'application	
			app.start();
			
		}
		catch (Throwable e) {
			
			// Erreur fatale
			if (e instanceof NoSuchMethodException) {
				System.err.println("FATAL ERROR: factory method must respect " +
						classe.getSimpleName() + "." + methodName + "(Boolean, Boolean)");
			}
			else {
				System.err.println("FATAL ERROR: " + e.getClass().getName());
			}
			
			// On affiche les traces
			if (debugMode) e.printStackTrace();
			
			// Et on arrête l'application avec un code d'erreur
			System.exit(500);
			
		}
		
	}
	
	public boolean enableSingletonBehavior() throws AllreadyStartedException {
		RuntimeMXBean rt = ManagementFactory.getRuntimeMXBean();  
        final int runtimePid = Integer.parseInt(rt.getName().substring(0,rt.getName().indexOf("@")));
        final File file = new File("./lock");
        try
        {
            if (file.createNewFile())
            {
                file.deleteOnExit();
                Util.filePutContents(file, "" + runtimePid);
                return true;
            }
        }
        catch (IOException e)
        {
        }
        throw new AllreadyStartedException(getApplicationName() + " is allready running. Check that no other instance is started and if so, delete the file lock");
	}
	
}
