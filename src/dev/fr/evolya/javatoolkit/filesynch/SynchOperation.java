package fr.evolya.javatoolkit.filesynch;

import fr.evolya.javatoolkit.threading.worker.*;

public abstract class SynchOperation extends AbstractOperation {

	public SynchOperation(String id) {
		super(id);
		// TODO Auto-generated constructor stub
	}

	public boolean equals(SynchOperation op) {
		System.out.println("Equals");
		return super.equals(op);
	}

	@Override
	public long getTimeRemaining() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void run() throws InterruptedException {
		setCompleted(false);
		try {
			execute();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		setCompleted(true);
	}

	protected abstract void execute() throws Exception;
	
}
