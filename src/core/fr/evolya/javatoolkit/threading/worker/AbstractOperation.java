package fr.evolya.javatoolkit.threading.worker;

public abstract class AbstractOperation implements IOperation {
	
	private boolean _completed = false;
	
	private String _id;

	private Throwable _failure;
	
	public AbstractOperation(String id) {
		_id = id;
	}

	@Override
	public float getOperationPercent() {
		return _completed ? 100 : 0;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + _id + "]";
	}

	@Override
	public boolean isCompleted() {
		return _completed;
	}

	public void setCompleted(boolean completed) {
		this._completed = completed;
	}

	@Override
	public boolean isCancelable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean cancel() throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		return false;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setFailure(Throwable ex) {
		_failure = ex;
	}
	
}
