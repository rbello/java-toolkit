package fr.evolya.javatoolkit.gui.swing.tasks;

import java.awt.EventQueue;

public abstract class GuiTask<A, B, C> {
	
	private boolean interrupt = false;
	private Thread thread;

	public void interrupt() {
		interrupt = true;
	}
	
	public void execute(final A param) {
		
		// Si on est déjà dans l'EDT
		if (EventQueue.isDispatchThread()) {
			
			// On en sort vers un nouveau thread
			thread = new Thread(new Runnable() {
				public void run() {
					
					// Interrupt
					if (interrupt) {
						return;
					}
					
					// On fait le preExecute
					B pre = onPreExecute(param);
					
					// Interrupt
					if (interrupt) {
						return;
					}
					
					// Et on repasse dans l'EDT pour la tâche IHM
					doInEDTAsynch(param, pre);
					
				}
			});
			
			thread.start();
			
		}
		
		// Si on n'est pas dans l'EDT
		else {
			// On fait le preExecute directement
			B pre = onPreExecute(param);
			// Interrupt
			if (interrupt) {
				return;
			}
			// Et on lance la tâche de fond dans l'EDT.
			// Le postExecute sera lancé en callback
			doInEDTAsynch(param, pre);
		}
		
	}
	
	private void doInEDTAsynch(final A input, final B pre) {
		// Dans le thread de l'EDT
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				// Interrupt
				if (interrupt) {
					return;
				}
				// On fait la tâche sur l'IHM
				final C result = doInEDT(pre, input);
				// Interrupt
				if (interrupt) {
					return;
				}
				// Et à la fin on appelle le postExecute dans un nouveau thread
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						// Interrupt
						if (interrupt) {
							return;
						}
						onPostExecute(result, pre, input);
					}
				});
			}
		});
	}

	/**
	 * Hors du thread EDT
	 */
	protected B onPreExecute(A input) {
		return null;
	}
	
	/**
	 * Dans le thread EDT
	 */
	protected abstract C doInEDT(B pre, A input);

	/**
	 * Hors du thread EDT
	 */
	protected void onPostExecute(C result, B pre, A input) { }
	
}
