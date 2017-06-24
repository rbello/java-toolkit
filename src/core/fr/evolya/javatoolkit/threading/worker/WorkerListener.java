package fr.evolya.javatoolkit.threading.worker;

import fr.evolya.javatoolkit.events.attr.EventListener;

public interface WorkerListener extends EventListener {

	public void onWorkerStateChanged(WorkerState newState, WorkerState oldState, IWorker worker);

	public void onJobAdded(IOperation job, IWorker worker);
	
	public void onJobStarted(IOperation job, IWorker worker);
	
	public void onJobFinished(IOperation job, IWorker worker, Throwable error);
	
	public void onJobInterrupted(IOperation job, IWorker worker, InterruptedException error);
	
}
