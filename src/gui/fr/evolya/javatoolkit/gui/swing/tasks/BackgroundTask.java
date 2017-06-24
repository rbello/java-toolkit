package fr.evolya.javatoolkit.gui.swing.tasks;

import java.awt.EventQueue;

public abstract class BackgroundTask<A, B, C> {
	
	private boolean interrupt = false;
	
	public void interrupt() {
		interrupt = true;
	}

	public void execute(final A param) {
		
		// Si on est déjà dans l'EDT
		if (EventQueue.isDispatchThread()) {
			// On fait le preExecute directement
			B pre = onPreExecute(param);
			// Interrupt
			if (interrupt) {
				return;
			}
			// Et on lance la tâche de fond dans un autre thread.
			// Le postExecute sera lancé en callback
			doInBackgroundAsynch(param, pre);
		}
		
		// Si on n'est pas dans l'EDT
		else {
			// On y passe
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					// Interrupt
					if (interrupt) {
						return;
					}
					// Pour le preExecute
					B pre = onPreExecute(param);
					// Interrupt
					if (interrupt) {
						return;
					}
					// Et on repasse dans un autre thread pour la tâche de fond
					doInBackgroundAsynch(param, pre);
				}
			});
		}
		
	}
	
	private void doInBackgroundAsynch(final A input, final B pre) {
		// Dans un thread hors EDT
		new Thread(new Runnable() {
			public void run() {
				// Interrupt
				if (interrupt) {
					return;
				}
				// On fait la tâche de fond
				final C result = doInBackground(pre, input);
				// Interrupt
				if (interrupt) {
					return;
				}
				// Et à la fin on appelle le postExecute dans l'EDT
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
		}).start();
	}

	/**
	 * Dans le thread EDT
	 */
	protected B onPreExecute(A param) {
		return null;
	}
	
	/**
	 * Hors du thread EDT
	 */
	protected abstract C doInBackground(B pre, A input);

	/**
	 * Dans le thread EDT
	 */
	protected void onPostExecute(C result, B pre, A input) { }
	
}
