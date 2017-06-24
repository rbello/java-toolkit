package fr.evolya.javatoolkit.appstandard;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import fr.evolya.javatoolkit.appstandard.events.ViewControllerListener;
import fr.evolya.javatoolkit.code.Logs;
import fr.evolya.javatoolkit.events.attr.EventListener;
import fr.evolya.javatoolkit.events.attr.EventSource;
import fr.evolya.javatoolkit.gui.swing.ProxyFrameView;
import fr.evolya.javatoolkit.gui.swing.decoratedframe.DecoratedFrame;

/**
 * Les controleurs de vue permettent de rajouter une vue à une application,
 * en respectant la logique de construction des IHM.
 *
 * @param <V> Le type de la vue
 * @param <W> Le type de composant graphique que la vue offre (JPanel, JFrame, ...)
 * @param <E> Le type d'event qui est propagé par le framework (java.awt.AWTEvent pour swing)
 * @param <C> Le type de composant contenu dans la vue (java.awt.Component pour swing)
 */
@Deprecated
public abstract class AppViewController<V extends AppView<W, E, C>, W, E, C>
	extends AbstractPlugin {

	public static final Logger LOGGER = Logs.getLogger("GUI");
	
	/**
	 * La vue associée à ce controleur
	 */
	protected V _view;
	
	/**
	 * Events des controleurs de vues
	 */
	protected EventSource<ViewControllerListener> _eventsViewCtrl = new EventSource<ViewControllerListener>(this) {
		@Override
		public Class<? extends EventListener> getListenerClass() {
			return ViewControllerListener.class;
		}
	};
	
	/**
	 * Indique si un travail sur la vue est en cours.
	 * NULL = Rien
	 * TRUE = Création
	 * FALSE = Destruction
	 */
	protected Boolean _working = null;

	/**
	 * L'ID de la vue.
	 */
	protected String _viewID;
	
	/**
	 * Constructeur vide. Si on l'utilise, c'est qu'à priori on utilisera
	 * la commande buildView() pour la création de la vue. Mais ce n'est
	 * pas obligé du tout.
	 */
	public AppViewController() {
	}
	
	/**
	 * Constructeur avec l'association au controlleur.
	 */
	public AppViewController(App app) {
		setApplication(app);
	}
	
	/**
	 * Constructeur avec l'association à l'application, et la vue déjà
	 * créee.
	 */
	public AppViewController(App app, V view) {
		setApplication(app);
		setView(view, null);
	}
	
	/**
	 * Renvoie la vue actuellement associée à ce controleur.
	 * Cette méthode peut renvoyer NULL si la séquence d'initialisation
	 * (avec createView) n'a pas été bien respectée.
	 */
	public final V getView() {
		return _view;
	}
	
	/**
	 * Renvoie TRUE si le controlleur est actuellement en train de construire
	 * la vue.
	 */
	public final synchronized boolean isWorking() {
		return _working != null;
	}
	
	/**
	 * Demande la création de la vue, en asynchrone.
	 * 
	 * @return TRUE Si la vue est en cours de création, ou FALSE
	 * si elle est déjé créée
	 */
	public synchronized boolean buildView(final Boolean setVisible) {
		
		// La vue existe déjé
		if (_view != null) {
			return false;
		}
		
		// Un travail de méme nature est en cours
		if (_working != null && _working == true) {
			return true;
		}
		
		// Un travail de nature différente est en cours
		if (_working != null && _working == false) {
			throw new SecurityException("Can't build during dispose");
		}

		// Création de l'IHM dans le pool GUI
		SwingUtilities.invokeLater(new Runnable() {
			
			public void run() {
        		
            	// Construction de la vue
            	final V view = constructView();
            	
            	// Association
        		setView(view, setVisible);
            	
            }
            
        });
		
		return true;
		
	}
	
	/**
	 * Méthode interne pour associer la vue au controller si elle
	 * est déjà créée, et qu'on utilise pas buildView().
	 */
	protected void setView(final V view, Boolean setVisible) {
		
		// Méfiance...
    	if (view == null) {
    		// TODO Gestion de cette erreur
    		synchronized (this) {
    			_working = null;
    		}
    		throw new NullPointerException();
    	}
		
		// Log
		if (LOGGER.isLoggable(Logs.INFO)) {
			LOGGER.log(Logs.INFO, "View created: " + view + " (by " + getClass().getSimpleName() + ")");
		}
		
		// ID unique de cette vue
    	_viewID = view.getClass().getSimpleName();
    	view.setViewController(this);
    	
    	// Event
		_eventsViewCtrl.trigger("onViewCreated", this, view);
    	
    	// Bind des events pour la fermeture de l'application
		view.getEventsView().bind("onViewCloseIntent", this, "onViewCloseIntent");

		// Bind automatique pour les DecoratedFrame décorées par un ProxyFrameView
		if (view instanceof ProxyFrameView) {
			if (((ProxyFrameView<?>) view).getFrameView() instanceof DecoratedFrame) {
				((DecoratedFrame) ((ProxyFrameView<?>) view).getFrameView())
				.setCloseButtonAction(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						viewCloseIntent(e, true);
					}
				});
			}
		}
		
		// On se resynchronise sur le controlleur
    	synchronized (this) {
    		
    		// On sauvegarde la vue
    		_view = view;
    		
    		// On déclenche un event interne
    		onViewCreated();
    		
    		// On lève le verrou
			_working = null;
			
		}
    	
		// Affichage de la vue
    	if (setVisible != null) {
    		view.setVisible(setVisible);
    	}
    	
	}
	
	/**
	 * Event interne quand la vue principale est crée.
	 */
	protected abstract void onViewCreated();
	
	/**
	 * Event interne quand la vue principale est fermée.
	 */
	protected abstract void onViewClosed();
	
	/**
	 * Demander la création de la vue.
	 */
	protected abstract V constructView();
	
	/**
	 * Renvoie le bus des events du controlleur de vue.
	 */
	public EventSource<ViewControllerListener> getEventsViewCtrl() {
		return _eventsViewCtrl;
	}

	@Override
	public synchronized void dispose() {
		if (_view != null) {
			_view.dispose();
			_view = null;
		}
		super.dispose();
	}
	
	@Override
	protected void connected(App app) {
	}
	
	/**
	 * Aliase.
	 * 
	 * C'est LA méthode à utiliser pour fermer une fenêtre controllée.
	 */
	public synchronized void viewCloseIntent(Object event, Boolean isUserIntent) {
		onViewCloseIntent(getView(), event, isUserIntent);
	}
	
	/**
	 * A la demande de fermeture, pouvant venir de plusieurs vues
	 */
	public synchronized void onViewCloseIntent(AppView<?, ?, ?> view, Object event, Boolean isUserIntent) {

		// Si on est en cours de construction, c'est une erreur
		if (_working != null && _working == true) {
			throw new SecurityException("can't close during building");
		}
		
		// Si on est déjà en train de le faire, on ne fait rien
		if (_working != null && _working == false) {
			return;
		}
		
    	// On ne garde que les events qui viennent de la vue du controlleur.
		// Pour les proxy, on compare aussi à la vue interne.
		if (_view instanceof ProxyFrameView) {
			if (view != ((ProxyFrameView<?>) _view).getFrameView()
					&& view != _view) {
				return;
			}
		}
		// Pour les vues normales
		else {
	    	if (view != _view) {
	    		return;
	    	}
		}

    	// Event before
    	if (!getEventsViewCtrl().trigger("beforeViewCloseIntent", this, isUserIntent)) {
    		return;
    	}
    	
		// Log
		if (LOGGER.isLoggable(Logs.INFO)) {
			LOGGER.log(Logs.INFO, "View closed: " + view + " (by " + getClass().getSimpleName() + ")");
		}
    
    	// Destruction interne
    	onViewClosed();
    	
		// On masque la vue
    	_view.setVisible(false);
		
		// On détruit la vue
    	_view.setViewController(null);
    	_view.dispose();
    	_view = null;
    	
		// Event after
		getEventsViewCtrl().trigger("afterViewCloseIntent", this, isUserIntent);
    	
	}
	
	public String getViewID() {
		return _viewID;
	}
	
}
