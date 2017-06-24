package fr.evolya.javatoolkit.threading.worker;

import java.util.Collection;

import fr.evolya.javatoolkit.code.annotations.AsynchOperation;
import fr.evolya.javatoolkit.events.attr.EventSourceAsynch;

public interface IWorker {

	public EventSourceAsynch<WorkerListener> getEventsWorker();
	
	public void start();
	
	public void stop();
	
	public WorkerState getState();
	
	@AsynchOperation
	public IOperation invokeLater(IOperation job);
	
	@AsynchOperation
	public IOperation invokeLater(IRunnable job);
	
	@AsynchOperation
	public IOperation invokeLater(Runnable run);
	
	public IOperation invokeAndWait(IOperation job);
	
	public IOperation invokeAndWait(IRunnable job);
	
	public int getRunningCount();
	
	public int getPendingCount();

	public Collection<IOperation> getPendingOperations();
	
	public Collection<IOperation> getRunningOperations();
	
	public Collection<IOperation> getOperations();

	public int getMaxRunningJobs();
	
	public void setMaxRunningJobs(int i);

	public boolean isActive();

}
