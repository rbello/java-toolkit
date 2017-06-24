package fr.evolya.javatoolkit.threading.worker;

public class RunnableOperation extends AbstractOperation {

	protected IRunnable _job;
	
	public RunnableOperation(IRunnable job) {
		this(job, "Operation " + job.hashCode());
	}
	
	public RunnableOperation(Runnable job) {
		this(job, "Operation " + job.hashCode());
	}
	
	public RunnableOperation(IRunnable job, String id) {
		super(id);
		_job = job;
	}

	public RunnableOperation(final Runnable job, String id) {
		this(new IRunnable() {
			@Override
			public void run() throws InterruptedException {
				job.run();
			}
		}, id);
	}

	@Override
	public long getTimeRemaining() {
		return -1;
	}

	@Override
	public void run() throws InterruptedException {
		setCompleted(false);
		_job.run();
		setCompleted(true);
	}

	public IRunnable getJob() {
		return _job;
	}
	
}
