package fr.evolya.javatoolkit.appstandard;

import java.io.File;

import fr.evolya.javatoolkit.app.config.AppConfiguration;
import fr.evolya.javatoolkit.app.config.Configurable;
import fr.evolya.javatoolkit.app.config.NonPersistentConfiguration;
import fr.evolya.javatoolkit.app.config.PropertiesFileConfiguration;
import fr.evolya.javatoolkit.appstandard.events.ConfigListener;
import fr.evolya.javatoolkit.code.Logs;
import fr.evolya.javatoolkit.code.utils.Utils;
import fr.evolya.javatoolkit.events.attr.EventSource;

/**
 * Classe abstraite pour la création d'applications basée sur un systéme de
 * fichier de configuration.
 */
@Deprecated
public abstract class AbstractConfigurableApplication
	extends AbstractApplication implements Configurable {

	/**
	 * La configuration
	 */
	private AppConfiguration _config;

	/**
	 * Chemin vers le fichier de configuration
	 * Mettre à NULL pour désactiver ce comportement 
	 */
	private String _configFilename = null;
	
	/**
	 * Events de la config
	 */
	private EventSource<ConfigListener> _eventsConfig =
			new EventSource<ConfigListener>(ConfigListener.class, this);
	
	/**
	 * Constructeur
	 */
	public AbstractConfigurableApplication(String appName, String appVersion, boolean isMainApplication) {
		super(appName, appVersion, isMainApplication);
		_config = new NonPersistentConfiguration();
		initDefaultConfig(_config);
	}
	
	@Override
	protected void onStart() {
		// On passe plus haut
		super.onStart();
		// On charge la configuration
		boolean success = loadLocalConfig();
		// Event
		_eventsConfig.trigger("onConfigurationRestored", _config, this, _configFilename, success);
	}
	
    
	@Override
	protected void onStop() {
		// On sauvegarde la configuration
		saveLocalConfig();
		// On passe plus haut
		super.onStop();
	}


	/**
	 * Chargement de la configuration locale
	 */
	public boolean loadLocalConfig() {
		
		// Ce système est désactivé
		if (_configFilename == null) {
			return false;
		}
		
		// On initialise la configuration
		initConfigImplementation(false);
		
		// On charge les données
		try {
			if (!_config.load(new File(_configFilename))) {
				return false;
			}
		}
		catch (Exception ex) {
			// Log
			if (App.LOGGER.isLoggable(Logs.INFO)) {
				App.LOGGER.log(Logs.INFO, "Unable to load configuration: " + _configFilename
						+ " (" + ex.getClass().getSimpleName() + " : " + ex.getMessage() + ")");
			}
			// On renvoie un false pour indiquer l'erreur
			return false;
		}
		
		// Log
		if (App.LOGGER.isLoggable(Logs.INFO)) {
			App.LOGGER.log(Logs.INFO, "Configuration loaded: " + _configFilename);
		}
		
		// On informe l'application
		try {
			onConfigurationRestored(_config);
		}
		catch (Throwable t) {
			// Log
			if (App.LOGGER.isLoggable(Logs.ERROR)) {
				App.LOGGER.log(Logs.ERROR, t.getClass().getSimpleName() + " thrown by onConfigurationRestored() : "
						+ t.getMessage(), t);
			}
			return false;
		}
		
		return true;
		
	}

	public boolean saveLocalConfig() {
		
		// Ce système est désactivé
		if (_configFilename == null) {
			return false;
		}
		
		// On initialise la configuration
		initConfigImplementation(false);
			
		try {
			
			// Event before
			if (!_eventsConfig.trigger("beforeConfigurationSaved", _config, this, _configFilename)) {
				return false;
			}
			
			// Enregistrement
			if (!_config.save(new File(_configFilename))) {
				return false;
			}
			
			// Event after
			_eventsConfig.trigger("afterConfigurationSaved", _config, this, _configFilename);
			
			// Log
			if (LOGGER.isLoggable(Logs.INFO)) {
				LOGGER.log(Logs.INFO, "Configuration saved: " + _configFilename);
			}
			
			return true;
			
		} catch (Exception ex) {
			// Log
			if (App.LOGGER.isLoggable(Logs.INFO)) {
				App.LOGGER.log(Logs.INFO, "Unable to save configuration: " + _configFilename
						+ " (" + ex.getClass().getSimpleName() + " : " + ex.getMessage() + ")");
			}
			return false;
		}
	
	}

	protected boolean initConfigImplementation(boolean log) {
		// Le système est désactivé
		if (_configFilename == null) {
			return false;
		}
		// Si la configuration n'a pas d'implementation précise
		if (_config instanceof NonPersistentConfiguration) {
			// On passe par la factory pour trouver une implémentation qui match avec l'extension
			AppConfiguration config = configFactory(Utils.get_extension(_configFilename), log);
			// Impossible de fabriquer la config
			if (config != null) {
				// On conserve les anciennes valeurs
				config.addProperties(_config.getProperties());
				// On change d'implementation
				_config = config;
				// Le système est prêt
				_eventsConfig.trigger("onConfigurationReady", _config, this);
				// C'est bon
				return true;
			}
		}
		return false;
	}
	
	private AppConfiguration configFactory(String extension, boolean log) {
		switch (extension.toLowerCase()) {
		case "properties" : return new PropertiesFileConfiguration();
		}
		// Log
		if (log && App.LOGGER.isLoggable(Logs.INFO)) {
			App.LOGGER.log(Logs.INFO, "No configuration implementation found for '"
					+ extension + "' extension");
		}
		return null;
	}

	@Override
	public void setConfigStorage(String filename) {
		
		// On enregistre le nom du fichier
		_configFilename = filename;

		// Si on retire l'implémentation, on s'assure que la configuration
		// soit bien une NonPersistentConfiguration
		if (filename == null) {
			if (!(_config instanceof NonPersistentConfiguration)) {
				_config = new NonPersistentConfiguration(_config);
			}
		}
		
		// Si on a un nom de fichier, on cherche à initialiser une implémentation
		else {
			initConfigImplementation(true);
		}
	}

	public String getConfigStorage() {
		return _configFilename;
	}
	
	@Override
	public AppConfiguration getConfig() {
		return _config;
	}
	
	public EventSource<ConfigListener> getEventsConfig() {
		return _eventsConfig;
	}
	
	// Méthodes abstraites à implémenter
	protected abstract void initDefaultConfig(AppConfiguration config);
	protected abstract void onConfigurationRestored(AppConfiguration config);

}
