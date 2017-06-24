package fr.evolya.javatoolkit.time;

/**
 * Un Timer mono-thread, ce qui signifie que le retard pris par chaque notification
 * a une incidence sur la fréquence des ticks.
 */
public class MonoThreadTimer extends Timer {

	private boolean _raiseExceptions;

	public MonoThreadTimer(boolean raiseExceptions) {
		_raiseExceptions = raiseExceptions;
	}
	
	protected Runnable _runnable = new Runnable() {
		public void run() {
			
			while (true) {
				
				// Wait
				try {
					Thread.sleep(getFrequency());
				} catch (InterruptedException e) {
					return;
				}
				
				// Trigger
				if (_raiseExceptions) {
					raiseElapsed();
				}
				else {
					try {
						raiseElapsed();
					}
					catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			
			}
			
		}
	};
	
	protected Thread _thread = null;
	
	@Override
	public synchronized boolean start() {
		if (_thread != null) {
			return false;
		}
		_thread = new Thread(_runnable);
		_thread.setName("MonoThreadTimer-" + hashCode());
		_thread.start();
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
