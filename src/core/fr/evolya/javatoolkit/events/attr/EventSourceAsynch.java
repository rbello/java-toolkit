package fr.evolya.javatoolkit.events.attr;

import java.util.ArrayList;

import fr.evolya.javatoolkit.code.annotations.AsynchOperation;
import fr.evolya.javatoolkit.code.funcint.Callback;

/**
 * Une source d'event permettant le trigger asynchrone
 *
 * @param <E>
 */
public class EventSourceAsynch<E extends EventListener> extends EventSource<E> {

	/**
	 * La liste des dispatch à faire.
	 */
	private ArrayList<Runnable> _jobs = new ArrayList<Runnable>();
	
	/**
	 * Le thread principal.
	 */
	private Thread _thread;
	
	public EventSourceAsynch() {
		super();
	}

	public EventSourceAsynch(Object sender) {
		super(sender);
	}
	
	public EventSourceAsynch(Class<? extends EventListener> listenerClass) {
		super(listenerClass);
	}

	public EventSourceAsynch(Class<? extends EventListener> listenerClass,
			Object sender) {
		super(listenerClass, sender);
	}
	
	@AsynchOperation
	public void triggerAsynch(final String eventName, final Object... args) {
		// Lancement asynchrone 
		addJob(new Runnable() {
			public void run() {
				// Dispatch synchrone
				broadcast(eventName, null, args, false);
			}
		});
	}
	
	@AsynchOperation
	public void triggerAsynch(final String eventName, final Runnable callback, final Object... args) {
		// Lancement asynchrone 
		addJob(new Runnable() {
			public void run() {
				// Dispatch synchrone
				broadcast(eventName, null, args, false);
				// Callback
				if (callback != null)
					callback.run();
			}
		});
	}
	
	@AsynchOperation
	public void triggerAsynch(final String eventName, @SuppressWarnings("rawtypes") final Callback callback, final Object... args) {
		// Lancement asynchrone 
		addJob(new Runnable() {
			@SuppressWarnings("unchecked")
			public void run() {
				try {
					// Dispatch synchrone
					boolean result = broadcast(eventName, null, args, callback != null);
					// Callback de success
					callback.onSuccess(result);
				}
				catch (Throwable t) {
					// Callback d'erreur
					if (callback != null)
						callback.onFailure(t);
				}
			}
		});
	}

	@AsynchOperation
	protected void addJob(Runnable job) {
		
		// On se synchronise sur l'eventSource.
		synchronized (this) {
		
			// On ajoute le job dans la list.
			_jobs.add(job);
			
			// Si le thread de propagation n'est pas en route,
			// on le lance.
			if (_thread == null) {
				start();
			}
		
		}
		
	}

	@AsynchOperation
	private void start() {
		
		// On fabrique le thread de propagation.
		_thread = new Thread(new Runnable() {
			public void run() {
				
				// Tant qu'il y a du travail...
				while (true) {
					
					// La chose à faire
					Runnable job = null;
					
					// On regarde s'il reste des choses à faire.
					// Pour cela, on se synchronise sur l'eventSource.
					synchronized (EventSourceAsynch.this) {
						
						// Plus de travail : on arrête
						if (_jobs.size() < 1) {
							// On arrête de boucler
							//System.out.println("Terminated");
							break;
						}
						
						// Encore du travail  : on recupère le premier
						// job ajouté (FIFO)
						job = _jobs.remove(0);
						
					}
					
					// Méfiance... mais ça ne devrait pas arriver.
					if (job == null) {
						//System.out.println("Terminated with error");
						break;
					}
					
					// Execution du job en toute précaution
					try {
						job.run();
					}
					catch (Throwable t) {
						//System.err.println("Error from " + getClass());
						t.printStackTrace();
					}
					
					// On continue
					
				}
				
				// Si on arrive ici, c'est qu'on a terminé le travail.
				// On supprime la référence du thread pour indiquer cet état.
				synchronized (EventSourceAsynch.this) {
					_thread = null;
				}
				
			}
			
		});
		
		// On lance le thread
		_thread.start();
		
	}

}
