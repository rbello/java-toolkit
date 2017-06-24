package fr.evolya.javatoolkit.app;

import java.util.logging.Logger;

import fr.evolya.javatoolkit.app.event.ApplicationBuilding;
import fr.evolya.javatoolkit.app.event.ApplicationStopping;
import fr.evolya.javatoolkit.code.Logs;
import fr.evolya.javatoolkit.code.annotations.AsynchOperation;
import fr.evolya.javatoolkit.events.fi.BindOnEvent;
import fr.evolya.javatoolkit.exceptions.StateChangeException;
import fr.evolya.javatoolkit.threading.worker.IWorker;
import fr.evolya.javatoolkit.threading.worker.TimerOperation;
import fr.evolya.javatoolkit.threading.worker.Worker;

/**
 * Classe adapter pour la création de services pouvant avoir des tâches à lancer
 * en background les unes après les autres.
 */
public abstract class ThreadedService implements AppActivity {
	
	/**
	 * Logger des services.
	 */
	public static final Logger LOGGER = Logs.getLogger("Service (v2)");

	/**
	 * Le worker.
	 */
	protected IWorker _worker;

	protected App _app;
	
	public ThreadedService() {
		_worker = new Worker(this.getClass().getSimpleName());
	}

	public final synchronized boolean isStarted() {
		return _worker.isActive();
	}

	@BindOnEvent(ApplicationBuilding.class)
	public void setApplication(App app) {
		_app = app;
	}
	
	@Override
	public final synchronized void start() throws StateChangeException, SecurityException {
		
		// Si le service est déjà lancé on ne fait rien
		if (isStarted()) {
			throw new StateChangeException("Service allready started");
		}
		
		// Lancement interne
		try {
			onStart();
		}
		catch (Exception e) {
			throw new StateChangeException("Unable to start service", e);
		}
		
		// On active le worker
		_worker.start();
		
		// Log
		if (LOGGER.isLoggable(Logs.INFO)) {
			LOGGER.log(Logs.INFO, getClass().getSimpleName() + " is started");
		}
		
	}

	protected abstract void onStart() throws Exception;

	@BindOnEvent(ApplicationStopping.class)
	@Override
	public final synchronized void stop() throws StateChangeException {
		
		// Si le service est arrêté, on ne fait rien 
		if (!isStarted()) {
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
		
		// Arrêt forcé
		interrupt();
		
	}
	
	@AsynchOperation
	public TimerOperation invokePeriodic(final Runnable run, final long delay) {
		return (TimerOperation) _worker.invokeLater(new TimerOperation(delay, run));
	}
	
	@AsynchOperation
	public void invokeLater(Runnable run) {
		_worker.invokeLater(run);
	}
	
	@Override
	public final synchronized void interrupt() {
		
		// Allready stopped
		if (!isStarted()) {
			return;
		}
		
		int count = _worker.getRunningCount() + _worker.getPendingCount();
		
		// On coupe le thread actif s'il est lancé
		if (_worker != null) {
			_worker.stop();
		}
		
		// Log
		if (LOGGER.isLoggable(Logs.INFO)) {
			LOGGER.log(Logs.INFO, "Service " + getClass().getSimpleName()
					+ " was stopped (" + count + " job(s) has been halted)");
		}
		
	}
	
	protected abstract void onStop() throws Exception;
	
	public void dispose() {
		interrupt();
	}

	public IWorker getWorker() {
		return _worker;
	}
	
	public App app() {
		return _app;
	}
	
}
