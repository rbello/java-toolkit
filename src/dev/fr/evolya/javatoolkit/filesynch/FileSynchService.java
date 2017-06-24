package fr.evolya.javatoolkit.filesynch;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import fr.evolya.javatoolkit.appstandard.AbstractThreadedService;
import fr.evolya.javatoolkit.appstandard.App;
import fr.evolya.javatoolkit.appstandard.AppService;
import fr.evolya.javatoolkit.appstandard.bridge.services.ELocalServiceType;
import fr.evolya.javatoolkit.code.IncaLogger;
import fr.evolya.javatoolkit.code.RunnableTimer;
import fr.evolya.javatoolkit.events.attr.EventSource;

public class FileSynchService extends AbstractThreadedService implements AppService, Runnable {

	public static final Logger LOGGER = IncaLogger.getLogger("FileSynch");
	
	private List<FileSynchManager> _mgrs;

	private RunnableTimer _timer;

	public FileSynchService() {
		this(false);
	}
	
	public FileSynchService(boolean autoStart) {
		
		// On donne la classe du listener de service
		super(FileSynchServiceListener.class);
		
		// Par défaut ont utilise deux threads
		getWorker().setMaxRunningJobs(2);
		
		// Si le service doit se lancer automatiquement au démarrage de l'application
		_autoStart = autoStart;
		
		// On fabrique la liste des managers de synchro
		_mgrs = new ArrayList<FileSynchManager>();
		
		// Le timer qui relance périodiquement le refresh
		_timer = new RunnableTimer(30000, true, this);
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public EventSource<FileSynchServiceListener> getEventsService() {
		// On surcharge cette méthode pour obtenir le bon type de listener
		return (EventSource<FileSynchServiceListener>) super.getEventsService();
	}
	
	@Override
	public ELocalServiceType getServiceType() {
		return ELocalServiceType.FILE_SYNCHRONIZATION;
	}

	protected void onStart() throws Exception {
//		_timer.start();
	}

	@Override
	protected void onStop() throws Exception {
//		_timer.stop();
	}

	public void addSynchManager(FileSynchManager mgr) {
		_mgrs.add(mgr);
	}
	
	@Override
	protected void onConnected(App app) {
		
	}

	@Override
	public void run() {
		
		// On parcours les managers
		for (FileSynchManager mgr : _mgrs) {
			
			// Si le manager n'est pas initialisé
			if (!mgr.isReady()) {
				try {
					// On tente de l'initialiser
					mgr.init();
					// Tant pis...
					if (!mgr.isReady()) {
						continue;
					}
					// Log
					if (LOGGER.isLoggable(IncaLogger.INFO)) {
						LOGGER.log(IncaLogger.INFO, "FileSynchManager ready: " + mgr);
					}
				}
				catch (Throwable t) {
					if (LOGGER.isLoggable(IncaLogger.DEBUG)) {
						LOGGER.log(IncaLogger.DEBUG, "Unable to initialize FileSynchManager: "
								+ mgr, t);
					}
					continue;
				}
			}
			
			// On liste les opérations à faire
			List<SynchOperation> operations = mgr.detectChanges("");
			
			// Aucune opération
			if (operations == null || operations.size() == 0) {
				continue;
			}

			// Log
			if (LOGGER.isLoggable(IncaLogger.INFO)) {
				LOGGER.log(IncaLogger.INFO, "Found " + operations.size() + " changes on "
						+ mgr);
			}
			
			for (final SynchOperation op : operations)
				invokeLater(op);
			
			// On parcours les opérations
			/*for (final SynchOperation op : operations) {
				invokeLater(new Runnable() {
					@Override
					public void run() {
						try {
							op.execute();
						} catch (Exception e) {
							System.out.println("Impossible d'executer l'opération dans"
									+ "le service de synchro");
						}
					}
				});
			}*/
		}
	}

	public void invokeLater(SynchOperation op) {
		_worker.invokeLater(op);
	}
	
}
