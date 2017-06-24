package fr.evolya.javatoolkit.threading.worker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import fr.evolya.javatoolkit.app.AppActivity;
import fr.evolya.javatoolkit.code.IncaLogger;
import fr.evolya.javatoolkit.events.attr.EventSourceAsynch;
import fr.evolya.javatoolkit.exceptions.NotImplementedException;

public class Worker implements IWorker {
	
	public static final Logger LOGGER = IncaLogger.getLogger("Worker");

	private EventSourceAsynch<WorkerListener> _eventsWorker =
			new EventSourceAsynch<>(WorkerListener.class, this);
	
	private int _maxRunningJobs;
	
	private WorkerState _state = WorkerState.STOPPED;

	List<IOperation> _jobs = new ArrayList<IOperation>();
	
	private List<WorkerThread> _threads = new ArrayList<WorkerThread>();

	private String id;

	public Worker() {
		this(null, 1);
	}
	
	public Worker(int maxRunningJobs) {
		this(null, maxRunningJobs);
	}
	
	public Worker(AppActivity parent) {
		this(parent, 1);
	}
	
	public Worker(AppActivity parent, int maxRunningJobs) {
		id = parent == null ? "" + hashCode() : parent.getClass().getSimpleName();
		_maxRunningJobs = maxRunningJobs;
	}

	IOperation getNextJob() {
		return _jobs.size() > 0 ? _jobs.remove(0) : null;
	}
	
	@Override
	public void start() {
		synchronized (this) {
			// Déjà lancé
			if (_state != WorkerState.STOPPED) {
//				throw new IllegalStateException("Worker allready started");
				return;
			}
			// Il n'y a pas de travail
			if (_jobs.size() == 0) {
				setState(WorkerState.IDLE);
			}
			// Il y a du travail
			else {
				consume();
			}
		}
	}

	@Override
	public synchronized void stop() {
		
//		System.err.println(toString() + " : interrupting " + _threads.size() + " threads");
		
		if (_state == WorkerState.STOPPING || _state == WorkerState.STOPPED)
			return;
		
		// On place l'état d'arrêt
		setState(WorkerState.STOPPING);
		
		// Arrêt immédiat
		if (_threads.isEmpty()) {
			setState(WorkerState.STOPPED);
			return;
		}
		
		// On demande l'interruption de tous les threads
		for (WorkerThread t : _threads) {
			
			// Le thread est déjà en arrêt
			if (t.isInterrupted()) continue;
			
			// On demande son interruption
			t.interrupt();
			
		}

	}

	public IOperation invokeLater(IOperation job) {
		// Méfiance...
		if (job == null) {
			throw new NullPointerException();
		}
		synchronized (this) {
			// On rajoute le job
			_jobs.add(job);
			// Event
			_eventsWorker.triggerAsynch("onJobAdded", new Object[] { job, this });
			// On lance la consommation par les threads
			if (_state != WorkerState.STOPPED && _state != WorkerState.STOPPING) {
				consume();
			}
		}
		return job;
	}
	
	@Override
	public IOperation invokeLater(Runnable job) {
		// Méfiance...
		if (job == null) {
			throw new NullPointerException();
		}
		RunnableOperation operation = new RunnableOperation(job);
		invokeLater(operation);
		return operation;
	}

	private void consume() {
	
//		System.out.println("Active stack=" + _jobs.size() + " threads="
//				+ _threads.size() +"/" + _maxRunningJobs);
		
		// Aucun travail
		if (_jobs.size() == 0) return;
		
		// Il y a déjà assez de threads actifs
		if (_threads.size() >= _maxRunningJobs) return;
		
		// On fabrique un thread, qui va s'occuper d'effectuer
		// les travaux qui sont dans la pile.
		WorkerThread thread = new WorkerThread(this);
		
		// On ajoute le thread dans la liste
		_threads.add(thread);
		
		// On lance le thread. C'est lui qui va s'occuper de
		// de se fermer tout seul.
		thread.start();
		
		// On rajoute des threads si neccessaire
		if (_threads.size() < _maxRunningJobs && _jobs.size() > _threads.size()) {
			consume();
		}
		
	}
	
	void passivate(WorkerThread thread) {
		
		// Le thread est déjà retiré
		if (!_threads.contains(thread)) {
			return;
		}
		
		// On retire le thread
		_threads.remove(thread);
		
		// On a terminé tout le travail
		if (_threads.isEmpty()) {
			setState(_state == WorkerState.STOPPING ? WorkerState.STOPPED : WorkerState.IDLE);
		}
		
	}
	
	/**
	 * Les events du worker.
	 */
	public EventSourceAsynch<WorkerListener> getEventsWorker() {
		return _eventsWorker;
	}

	public int getMaxRunningJobs() {
		return _maxRunningJobs;
	}

	public void setMaxRunningJobs(int maxRunningJobs) {
		_maxRunningJobs = maxRunningJobs;
	}

	@Override
	public WorkerState getState() {
		return _state;
	}

	@Override
	public IOperation invokeLater(IRunnable job) {
		// Méfiance...
		if (job == null) {
			throw new NullPointerException();
		}
		RunnableOperation operation = new RunnableOperation(job);
		invokeLater(operation);
		return operation;
	}

	@Override
	public IOperation invokeAndWait(IOperation job) {
		throw new NotImplementedException(); // TODO
	}

	@Override
	public IOperation invokeAndWait(IRunnable job) {
		throw new NotImplementedException(); // TODO
	}

	@Override
	public int getRunningCount() {
		return _threads.size();
	}

	@Override
	public int getPendingCount() {
		return _jobs.size();
	}
	
	void setState(WorkerState state) {
		if (_state != state && state != null) {
			_state = state;
			if (LOGGER.isLoggable(IncaLogger.DEBUG)) {
				LOGGER.log(IncaLogger.DEBUG, toString() + " : changed state to " + state);
			}
			_eventsWorker.triggerAsynch("onWorkerStateChanged", state, _state, this);
		}
	}
	
	@Override
	public String toString() {
		return "Worker " + id;
	}
	
	public String getID() {
		return id;
	}

	public void invokeAndLoop(int milliseconds, Runnable job) {
		invokeLater(new TimerOperation(milliseconds, job));
	}

	@Override
	public synchronized Collection<IOperation> getPendingOperations() {
		return new ArrayList<IOperation>(_jobs);
	}

	@Override
	public synchronized Collection<IOperation> getRunningOperations() {
		ArrayList<IOperation> jobs = new ArrayList<IOperation>();
		for (WorkerThread thread : _threads) {
			jobs.add(thread.getCurrentOperation());
		}
		return jobs;
	}

	@Override
	public synchronized Collection<IOperation> getOperations() {
		ArrayList<IOperation> jobs = new ArrayList<IOperation>(_jobs);
		for (WorkerThread thread : _threads) {
			IOperation op = thread.getCurrentOperation();
			if (op != null)
				jobs.add(op);
		}
		return jobs;
	}

	@Override
	public boolean isActive() {
		return _state == WorkerState.IDLE || _state == WorkerState.WORKING;
	}

}
