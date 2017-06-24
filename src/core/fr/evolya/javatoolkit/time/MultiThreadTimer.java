package fr.evolya.javatoolkit.time;

/**
 * Un Timer multi-thread, ce qui signifie que la notification est traitée dans un thread
 * nouveau à chaque fois, ce qui permet de ne pas être retardé par le temps pris par
 * la notification, et d'avoir un timer plus régulier.
 */
public class MultiThreadTimer extends Timer {

	protected Runnable _runnable = new Runnable() {
		@Override
		public void run() {
			
			// Trigger threadé
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					raiseElapsed();
				}
			});
			
			// Lancement du sous-thread
			t.start();
			
			// Wait
			try {
				Thread.sleep(getFrequency());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
	};
	
	protected Thread _thread;
	
	@Override
	public synchronized boolean start() {
		if (_thread != null) {
			return false;
		}
		_thread = new Thread(_runnable);
		return true;
	}

	@Override
	public synchronized boolean stop() {
		if (_thread == null) {
			return false;
		}
		_thread.interrupt();
		_thread = null;
		return true;
	}

	@Override
	public synchronized boolean isAlive() {
		return _thread != null && _thread.isAlive();
	}

}
