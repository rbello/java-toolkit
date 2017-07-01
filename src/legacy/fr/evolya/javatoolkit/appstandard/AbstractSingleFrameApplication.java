package fr.evolya.javatoolkit.appstandard;

import fr.evolya.javatoolkit.app.config.AppConfiguration;
import fr.evolya.javatoolkit.appstandard.events.AppListenerAdapter;
import fr.evolya.javatoolkit.appstandard.events.ConfigListenerAdapter;
import fr.evolya.javatoolkit.appstandard.events.ViewControllerListenerAdapter;
import fr.evolya.javatoolkit.exceptions.StateChangeException;
import fr.evolya.javatoolkit.gui.ViewBounds;

/**
 * Classe abstraite pour la création d'applications basées sur une IHM principale,
 * et qui a un systême de configuration type .properties.
 * 
 * @param <X> La classe du controleur
 * @param <V> La classe de la vue
 * @param <W> Le type de composant graphique que la vue offre (JPanel, JFrame, ...)
 * @param <E> Le type d'event qui est propagé par le framework (AWT event pour swing)
 * @param <C> La classe du composant
 */
@Deprecated
public abstract class AbstractSingleFrameApplication
	<
		X extends AppViewController<V, W, E, C>,
		V extends AppView<W, E, C>,
		W,
		E,
		C
	>
	extends AbstractConfigurableApplication {
	
	/**
	 * Le controleur de la vue principale de l'application.
	 */
	private X _ctrl;
	
	/**
	 * Les dernières bornes de la fenêtre principale, utile pour l'enregistrement
	 * dans le configuration (pour permettre la mémorisation de la position des fen�tres)
	 */
	private ViewBounds _bounds;

	/**
	 * Enregistrer les dimensions et positions de la mainframe dans la config
	 * automatiquement, et les restaurer au lancement de l'application.
	 */
	private boolean _isAutoSaveFrameGeometry = true;
	
	/**
	 * Constructeur.
	 */
	public AbstractSingleFrameApplication(String appName, String appVersion, boolean isMainApplication) {
		
		super(appName, appVersion, isMainApplication);
		
	}
	
	public void setAutoSaveFrameGeometry(boolean enable) {
		_isAutoSaveFrameGeometry = enable;
	}
	
	@Override
	protected void onStart() {

		// On démarre un niveau plus haut
		super.onStart();
		
		// On demande la création du controlleur principal
		createMainController();
		

	}

	/**
	 * Construction du controleur principal.
	 */
	protected void createMainController() {
		
		// Création du controleur de vue
		_ctrl = createMainViewController();
		
		// Vérification
		if (_ctrl == null) {
			throw new NullPointerException("View controller is null");
		}
		
		// Ajout de la vue
		// TODO Pouvoir configurer le nom du plugin
		addPlugin(_ctrl, "main-view");

		// Binds sur les events d'application
		getEventsApp().bind(new AppListenerAdapter() {
			
			/**
			 * Création de la vue au lancement de l'application
			 */
			public void onApplicationStarted(App app) {
				_ctrl.buildView(true);
			}
			
		});
		
		// Binds sur les events du controleur de vue
		_ctrl.getEventsViewCtrl().bind(new ViewControllerListenerAdapter() {
			
			/**
			 * Sauvegarde de la position de la fenêtre avant l'extinction de l'application. 
			 */ 
			@SuppressWarnings("unchecked")
			public boolean beforeViewCloseIntent(AppViewController ctrl, boolean isUserIntent) {
				_bounds = ctrl.getView().getViewBounds();
				return true;
			}
			
			/**
			 * Arrêt de l'application à la fermeture de la vue principale.
			 */
			@SuppressWarnings("unchecked")
			public void afterViewCloseIntent(AppViewController ctrl, boolean isUserIntent) {
				// Et on arrête l'application
		    	try {
					stop();
				} catch (StateChangeException e) {
					new RuntimeException(e);
				}
			}
			
			/**
			 * Restoration de la position et des dimensions de la fenêtre principale
			 * au lancement de l'application. 
			 */
			@SuppressWarnings("rawtypes")
			public void onViewCreated(AppViewController ctrl, AppView view) {
				if (!_isAutoSaveFrameGeometry) {
					return;
				}
				if (getConfig().containsKey("frame." + ctrl.getViewID() + ".x")) {
					view.setViewBounds(new ViewBounds(
						Double.parseDouble(getConfig().getProperty("frame." + ctrl.getViewID() + ".x")),
						Double.parseDouble(getConfig().getProperty("frame." + ctrl.getViewID() + ".y")),
						Double.parseDouble(getConfig().getProperty("frame." + ctrl.getViewID() + ".w")),
						Double.parseDouble(getConfig().getProperty("frame." + ctrl.getViewID() + ".h"))
					));
				}
			}
			
		});
    	
		// Binds sur les events de la configuration
		getEventsConfig().bind(new ConfigListenerAdapter() {
			/**
			 * Sauvegarde de la geometrie de la fenêtre principale
			 */
			@Override
			public boolean beforeConfigurationSaved(AppConfiguration config, Configurable target, String filename) {
				if (!_isAutoSaveFrameGeometry) {
					return true;
				}
				if (_bounds != null) {
					config.setProperty("frame." + _ctrl.getViewID() + ".x", "" + _bounds.getX());
					config.setProperty("frame." + _ctrl.getViewID() + ".y", "" + _bounds.getY());
					config.setProperty("frame." + _ctrl.getViewID() + ".w", "" + _bounds.getWidth());
					config.setProperty("frame." + _ctrl.getViewID() + ".h", "" + _bounds.getHeight());
				}
				return true;
			}
		});
		
	}
	
	/**
	 * Renvoie le controlleur de la vue principale.
	 * @return
	 */
	public X getViewController() {
		return _ctrl;
	}
	
	/**
	 * Demander la création du controleur principale de vue.
	 */
	protected abstract X createMainViewController();

}
