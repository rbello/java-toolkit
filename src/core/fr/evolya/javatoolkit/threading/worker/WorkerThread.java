package fr.evolya.javatoolkit.threading.worker;

public class WorkerThread extends Thread {
	
	private Worker _worker;
	private IOperation _operation;

	public WorkerThread(Worker worker) {
		_worker = worker;
		setName("WorkerThread-" + worker.getID() + "-" + hashCode());
	}
	
	@Override
	public void run() {
		
		// On boucle tant que le travail n'est pas terminé
		while (!Thread.currentThread().isInterrupted()) {

			// On se synchronise sur le worker
			synchronized (_worker) {
				
				// On recupère le job le plus ancien (FIFO) et on
				// le retire des jobs en cours
				_operation = _worker.getNextJob();
				
				// S'il n'y a plus de travail, on arrête ce thread
				if (_operation == null) {
					_worker.passivate(this);
					return;
				}
				
			}
			
			// Il s'agit du changement d'état INTERRUPTED pile-poil au
			// moment du bloc de synchro plus haut
			if (isInterrupted()) {
				// On enregistre une exception
				_operation.setFailure(new InterruptedException());
				// On replace l'opération dans le stack
				// On se synchronise sur le worker
				synchronized (_worker) {
					_worker._jobs.add(_operation);
				}
				// On retire l'opération
				_operation = null;
				// On déclare le thread comme terminé
				_worker.passivate(this);
				// On arrête
				return;
			}
			
			// On travail
			_worker.setState(WorkerState.WORKING);
			
			// Lancement du job
			_worker.getEventsWorker().triggerAsynch("onJobStarted", new Object[] { _operation, _worker });
			
			// On execute le job, en se protégeant des exceptions
			try {
				_operation.run();
				_worker.getEventsWorker().triggerAsynch("onJobFinished", new Object[] { _operation, _worker, null });
			}
			
			// Il s'agit d'une exception d'interruption
			catch (InterruptedException ex) {
				// On enregistre l'exception
				_operation.setFailure(ex);
				// Event
				_worker.getEventsWorker().triggerAsynch("onJobInterrupted", new Object[] { _operation, _worker, ex });
				// On replace l'opération dans le stack
				// On se synchronise sur le worker
				synchronized (_worker) {
					_worker._jobs.add(_operation);
				}
				// On retire l'opération
				_operation = null;
				// On déclare le thread comme terminé
				_worker.passivate(this);
				// On arrête
				return;
			}
			
			// Erreur dans le code utilisateur
			catch (Throwable ex) {
				// On enregistre l'exception
				_operation.setFailure(ex);
				// Event
				_worker.getEventsWorker().triggerAsynch("onJobFinished", new Object[] { _operation, _worker, ex });
				// On retire l'opération
				_operation = null;
			}
			
			// On laisse continuer pour consommer la pile des jobs
			
		}
		
		// Il s'agit du thread qui détecte son interruption
		_worker.passivate(this);
		
		// Il n'y a plus d'opérations
		_operation = null;
		
	}

	public IOperation getCurrentOperation() {
		return _operation;
	}
	
}
