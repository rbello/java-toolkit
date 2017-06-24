package fr.evolya.javatoolkit.threading.worker;

public class TimerOperation implements IOperation {

	private Runnable _job;
	private long _frequency;
	private boolean _loop;
	private Throwable _failure;

	public TimerOperation(long milliseconds, Runnable job) {
		_job = job;
		_frequency = milliseconds;
		_loop = true;
	}

	@Override
	public float getOperationPercent() {
		return 0;
	}

	@Override
	public long getTimeRemaining() {
		return 0;
	}

	@Override
	public void run() throws InterruptedException {
		while (_loop) {
			try {
				if (Thread.interrupted()) return;
				_job.run();
				if (Thread.interrupted()) return;
				Thread.sleep(_frequency);
			}
			catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
				return;
			}
			catch (Throwable t) {
				_failure = t;
			}
		}
	}

	@Override
	public boolean isCompleted() {
		return !_loop;
	}

	@Override
	public boolean isCancelable() {
		return true;
	}

	@Override
	public boolean cancel() throws UnsupportedOperationException {
		_loop = false;
		return true;
	}

	@Override
	public boolean hasFailed() {
		return _failure != null;
	}

	@Override
	public Throwable getFailure() {
		return _failure;
	}

	@Override
	public Object getInitiator() {
		return null;
	}

	@Override
	public void setFailure(Throwable ex) {
		_failure = ex;
	}

	public long getFrequency() {
		return _frequency;
	}

	public void setFrequency(long frequency) {
		_frequency = frequency;
	}

}
