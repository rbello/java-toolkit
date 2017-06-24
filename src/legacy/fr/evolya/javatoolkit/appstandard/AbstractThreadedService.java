package fr.evolya.javatoolkit.appstandard;

import java.util.logging.Logger;

import fr.evolya.javatoolkit.appstandard.events.ServiceListener;
import fr.evolya.javatoolkit.code.Logs;
import fr.evolya.javatoolkit.code.annotations.AsynchOperation;
import fr.evolya.javatoolkit.code.annotations.ToOverride;
import fr.evolya.javatoolkit.events.attr.EventSource;
import fr.evolya.javatoolkit.exceptions.StateChangeException;
import fr.evolya.javatoolkit.threading.worker.IWorker;
import fr.evolya.javatoolkit.threading.worker.TimerOperation;
import fr.evolya.javatoolkit.threading.worker.Worker;

/**
 * Classe adapter pour la création de services pouvant avoir des tâches à lancer
 * en background les unes après les autres.
 */
@Deprecated
public abstract class AbstractThreadedService extends AbstractPlugin
	implements AppService {
	
	/**
	 * Logger des services.
	 */
	public static final Logger LOGGER = Logs.getLogger("Services");

	/**
	 * Indique si ce service doit se lancer au démarrage de l'application.
	 */
	protected boolean _autoStart;

	/**
	 * Indique si ce service est publié de manière publique.
	 */
	protected boolean _isPublished;

	/**
	 * Indique si le service est actuellement lancé.
	 */
	protected boolean _started = false;
	
	/**
	 * Les threads qui vont consommer les traitements asynchrones opérations
	 * à traiter par le service. Le but étant de pouvoir réagire à des events
	 * sans bloquer les threads.
	 */
//	protected Thread _thread;

	/**
	 * La liste des jobs en cours.
	 */
//	protected List<Runnable> _job = new ArrayList<Runnable>();
	
	protected IWorker _worker;

	/**
	 * Bus des events des services.
	 */
	protected EventSource<? extends ServiceListener> _eventsService;
	
	/**
	 * Constructeur simple.
	 * Par défaut, l'interface de listener pour le service est ServiceListener.
	 * Egalement, le service n'est pas publié et il ne démarre pas automatiquement.
	 */
	public AbstractThreadedService() {
		this(ServiceListener.class, false, false);
	}
	
	/**
	 * Constructeur avec la classe de l'iterface du listener de service.
	 * Par défaut, le service n'est pas publié et il ne démarre pas automatiquement.
	 */
	public <L extends ServiceListener> AbstractThreadedService(Class<L> classe) {
		this(classe, false, false);
	}
	
	/**
	 * Constructeur avec les paramétres de publication et de démarrage automatique.
	 * Par défaut, l'interface de listener pour le service est ServiceListener.
	 */
	public AbstractThreadedService(boolean isServicePublished, boolean autoStart) {
		this(ServiceListener.class, isServicePublished, autoStart);
	}
	
	/**
	 * Constructeur complet avec tous les paramètres.
	 */
	public <L extends ServiceListener> AbstractThreadedService(Class<L> classe,
			boolean isServicePublished, boolean autoStart) {
		_autoStart = autoStart;
		_isPublished = isServicePublished;
		_eventsService = new EventSource<L>(classe, this);
		_worker = new Worker(this);
	}

	@Override
	public EventSource<? extends ServiceListener> getEventsService() {
		return _eventsService ;
	}
	
	@Override
	public final synchronized boolean isStarted() {
		return _started;
	}

	@Override
	public final synchronized void start() throws StateChangeException, SecurityException {
		
		// Si le service est déjà lancé on ne fait rien
		if (_started) {
			throw new StateChangeException("Service allready started");
		}
		
		// Event before
		//_eventsService.trigger("beforeServiceStarted", this);
		
		getEventsService().setEnabled(true);
		
		// Lancement interne
		try {
			onStart();
		}
		catch (Exception e) {
			throw new StateChangeException("Unable to start service", e);
		}
		
		// On change le status
		_started = true;
		
		// On active la source d'events
		_eventsService.setEnabled(true);
		
		// On active le worker
		_worker.start();
		
		// Log
		if (LOGGER.isLoggable(Logs.INFO)) {
			if (getApplication() != null) {
				LOGGER.log(Logs.INFO, "Service " + getApplication().getPluginName(this) + " is started");
			}
			else {
				LOGGER.log(Logs.INFO, getClass().getSimpleName() + " is started");
			}
		}
		
		// Event au niveau de l'application
		if (getApplication() != null)
			getApplication().getEventsApp().trigger("onServiceStarted", getApplication(), this);
		
		// Event after
		_eventsService.trigger("onServiceStarted", this);
		
	}

	protected abstract void onStart() throws Exception;

	@Override
	public final synchronized void stop() throws StateChangeException {
		
		// Si le service est arrêté, on ne fait rien 
		if (!_started) {
			throw new StateChangeException("Service not started");
		}
		
		// On arrête le worker
		_worker.stop();
		
		// Event before
		//_eventsService.trigger("beforeServiceStopped", this);
		
		try {
		
			// Arrêt interne
			onStop();

		}
		catch (Exception e) {
			throw new StateChangeException("Unable to stop service gracefully", e);
		}
		
		getEventsService().setEnabled(false);
		
		// Arrêt forcé
		interrupt();
		
		// Event au niveau de l'application
		getApplication().getEventsApp().trigger("onServiceStopped", getApplication(), this);
		
		// Event after
		_eventsService.trigger("onServiceStopped", this);
		
		// On arrête la source d'events
		_eventsService.setEnabled(false);
		
	}
	
	@AsynchOperation
	public TimerOperation invokePeriodic(final Runnable run, final long delay) {
		return (TimerOperation) _worker.invokeLater(new TimerOperation(delay, run));
	}
	
	@AsynchOperation
	public void invokeLater(Runnable run) {
		_worker.invokeLater(run);
		/*
		// On se synchronise sur le manager
		synchronized (AbstractThreadedService.this) {
			
			// On ajoute un travail dans la list
			_job.add(run);
			
			// Log
			if (LOGGER.isLoggable(IncaLogger.DEBUG)) {
				LOGGER.log(IncaLogger.DEBUG, "Service "
						+ getApplication().getPluginName(AbstractThreadedService.this)
						+ " has a new job, " + _job.size() + " stacked");
			}
			
			// Si le thread est en cours d'action, il découvrira
			// automatiquement le nouveau job. 
			if (_thread != null) {
				return;
			}
			
			// On fabrique un thread, qui va s'occuper d'effectuer
			// les travaux qui sont dans la pile.
			_thread = new Thread(new Runnable() {
				public void run() {
					
					// On boucle en continue
					while (true) {
					
						// Le job à effectuer
						Runnable job = null;
						
						// Nombre de jobs restants
						int remaining = 0;
						
						// On se synchronise sur le manager
						synchronized (AbstractThreadedService.this) {
							
							// Aucun job
							if (_job.size() < 1) {

								// On termine le travail du thread
								_thread = null;
								
								// On en stoppe
								return;
								
							}
							
							// On recupère le job le plus ancien (FIFO) et on
							// le retire des jobs en cours
							job = _job.remove(0);
							
							// On compte le nombre de jobs restants
							remaining = _job.size();
							
						}
						
						// En dehors du bloc synchronisé, on regarde si on a trouvé un job
						// Si on n'en a pas, on a une situation inatendue
						if (job == null) {
							
							// On log
							if (LOGGER.isLoggable(IncaLogger.WARNING)) {
								LOGGER.log(IncaLogger.WARNING, "Unexpected case: the service" +
										" thread is unable to find a job to execute. Class: "
										+ getClass().getSimpleName());
							}
							
							// On termine le travail du thread
							_thread = null;
							
							// On en stoppe
							return;
							
						}
						
						// On execute le job, en se protégeant des exceptions
						try {
							job.run();
						}
						catch (Throwable ex) {

							// On délégue le traitement de l'erreur
							onThreadException(ex, job);
							
						}
						
						// Log
						if (LOGGER.isLoggable(IncaLogger.DEBUG)) {
							LOGGER.log(IncaLogger.DEBUG, "Service "
									+ getApplication().getPluginName(AbstractThreadedService.this)
									+ " finished a job, " + remaining + " remaining");
						}
						
						// On laisse continuer pour consommer la pile des jobs
						
					}
					
				}

			});
			
			_thread.setName("ThreadedService-" + hashCode());
			
			// On lance le thread. C'est lui qui va s'occuper de
			// de se fermer tout seul.
			_thread.start();
			
		}
		*/
		
	}
	
	/**
	 * Lors de la levée d'une exception dans le thread de traitement des
	 * jobs en asynchrone. Méthode à overrider.
	 */
//	@ToOverride
//	protected void onThreadException(Throwable ex, Runnable job) { }
	
	@Override
	public final synchronized void interrupt() {
		
		// Allready stopped
		if (!_started) {
			return;
		}
		
		int count = _worker.getRunningCount() + _worker.getPendingCount();
		
		// On coupe le thread actif s'il est lancé
		if (_worker != null) {
			_worker.stop();
		}
		
		// On indique qu'on a fait l'arrêt
		_started = false;
		
		// On déconnecte les listeners
		if (getApplication() != null) {
			getApplication().getEventsApp().unbind("afterApplicationStarted", this, "start");
			getApplication().getEventsApp().unbind("beforeApplicationStopped", this, "stop");
		}
		
		// Log
		if (LOGGER.isLoggable(Logs.INFO)) {
			LOGGER.log(Logs.INFO, "Service " + getApplication().getPluginName(this)
					+ " was stopped (" + count + " job(s) has been halted)");
		}
		
	}
	
	protected abstract void onStop() throws Exception;
	
	public void dispose() {
		interrupt();
		_eventsService.unbind();
		_eventsService = null;
		super.dispose();
	}
	
	@Override
	protected final synchronized void connected(App app) {
		
		// On associe le lancement de l'application avec le lancement de ce service
		// si c'est demandé.
		if (_autoStart) {
			app.getEventsApp().bind("onApplicationStarted", this, "start");
		}
		
		// On avertit les implémentations
		onConnected(app);
		
	}

	@ToOverride
	protected void onConnected(App app) {
	}
	
	@Override
	public final boolean isPublished() {
		return _isPublished;
	}
	
	public IWorker getWorker() {
		return _worker;
	}
	
}
